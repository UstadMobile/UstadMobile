package com.ustadmobile.sharedse.security


expect fun getMessageDigestInstance(algorithm: String): MessageDigestSe

expect abstract class MessageDigestSe {

    fun update(buffer: ByteArray, offset: Int, len: Int)

    fun digest(): ByteArray

}