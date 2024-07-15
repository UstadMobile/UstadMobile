package com.ustadmobile.core.domain.xxhash

import net.jpountz.xxhash.XXHashFactory

class XXHasher64FactoryCommonJvm(): XXHasher64Factory {

    private val factory = XXHashFactory.fastestJavaInstance()

    override fun newHasher(seed: Long): XXHasher64 {
        return XXHasher64CommonJvm(factory.newStreamingHash64(seed))
    }
}