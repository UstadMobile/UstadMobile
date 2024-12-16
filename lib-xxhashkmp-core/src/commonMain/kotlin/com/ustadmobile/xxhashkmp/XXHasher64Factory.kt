package com.ustadmobile.xxhashkmp

interface XXHasher64Factory {

    fun newHasher(seed: Long): XXHasher64

}