package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.StateEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Store/update xAPI state (e.g. via PUT or POST request)
 */
class StoreXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val json: Json,
    private val xxHasher64Factory: XXHasher64Factory,
    private val xxStringHasher: XXStringHasher,
) {

    suspend operator fun invoke(
        xapiSession: XapiSession,
        xapiStateParams: XapiStateParams,
        stateBody: String,
    ) {
        val jsonObject = try {
            json.decodeFromString(JsonObject.serializer(), stateBody)
        }catch(e: Throwable) {
            throw HttpApiException(400, "Body is not valid json: ${e.message}", e)
        }

        //As per the Xapi Spec this MUST be an Agent (Communications - section 2.3)
        val xapiAgent = try {
            json.decodeFromString(XapiAgent.serializer(), xapiStateParams.agent)
        }catch(e: Throwable) {
            throw HttpApiException(400, "Agent is not valid json: ${e.message}", e)
        }

        val agentActorUid = xapiAgent.identifierHash(xxStringHasher)

        if(xapiSession.agent.identifierHash(xxStringHasher) !=
            xapiAgent.identifierHash(xxStringHasher)
        ) {
            throw HttpApiException(403, "Unauthorized: agent does not match with session")
        }

        val activityUid = xapiStateParams.activityUid(xxStringHasher)
        val registrationUuid = xapiStateParams.registrationUuid

        val hasher = xxHasher64Factory.newHasher(0)
        val seHash = xapiStateParams.hash(hasher)
        val lastModTime = systemTimeInMillis()

        val stateEntities = jsonObject.entries.map {
            StateEntity(
                seActorUid = agentActorUid,
                seHash = seHash,
                seKey = it.key,
                seKeyHash = xxStringHasher.hash(it.key),
                seActivityUid = activityUid,
                seStateId = xapiStateParams.stateId,
                seLastMod = lastModTime,
                seContent = json.encodeToString(JsonElement.serializer(), it.value),
                seDeleted = false,
                seRegistrationHi = registrationUuid?.mostSignificantBits,
                seRegistrationLo = registrationUuid?.leastSignificantBits
            )
        }

        (repo ?: db).stateEntityDao().upsertAsync(stateEntities)
    }

}