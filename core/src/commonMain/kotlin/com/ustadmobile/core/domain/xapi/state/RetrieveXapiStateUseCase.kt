package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class RetrieveXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
) {

    data class RetrieveXapiStateResult(
        val doc: JsonObject,
    )

    suspend operator fun invoke(
        xapiSession: XapiSession,
        xapiStateParams: XapiStateParams,
    ): RetrieveXapiStateResult? {
        val xapiAgent = try {
            json.decodeFromString(XapiAgent.serializer(), xapiStateParams.agent)
        }catch(e: Throwable) {
            throw HttpApiException(400, "Agent is not valid json: ${e.message}", e)
        }

        val stateEntities = db.stateEntityDao().getByParams(
            accountPersonUid = xapiSession.accountPersonUid,
            agentActorUid = xapiAgent.identifierHash(xxStringHasher),
            activityUid = xxStringHasher.hash(xapiStateParams.activityId),
            registrationIdHi = xapiStateParams.registrationUuid?.mostSignificantBits,
            registrationIdLo = xapiStateParams.registrationUuid?.leastSignificantBits,
            stateId = xapiStateParams.stateId
        )
        if(stateEntities.isEmpty())
            return null

        val jsonObject = buildJsonObject {
            stateEntities.forEach {
                val key = it.seKey ?: throw IllegalArgumentException("no key")
                val contentJson = it.seContent ?: throw IllegalArgumentException("no content")
                put(key, json.decodeFromString(JsonElement.serializer(), contentJson))
            }
        }

        return RetrieveXapiStateResult(doc = jsonObject)
    }

}