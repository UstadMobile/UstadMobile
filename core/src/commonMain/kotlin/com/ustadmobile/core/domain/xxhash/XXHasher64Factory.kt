package com.ustadmobile.core.domain.xxhash

interface XXHasher64Factory {

    fun newHasher(seed: Long): XXHasher64

}