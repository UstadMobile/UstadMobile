package com.ustadmobile.sharedse.io

expect class ByteBufferSe {

    fun getLong(): Long

    fun getShort(): Short

    fun getInt(): Int

    companion object {
        fun wrap(array: ByteArray): ByteBufferSe
    }

}

