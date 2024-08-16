package com.ustadmobile.core.domain.xxhash

import com.ustadmobile.core.wrappers.xxhash.XXH

class XXHasher64FactoryJs: XXHasher64Factory {
    override fun newHasher(seed: Long): XXHasher64 {
        val hasher = XXH.h64()
        hasher.init(seed)
        return XXHasher64Js(hasher)
    }
}
