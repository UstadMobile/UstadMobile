package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

fun XapiSession.toXapiSessionEntity(
    xseUid: Long,
    registrationUuid: Uuid,
    auth: String,
): XapiSessionEntity {
    return XapiSessionEntity(
        xseUid = xseUid,
        xseLastMod = systemTimeInMillis(),
        xseUsUid = userSessionUid,
        xseAccountPersonUid = accountPersonUid,
        xseAccountUsername = accountUsername,
        xseClazzUid = clazzUid,
        xseCbUid = cbUid,
        xseStartTime = systemTimeInMillis(),
        xseRegistrationHi = registrationUuid.mostSignificantBits,
        xseRegistrationLo = registrationUuid.leastSignificantBits,
        xseContentEntryUid = contentEntryUid,
        xseRootActivityId = rootActivityId,
        xseAuth = auth,
    )
}

fun XapiSessionEntity.toXapiSession(
    endpoint: Endpoint
): XapiSession {
    return XapiSession(
        endpoint = endpoint,
        accountPersonUid = xseAccountPersonUid,
        accountUsername = xseAccountUsername ?: throw IllegalArgumentException("xse account username required"),
        clazzUid = xseClazzUid,
        userSessionUid = xseUsUid,
        cbUid = xseCbUid,
        contentEntryUid = xseContentEntryUid,
        registrationUuid = Uuid(xseRegistrationHi, xseRegistrationLo).toString(),
        rootActivityId = xseRootActivityId,
    )
}
