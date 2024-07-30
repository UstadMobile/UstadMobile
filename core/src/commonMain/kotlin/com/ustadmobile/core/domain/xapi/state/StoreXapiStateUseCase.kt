package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.ext.requireBodyAsBytes
import com.ustadmobile.core.util.ext.requireBodyAsText
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.lib.db.entities.xapi.StateEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * Store/update xAPI state (e.g. via PUT or POST request)
 */
class StoreXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    xapiJson: XapiJson,
    private val xxHasher64Factory: XXHasher64Factory,
    private val xxStringHasher: XXStringHasher,
) {

    private val json = xapiJson.json

    /**
     * @param method as per the Xapi spec if this is a POST request and JSON content, then we will
     *        merge with the previous doc.
     */
    suspend operator fun invoke(
        xapiSession: XapiSession,
        xapiStateParams: XapiStateParams,
        method: IHttpRequest.Companion.Method,
        contentType: String,
        request: IHttpRequest,
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

        /*
         * As per the xAPI spec when using POST and content-type is application/json, we MUST merge
         * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#json-procedure-with-requirements
         */
        val content = if(
            method == IHttpRequest.Companion.Method.POST && contentType == "application/json"
        ) {
            val requestBody = request.requireBodyAsText()

            val existingState = db.stateEntityDao().getByParams(
                accountPersonUid = xapiSession.accountPersonUid,
                agentActorUid = agentActorUid,
                seHash = seHash
            )
            when {
                existingState == null -> requestBody

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
                        json.decodeFromString(JsonObject.serializer(), requestBody)
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

                    json.encodeToString(JsonObject.serializer(), mergedDoc).also {
                        if(it.length > MAX_STATE_SIZE)
                            throw HttpApiException(413, "State content too large: ${it.length} exceeds limit of $MAX_STATE_SIZE")
                    }
                }
            }

        }else {
            //If the contentType is application/json then we should check it as per the spec
            // https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#requirements-14

            if(
                contentType == "application/json" || contentType.startsWith("text/")
            ) {
                val requestBodyText = request.requireBodyAsText()

                if(contentType == "application/json") {
                    try {
                        json.decodeFromString(JsonObject.serializer(), requestBodyText)
                    }catch(e: Throwable) {
                        throw HttpApiException(400, "Content-type is application/json, but not valid json: ${e.message}", e)
                    }
                }

                if(requestBodyText.length > MAX_STATE_SIZE) {
                    throw HttpApiException(413, "State content too large: ${requestBodyText.length} exceeds limit of $MAX_STATE_SIZE")
                }

                requestBodyText
            }else {
                request.requireBodyAsBytes().also {
                    if(it.size > MAX_STATE_SIZE)
                        throw HttpApiException(413, "State content too large: ${it.size} exceeds limit of $MAX_STATE_SIZE")
                }.encodeBase64()
            }
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

    companion object {

        /**
         * The suspend data limits are pretty small: SCORM 1.2 had a 4KB limit, 64KB limit for
         * SCORM 2004. Because the suspend data is expected to be small, we don't use blob
         * storage, we just save the string on the entity (base64 encoded for binary data).
         *
         * See
         * https://access.articulate.com/support/article/exceeding-scorm-suspend-data-limits
         */
        const val MAX_STATE_SIZE = 256 * 1024

    }

}