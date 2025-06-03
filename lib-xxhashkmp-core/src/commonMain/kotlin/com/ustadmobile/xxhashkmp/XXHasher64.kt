package com.ustadmobile.xxhashkmp

interface XXHasher64 {

    fun update(data: ByteArray)

    fun digest(): Long

}