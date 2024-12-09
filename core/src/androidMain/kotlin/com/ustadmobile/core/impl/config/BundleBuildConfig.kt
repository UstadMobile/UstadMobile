package com.ustadmobile.core.impl.config

import android.os.Bundle

class BundleBuildConfig(
    private val bundle: Bundle?
): UstadBuildConfig {

    override fun get(key: String): String? {
        return bundle?.getString(key)
    }
}