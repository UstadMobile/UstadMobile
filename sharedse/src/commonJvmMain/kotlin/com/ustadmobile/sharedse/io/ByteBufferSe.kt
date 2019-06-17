package com.ustadmobile.sharedse.io


typealias UMByteBuffer = java.nio.ByteBuffer

actual class ByteBufferSe {

    private lateinit var dw: UMByteBuffer

    constructor(dw: UMByteBuffer) {
        this.dw = dw
    }

    actual fun getLong(): Long = dw.long

    actual fun getShort(): Short = dw.short

    actual fun getInt(): Int = dw.int

    actual companion object {
        actual fun wrap(array: ByteArray) = ByteBufferSe(java.nio.ByteBuffer.wrap(array))
    }

}