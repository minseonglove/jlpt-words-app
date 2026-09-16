package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.util.currentTimeMillis
import com.minseonglove.jlptwords.util.elapsedRealtime
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.fromHttpToGmtDate
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readFully
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.math.abs

class TimeDataSource(
    private val httpClient: HttpClient,
) {
    private val cacheLock = Mutex()

    private var lastNetworkTimeMillis: Long? = null

    private var lastElapsedRealtimeMillis: Long? = null

    /** 기기 로컬 시계 기준 현재 시각(epoch millis). */
    fun getDeviceTimeMillis(): Long = currentTimeMillis()

    suspend fun getNetworkTimeMillis(): Long? =
        withContext(Dispatchers.IO) {
            cacheLock.withLock {
                val baseNetwork = lastNetworkTimeMillis
                val baseElapsed = lastElapsedRealtimeMillis
                if (baseNetwork != null && baseElapsed != null) {
                    val delta = elapsedRealtime() - baseElapsed
                    baseNetwork + delta
                } else {
                    val networkNow =
                        runCatching { fetchNtpTimeMillis() }.getOrNull()
                            ?: fetchHttpDateHeaderMillis()
                    lastNetworkTimeMillis = networkNow
                    lastElapsedRealtimeMillis = elapsedRealtime()
                    networkNow
                }
            }
        }

    private suspend fun fetchNtpTimeMillis(): Long {
        val host = "time.google.com"
        val port = 123
        val buffer = ByteArray(48)

        // LI=0, VN=4, Mode=3 (client)
        buffer[0] = 0b00_100_011

        return withTimeout(2_000) {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket =
                aSocket(selectorManager).udp().bind()

            try {
                val requestSendTimeUnixMs = currentTimeMillis()
                writeTimestamp(buffer, 40, requestSendTimeUnixMs)

                val packet =
                    buildPacket {
                        writeFully(buffer)
                    }
                val datagram =
                    Datagram(
                        packet,
                        io.ktor.network.sockets
                            .InetSocketAddress(host, port),
                    )
                socket.send(datagram)

                val received = socket.receive()
                val responseRecvTimeUnixMs = currentTimeMillis()

                // Packet을 ByteArray로 변환
                val responseBuffer = ByteArray(48)
                received.packet.readFully(responseBuffer)

                // 서버가 보낸 타임스탬프들 (Unix ms)
                val originateTimeUnixMs = readTimestamp(responseBuffer, 24)
                val receiveTimeUnixMs = readTimestamp(responseBuffer, 32)
                val transmitTimeUnixMs = readTimestamp(responseBuffer, 40)

                // 간단한 유효성 확인: 서버가 요청의 originate를 복사했는지 (허용 오차 몇 ms)
                if (originateTimeUnixMs != 0L &&
                    abs(originateTimeUnixMs - requestSendTimeUnixMs) > 10_000
                ) {
                    throw IllegalStateException("NTP originate mismatch")
                }

                // NTP 오프셋 계산 (밀리초 단위)
                val t0 = requestSendTimeUnixMs
                val t1 = receiveTimeUnixMs
                val t2 = transmitTimeUnixMs
                val t3 = responseRecvTimeUnixMs

                val offsetMs = ((t1 - t0) + (t2 - t3)) / 2
                t3 + offsetMs
            } finally {
                socket.close()
                selectorManager.close()
            }
        }
    }

    private suspend fun fetchHttpDateHeaderMillis(): Long? =
        runCatching {
            val startElapsed = elapsedRealtime()
            val response: HttpResponse =
                withTimeout(3_000) {
                    httpClient.head("https://www.google.com/generate_204")
                }

            val dateHeader = response.headers[HttpHeaders.Date]
            if (dateHeader != null) {
                val serverTimeMs = parseHttpDate(dateHeader)
                if (serverTimeMs > 0L) {
                    val endElapsed = elapsedRealtime()
                    val rttHalf = (endElapsed - startElapsed) / 2
                    serverTimeMs + rttHalf
                } else {
                    null
                }
            } else {
                null
            }
        }.getOrNull()

    // Ktor 의 GMTDateParser 는 SimpleDateFormat 패턴을 쓰지 않는다(토큰은 s m h d M Y z * 뿐).
    // 직접 패턴을 넘기면 조용히 항상 파싱에 실패하므로, RFC 형식 목록을 내장한 Ktor 확장을 쓴다.
    private fun parseHttpDate(dateString: String): Long =
        try {
            dateString.fromHttpToGmtDate().timestamp
        } catch (e: Exception) {
            0L
        }

    private fun readTimestamp(
        buffer: ByteArray,
        offset: Int,
    ): Long {
        val seconds = readUnsignedInt(buffer, offset)
        val fraction = readUnsignedInt(buffer, offset + 4)

        val unixSeconds = seconds - NTP_EPOCH_OFFSET_SECONDS
        val msFromSeconds = unixSeconds * 1000L
        val msFromFraction = (fraction * 1000L) ushr 32
        return msFromSeconds + msFromFraction
    }

    private fun writeTimestamp(
        buffer: ByteArray,
        offset: Int,
        unixTimeMs: Long,
    ) {
        val secondsSinceUnix = unixTimeMs / 1000L
        val fractionMs = (unixTimeMs % 1000L)
        val ntpSeconds = secondsSinceUnix + NTP_EPOCH_OFFSET_SECONDS
        val ntpFraction = (fractionMs shl 32) / 1000L

        writeUnsignedInt(buffer, offset, ntpSeconds)
        writeUnsignedInt(buffer, offset + 4, ntpFraction)
    }

    private fun readUnsignedInt(
        buffer: ByteArray,
        offset: Int,
    ): Long {
        var result = 0L
        for (i in 0 until 4) {
            result = (result shl 8) or (buffer[offset + i].toLong() and 0xFF)
        }
        return result
    }

    private fun writeUnsignedInt(
        buffer: ByteArray,
        offset: Int,
        value: Long,
    ) {
        buffer[offset + 0] = ((value ushr 24) and 0xFF).toByte()
        buffer[offset + 1] = ((value ushr 16) and 0xFF).toByte()
        buffer[offset + 2] = ((value ushr 8) and 0xFF).toByte()
        buffer[offset + 3] = (value and 0xFF).toByte()
    }

    private companion object {
        const val NTP_EPOCH_OFFSET_SECONDS: Long = 2_208_988_800L
    }
}
