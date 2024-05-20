package com.ustadmobile.core.domain.xxhash

interface XXHasher64 {

    fun update(data: ByteArray)

    fun digest(): Long

}