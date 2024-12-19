package com.ustadmobile.core.impl.config

import com.jcabi.manifests.Manifests
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementation of AppConfig that reads values from the Jar manifest. The use of . is not valid
 * for keys, so any '.' is replaced with '-'
 */
class ManifestAppConfig : UstadBuildConfig{

    private val valueCache = ConcurrentHashMap<String, String?>()

    private val nullKeys = CopyOnWriteArrayList<String>()

    override fun get(key: String): String? {
        if(key in nullKeys)
            return null

        val loadedVal = valueCache[key]
        if(loadedVal != null)
            return loadedVal

        return try {
            Manifests.read(key.replace(".", "-")).also {
                valueCache[key] = it
            }
        }catch(e: Throwable) {
            nullKeys.add(key)
            null
        }
    }
}