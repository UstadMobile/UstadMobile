package com.ustadmobile.core.util.digest

import java.security.MessageDigest

class DigesterAndroid(private val messageDigest: MessageDigest) : Digester{
    override fun digest(bytes: ByteArray, offset: Int, len: Int): ByteArray {
        messageDigest.reset()
        messageDigest.update(bytes, offset, len)
        return messageDigest.digest()
    }

}