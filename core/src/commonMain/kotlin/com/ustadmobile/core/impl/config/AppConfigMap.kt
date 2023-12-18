package com.ustadmobile.core.impl.config

class AppConfigMap(private val delegate: Map<String, String>) : AppConfig{

    override fun get(key: String): String? {
        return delegate[key]
    }

}