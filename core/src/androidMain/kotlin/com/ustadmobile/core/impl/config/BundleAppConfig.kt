package com.ustadmobile.core.impl.config

import android.os.Bundle

class BundleAppConfig(
    private val bundle: Bundle?
): AppConfig {

    override fun get(key: String): String? {
        return bundle?.getString(key)
    }
}