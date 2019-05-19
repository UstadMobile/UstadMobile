package com.ustadmobile.lib.util

import kotlin.jvm.JvmStatic

expect object Base64Coder {

    val table: IntArray

    @JvmStatic
    fun encodeToString(value: String): String

    @JvmStatic
    fun encodeToByteArray(value: String): ByteArray

    @JvmStatic
    fun encodeToString(value: ByteArray): String

    @JvmStatic
    fun decodeBase64(value: String): String


    @JvmStatic
    fun decodeToByteArray(value: String): ByteArray
}