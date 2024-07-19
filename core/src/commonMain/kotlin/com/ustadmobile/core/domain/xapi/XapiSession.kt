package com.ustadmobile.core.domain.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiObjectType
import kotlinx.serialization.Serializable

/**
 * Represents an xAPI session e.g. where a given content entry is opened for a given user. Used where
 * an xAPI session happens within the app itself (e.g. not involving external content).
 *
 * @param endpoint The system endpoint (NOT the local Xapi Actor.actorUid to their corresponding
 * person UIDs e.g. where used where groups are involved
 * @param knownActorUidToPersonUidMap a map of known ActorEntity.actorUids and their corresponding
 *        personUids - useful where group actors are used.
 *
 */
@Serializable
data class XapiSession(
    val endpoint: Endpoint,
    val accountPersonUid: Long,
    val accountUsername: String,
    val clazzUid: Long,
    val cbUid: Long = 0,
    val contentEntryUid: Long = 0,
    val rootActivityId: String? = null,
    val registrationUuid: String? = null,
    val knownActorUidToPersonUidMap: Map<Long, Long> = emptyMap(),
    val auth: String? = null,
) {

    val agent: XapiAgent
        get() = XapiAgent(
            account = XapiAccount(
                homePage = endpoint.url,
                name = accountUsername,
            ),
            objectType = XapiObjectType.Agent
        )

}

