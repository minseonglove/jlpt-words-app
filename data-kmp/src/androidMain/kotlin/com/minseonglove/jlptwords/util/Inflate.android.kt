package com.minseonglove.jlptwords.util

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

internal actual fun inflateRaw(data: ByteArray): ByteArray {
    if (data.isEmpty()) return ByteArray(0)
    val inflater = Inflater(true)
    return try {
        inflater.setInput(data)
        val out = ByteArrayOutputStream(data.size * 4)
        val buffer = ByteArray(8192)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                error("손상된 deflate 스트림")
            }
            out.write(buffer, 0, count)
        }
        out.toByteArray()
    } finally {
        inflater.end()
    }
}
