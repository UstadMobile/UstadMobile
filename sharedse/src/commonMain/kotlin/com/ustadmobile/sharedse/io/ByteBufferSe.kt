package com.ustadmobile.sharedse.io

expect class ByteBufferSe {

    fun getLong(): Long

    fun getShort(): Short

    fun getInt(): Int

    fun array(): ByteArray

    fun getChar(): Char

    fun get(dst: ByteArray, offset: Int, length: Int): ByteBufferSe

    companion object {
        fun wrap(array: ByteArray): ByteBufferSe
    }

}

