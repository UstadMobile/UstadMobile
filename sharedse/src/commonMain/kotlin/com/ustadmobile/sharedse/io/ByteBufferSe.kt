package com.ustadmobile.sharedse.io

import io.ktor.utils.io.core.ByteOrder

/**
 * This is a simple expect / actual so that we can reference ByteBufferSe in common code.
 */
expect class ByteBufferSe {

    fun getLong(): Long

    fun getShort(): Short

    fun getInt(): Int

    fun array(): ByteArray

    fun getChar(): Char

    fun get(dst: ByteArray, offset: Int, length: Int): ByteBufferSe

    fun remaining(): Int

    fun position(): Int

    fun slice(): ByteBufferSe

    fun order(byteOrder: ByteOrder): ByteBufferSe

    fun put(byteArr: ByteArray, offset: Int, length: Int): ByteBufferSe

    fun put(byteArr: ByteArray): ByteBufferSe

    fun put(value: Byte): ByteBufferSe

    fun putInt(value: Int): ByteBufferSe

    fun putLong(value: Long): ByteBufferSe

    fun putShort(value: Short): ByteBufferSe

    fun putChar(value: Char): ByteBufferSe

    fun clear(): ByteBufferSe

    companion object {
        fun wrap(array: ByteArray): ByteBufferSe

        fun allocate(length: Int): ByteBufferSe

    }

}

