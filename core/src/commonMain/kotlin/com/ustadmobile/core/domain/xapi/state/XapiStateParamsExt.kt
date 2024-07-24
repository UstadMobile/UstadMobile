package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.domain.xxhash.XXHasher64
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toByteArray
import io.ktor.utils.io.core.toByteArray

/**
 * Hash the Xapi State resource parameters that are part of the identifier (except the agent)
 * to generate the value for StateEntity.seHash
 */
fun XapiStateParams.hash(xxHasher64: XXHasher64): Long {
    xxHasher64.update(activityId.toByteArray())
    registrationUuid?.also {
        xxHasher64.update(it.mostSignificantBits.toByteArray())
        xxHasher64.update(it.leastSignificantBits.toByteArray())
    }

    xxHasher64.update(stateId.toByteArray())
    return xxHasher64.digest()
}

fun XapiStateParams.activityUid(stringHasher: XXStringHasher): Long {
    return stringHasher.hash(activityId)
}
