package com.ustadmobile.core.impl.config

import com.jcabi.manifests.Manifests
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of AppConfig that reads values from the Jar manifest. The use of . is not valid
 * for keys, so any '.' is replaced with '-'
 */
class ManifestAppConfig : AppConfig{

    private val valueCache = ConcurrentHashMap<String, String?>()

    override fun get(key: String): String? {
        return valueCache.getOrPut(key) {
            Manifests.read(key.replace(".", "-"))
        }
    }
}