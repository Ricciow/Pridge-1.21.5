package io.github.ricciow.util

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterOutputStream

object UrlFormatter {
    private const val CUSTOM_CHARSET =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789áàäãâóòôöõÖÕúùûüÚÙÛÜñÑœŒÿŸἰἱἲἳἴἵἶἷÍÌÎÏąćĉċçéèêëабвгдеёжзийклмнопрстуфхцчшщъыьэюя:?&=+#%-{}|[]"
    private val BASE = CUSTOM_CHARSET.length.toBigInteger()
    private val CHAR_TO_INT_MAP = CUSTOM_CHARSET.withIndex().associate { it.value to it.index.toLong() }

    private fun compress(data: ByteArray): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            DeflaterOutputStream(outputStream, Deflater(Deflater.BEST_COMPRESSION)).use { deflaterStream ->
                deflaterStream.write(data)
            }
            outputStream.toByteArray()
        }
    }

    private fun decompress(data: ByteArray): ByteArray {
        if (data.isEmpty()) return ByteArray(0)
        return ByteArrayOutputStream().use { outputStream ->
            InflaterOutputStream(outputStream, Inflater()).use { inflaterStream ->
                inflaterStream.write(data)
            }
            outputStream.toByteArray()
        }
    }

    fun encode(url: String): String {
        if (url.isEmpty()) return ""

        var number = BigInteger(byteArrayOf(1) + compress(url.toByteArray(Charsets.UTF_8)))
        val encoded = StringBuilder()
        while (number > BigInteger.ZERO) {
            val remainder = number.mod(BASE).toInt()
            encoded.append(CUSTOM_CHARSET[remainder])
            number = number.divide(BASE)
        }

        return encoded.reverse().toString()
    }

    fun decode(encodedUrl: String): String {
        if (encodedUrl.isEmpty()) return ""

        var number = BigInteger.ZERO
        encodedUrl.forEach { char ->
            val value = CHAR_TO_INT_MAP[char]
                ?: throw IllegalArgumentException("Encoded data contains an invalid character: '$char'")
            number = number.multiply(BASE).add(BigInteger.valueOf(value.toLong()))
        }

        val compressedBytes = number.toByteArray()
            .takeIf { it.isNotEmpty() && it[0] == 1.toByte() }
            ?.drop(1)
            ?.toByteArray()
            ?: throw IllegalStateException("Invalid data format: missing or incorrect prefix byte.")

        return decompress(compressedBytes).toString(Charsets.UTF_8)
    }
}