package com.ustadmobile.core.domain.xapi.ext

import com.benasher44.uuid.Uuid
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiObjectType
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.ktor.util.encodeBase64
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun XapiSessionEntity.agent(
    learningSpace: LearningSpace,
) : XapiAgent {
    return XapiAgent(
        account = XapiAccount(
            homePage = learningSpace.url,
            name = xseAccountUsername,
        ),
        objectType = XapiObjectType.Agent
    )
}

val XapiSessionEntity.registrationUuid: Uuid
    get() = Uuid(xseRegistrationHi, xseRegistrationLo)

fun XapiSessionEntity.knownActorUidToPersonUidsMap(json: Json): Map<Long, Long> {
    return if(knownActorUidToPersonUids.isNotEmpty()) {
        json.decodeFromString(
            MapSerializer(Long.serializer(), Long.serializer()),
            knownActorUidToPersonUids
        )
    }else {
        emptyMap()
    }
}

fun XapiSessionEntity.authorizationHeader() : String {
    return "Basic " + "${xseUid}:$xseAuth".encodeBase64()
}

