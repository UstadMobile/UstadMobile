package com.ustadmobile.core.util.ext

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import kotlin.random.Random

fun Settings.getStringOrSet(
    key: String,
    block: () -> String,
): String {
    return getStringOrNull(key) ?: block().also {
        set(key, it)
    }
}

/**
 * Gets an already existing NodeIdAndAuth for the given context (e.g. virtual host / endpoint), or
 * if none has been created yet, it will generate one using a random int for the id and a random
 * uid for the auth string.
 *
 * @param contextPrefix a prefix used for appPref keys e.g. the sanitized hostname
 */
fun Settings.getOrGenerateNodeIdAndAuth(
    contextPrefix: String
) : NodeIdAndAuth {
    val nodeId = getStringOrSet("${contextPrefix}_nodeId") {
        Random.nextLong(0, Long.MAX_VALUE).toString()
    }

    val nodeAuth = getStringOrSet("${contextPrefix}_nodeAuth") {
        randomUuid().toString()
    }

    return NodeIdAndAuth(nodeId.toLong(), nodeAuth)
}
