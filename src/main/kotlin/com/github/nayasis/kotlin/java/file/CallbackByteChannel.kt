package com.github.nayasis.kotlin.java.file

import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel


class CallbackByteChannel(
    private val rbc: ReadableByteChannel,
    private val sizeTotal: Long,
    private val callback: ((read: Long, size: Long) -> Unit)?,
): ReadableByteChannel {

    private var sizeRead: Long = 0

    override fun close() = rbc.close()

    override fun isOpen(): Boolean = rbc.isOpen

    override fun read(buffer: ByteBuffer): Int {
        var n: Int
        if (rbc.read(buffer).also { n = it } > 0) {
            sizeRead += n.toLong()
            callback?.invoke(sizeRead,sizeTotal)
        }
        return n
    }

}