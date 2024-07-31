package com.ustadmobile.core.domain.xapi.state

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher

class ListXapiStateIdsUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val xxStringHasher: XXStringHasher,
) {

    data class ListXapiStateIdsRequest(
        val activityId: String,
        val agent: XapiAgent,
        val registration: String? = null,
        val since: Long = 0,
    )

    data class ListXapiStateIdsResponse(
        val stateIds: List<String>,
        val lastModified: Long,
    )

    suspend operator fun invoke(
        request: ListXapiStateIdsRequest,
        xapiSession: XapiSession,
    ): ListXapiStateIdsResponse {
        val registrationUuid = request.registration?.let { uuidFrom(it) }
        
        val stateIdsAndLastMod =  (repo ?: db).stateEntityDao().getStateIds(
            accountPersonUid = xapiSession.accountPersonUid,
            actorUid = request.agent.identifierHash(xxStringHasher),
            seActivityUid = xxStringHasher.hash(request.activityId),
            modifiedSince = request.since,
            registrationUuidHi = registrationUuid?.mostSignificantBits,
            registrationUuidLo = registrationUuid?.leastSignificantBits,
        )

        return ListXapiStateIdsResponse(
            stateIds = stateIdsAndLastMod.map { it.seStateId },
            lastModified = stateIdsAndLastMod.takeIf { it.isNotEmpty() }?.maxOf { it.seLastMod } ?: 0,
        )
    }

}