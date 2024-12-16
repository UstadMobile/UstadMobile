package com.ustadmobile.xxhashkmp.jsimpl

import com.ustadmobile.xxhashkmp.wrappers.XXH
import com.ustadmobile.xxhashkmp.XXHasher64
import com.ustadmobile.xxhashkmp.XXHasher64Factory

class XXHasher64FactoryJs: XXHasher64Factory {
    override fun newHasher(seed: Long): XXHasher64 {
        val hasher = XXH.h64()
        hasher.init(seed)
        return XXHasher64Js(hasher)
    }
}
