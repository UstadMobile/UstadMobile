package com.ustadmobile.libcache.md5

import java.security.MessageDigest

class Md5DigestJvm : Md5Digest {
    private val messageDigest = MessageDigest.getInstance("MD5")

    override fun digest(bytes: ByteArray): ByteArray {
        messageDigest.reset()
        return messageDigest.digest(bytes)
    }

}