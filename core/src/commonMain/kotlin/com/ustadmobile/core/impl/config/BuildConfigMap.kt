package com.ustadmobile.core.impl.config

class BuildConfigMap(private val delegate: Map<String, String>) : UstadBuildConfig{

    override fun get(key: String): String? {
        return delegate[key]
    }

}