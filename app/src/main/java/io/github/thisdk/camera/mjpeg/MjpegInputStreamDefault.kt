package io.github.thisdk.camera.mjpeg

import java.io.*
import java.util.*

class MjpegInputStreamDefault internal constructor(inputStream: InputStream?) :
    MjpegInputStream(BufferedInputStream(inputStream, FRAME_MAX_LENGTH)) {

    private val SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val EOF_MARKER = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    private var mContentLength = -1

    @Throws(IOException::class)
    private fun getEndOfSequence(inputStream: DataInputStream, sequence: ByteArray): Int {
        var seqIndex = 0
        var c: Byte
        for (i in 0 until FRAME_MAX_LENGTH) {
            c = inputStream.readUnsignedByte().toByte()
            if (c == sequence[seqIndex]) {
                seqIndex++
                if (seqIndex == sequence.size) {
                    return i + 1
                }
            } else {
                seqIndex = 0
            }
        }
        return -1
    }

    @Throws(IOException::class)
    private fun getStartOfSequence(inputStream: DataInputStream, sequence: ByteArray): Int {
        val end = getEndOfSequence(inputStream, sequence)
        return if (end < 0) -1 else end - sequence.size
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    private fun parseContentLength(headerBytes: ByteArray): Int {
        val headerIn = ByteArrayInputStream(headerBytes)
        val props = Properties()
        props.load(headerIn)
        return props.getProperty("Content-Length").toInt()
    }

    @Throws(IOException::class)
    fun readHeader(): ByteArray {
        mark(FRAME_MAX_LENGTH)
        val headerLen = getStartOfSequence(this, SOI_MARKER)
        reset()
        val header = ByteArray(headerLen)
        readFully(header)
        return header
    }

    @Throws(IOException::class)
    fun readMjpegFrame(header: ByteArray): ByteArray {
        mContentLength = try {
            parseContentLength(header)
        } catch (iae: IllegalArgumentException) {
            getEndOfSequence(this, EOF_MARKER)
        }
        reset()
        val frameData = ByteArray(mContentLength)
        skipBytes(header.size)
        readFully(frameData)
        return frameData
    }

    companion object {
        private const val HEADER_MAX_LENGTH = 100
        private const val FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH
    }

}