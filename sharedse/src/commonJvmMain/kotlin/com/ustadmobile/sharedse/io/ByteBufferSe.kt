package com.ustadmobile.sharedse.io
import io.ktor.utils.io.core.ByteOrder

typealias UMByteBuffer = java.nio.ByteBuffer

actual class ByteBufferSe {

    private lateinit var dw: UMByteBuffer

    constructor(dw: UMByteBuffer) {
        this.dw = dw
    }

    actual fun getLong(): Long = dw.long

    actual fun getShort(): Short = dw.short

    actual fun getInt(): Int = dw.int

    actual fun array(): ByteArray = dw.array()

    actual fun getChar(): Char = dw.char

    actual fun get(dst: ByteArray, offset: Int, length: Int): ByteBufferSe {
        return ByteBufferSe(dw.get(dst, offset, length))
    }

    actual fun remaining(): Int {
        return dw.remaining()
    }

    actual fun position(): Int {
        return dw.position()
    }

    actual fun slice(): ByteBufferSe {
        return ByteBufferSe(dw.slice())
    }

    actual fun order(byteOrder: ByteOrder): ByteBufferSe {
        dw.order(if(byteOrder == ByteOrder.BIG_ENDIAN) java.nio.ByteOrder.BIG_ENDIAN else java.nio.ByteOrder.LITTLE_ENDIAN)
        return this
    }

    actual fun clear(): ByteBufferSe {
        dw.clear()
        return this
    }

    actual fun put(byteArr: ByteArray, offset: Int, length: Int): ByteBufferSe {
        dw.put(byteArr, offset, length)
        return this
    }

    actual fun put(byteArr: ByteArray): ByteBufferSe {
        dw.put(byteArr)
        return this
    }

    actual fun put(value: Byte): ByteBufferSe {
        dw.put(value)
        return this
    }

    actual fun putChar(value: Char):ByteBufferSe {
        dw.putChar(value)
        return this
    }

    actual fun putInt(value: Int) : ByteBufferSe {
        dw.putInt(value)
        return this
    }

    actual fun putLong(value: Long): ByteBufferSe {
        dw.putLong(value)
        return this
    }

    actual fun putShort(value: Short): ByteBufferSe {
        dw.putShort(value)
        return this
    }

    actual companion object {
        actual fun wrap(array: ByteArray) = ByteBufferSe(java.nio.ByteBuffer.wrap(array))

        actual fun allocate(length: Int) = ByteBufferSe(java.nio.ByteBuffer.allocate(length))
    }

}