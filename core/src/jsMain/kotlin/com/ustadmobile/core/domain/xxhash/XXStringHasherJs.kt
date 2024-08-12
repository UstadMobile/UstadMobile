package com.ustadmobile.core.domain.xxhash

import com.ustadmobile.core.wrappers.xxhash.XXH
import io.ktor.utils.io.core.toByteArray
import js.typedarrays.toUint8Array

class XXStringHasherJs: XXStringHasher {

    override fun hash(string: String): Long {
        val array = string.toByteArray().toUint8Array()
        val hash = XXH.h64(array.buffer, 0).toString(10)
        return hash.toULong().toLong()
    }

}