package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.lib.db.entities.xapi.StateEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

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

    /**
     * @param method as per the Xapi spec if this is a POST request and JSON content, then we will
     *        merge with the previous doc.
     */
    suspend operator fun invoke(
        xapiSession: XapiSession,
        xapiStateParams: XapiStateParams,
        method: IHttpRequest.Companion.Method,
        contentType: String,
        stateBody: String,
    ) {
        val hasher = xxHasher64Factory.newHasher(0)
        val seHash = xapiStateParams.hash(hasher)
        //As per the Xapi Spec this MUST be an Agent (Communications - section 2.3)
        val xapiAgent = try {
            json.decodeFromString(XapiAgent.serializer(), xapiStateParams.agent)
        }catch(e: Throwable) {
            throw HttpApiException(400, "Agent is not valid: ${e.message}", e)
        }

        val agentActorUid = xapiAgent.identifierHash(xxStringHasher)

        val merge = method == IHttpRequest.Companion.Method.POST && contentType == "application/json"
        val content = if(merge) {
            val existingState = db.stateEntityDao().getByParams(
                accountPersonUid = xapiSession.accountPersonUid,
                agentActorUid = agentActorUid,
                seHash = seHash
            )
            when {
                existingState == null -> stateBody

                /*
                 * As per the spec, if the content type is application/json and there is an existing
                 * state that has any other content type, we MUST respond with a 400 bad request error
                 */
                existingState.seContentType != "application/json" -> {
                    throw HttpApiException(400, "POST application/json state for merge: existing state is not application/json")
                }

                else -> {
                    val existingStateJsonDoc = try {
                        json.decodeFromString(JsonObject.serializer(), existingState.seContent!!)
                    }catch(e: Throwable) {
                        throw HttpApiException(400, "Existing state is not valid json: ${e.message}", e)
                    }

                    val newStateJsonDoc = try {
                        json.decodeFromString(JsonObject.serializer(), stateBody)
                    }catch(e: Throwable) {
                        throw HttpApiException(400, "New state is not valid json: ${e.message}", e)
                    }

                    val mergedDoc = buildJsonObject {
                        existingStateJsonDoc.forEach {
                            put(it.key, it.value)
                        }

                        newStateJsonDoc.forEach {
                            put(it.key, it.value)
                        }
                    }

                    json.encodeToString(JsonObject.serializer(), mergedDoc)
                }
            }

        }else {
            //If the contentType is application/json then we should check it as per the spec
            // https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#requirements-14
            if(contentType == "application/json") {
                try {
                    json.decodeFromString(JsonObject.serializer(), stateBody)
                }catch(e: Throwable) {
                    throw HttpApiException(400, "Content-type is application/json, but not valid json: ${e.message}", e)
                }
            }

            stateBody
        }

        if(xapiSession.agent.identifierHash(xxStringHasher) !=
            xapiAgent.identifierHash(xxStringHasher)
        ) {
            throw HttpApiException(403, "Forbidden: agent does not match with session")
        }

        val activityUid = xapiStateParams.activityUid(xxStringHasher)
        val registrationUuid = xapiStateParams.registrationUuid

        val lastModTime = systemTimeInMillis()

        val stateEntity = StateEntity(
            seActorUid = agentActorUid,
            seHash = seHash,
            seActivityUid = activityUid,
            seStateId = xapiStateParams.stateId,
            seContentType = contentType,
            seLastMod = lastModTime,
            seContent = content,
            seDeleted = false,
            seRegistrationHi = registrationUuid?.mostSignificantBits,
            seRegistrationLo = registrationUuid?.leastSignificantBits
        )

        (repo ?: db).stateEntityDao().upsertAsync(listOf(stateEntity))
    }

}