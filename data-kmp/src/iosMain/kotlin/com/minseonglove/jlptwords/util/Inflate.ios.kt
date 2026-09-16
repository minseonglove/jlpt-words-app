package com.minseonglove.jlptwords.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDataCompressionAlgorithmZlib
import platform.Foundation.create
import platform.Foundation.decompressedDataUsingAlgorithm
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun inflateRaw(data: ByteArray): ByteArray {
    if (data.isEmpty()) return ByteArray(0)
    val nsData =
        data.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
        }
    val inflated =
        nsData.decompressedDataUsingAlgorithm(NSDataCompressionAlgorithmZlib, null)
            ?: error("손상된 deflate 스트림")
    val result = ByteArray(inflated.length.toInt())
    if (result.isNotEmpty()) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), inflated.bytes, inflated.length)
        }
    }
    return result
}
