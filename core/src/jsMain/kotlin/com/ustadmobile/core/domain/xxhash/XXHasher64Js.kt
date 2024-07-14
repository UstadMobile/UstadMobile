package com.ustadmobile.core.domain.xxhash

import com.ustadmobile.core.wrappers.xxhash.XXH64
import js.typedarrays.toUint8Array

class XXHasher64Js(private val hasher64: XXH64): XXHasher64 {

    override fun update(data: ByteArray) {
        val array = data.toUint8Array()
        hasher64.update(array.buffer)
    }

    override fun digest(): Long {
        val digestStr = hasher64.digest().toString(10)
        return digestStr.toULong().toLong()
    }
}