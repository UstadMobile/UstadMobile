package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.util.randomUuid
import kotlin.random.Random

/**
 * Gets an already existing NodeIdAndAuth for the given context (e.g. virtual host / endpoint), or
 * if none has been created yet, it will generate one using a random int for the id and a random
 * uid for the auth string.
 *
 * @param contextPrefix a prefix used for appPref keys e.g. the sanitized hostname
 */
fun UstadMobileSystemImpl.getOrGenerateNodeIdAndAuth(contextPrefix: String, context: Any): NodeIdAndAuth {
    val nodeId: String = getOrPutAppPref("${contextPrefix}_nodeId", context) {
        Random.nextLong(0, Long.MAX_VALUE).toString()
    }

    val nodeAuth: String = getOrPutAppPref("${contextPrefix}_nodeAuth", context) {
        randomUuid().toString()
    }

    return NodeIdAndAuth(nodeId.toLong(), nodeAuth)
}