package com.ustadmobile.core.domain.xapi.state.h5puserdata

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xapi.state.XapiStateParams
import com.ustadmobile.core.domain.xapi.state.activityUid
import com.ustadmobile.core.domain.xapi.state.hash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequestWithFormUrlEncodedData
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.ihttp.response.StringResponse
import com.ustadmobile.lib.db.entities.xapi.StateEntity
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

class H5PUserDataEndpointUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val xxStringHasher: XXStringHasher,
    private val xxHasher64Factory: XXHasher64Factory,
    xapiJson: XapiJson,
) {

    private val json = xapiJson.json

    suspend operator fun invoke(
        request: IHttpRequest,
        xapiStateParams: XapiStateParams,
        xapiSessionEntity: XapiSessionEntity,
    ) : IHttpResponse {
        val hasher = xxHasher64Factory.newHasher(0)
        val subContentId = request.queryParam("subContentId")
        val seHash = xapiStateParams.hash(hasher, h5pSubContentId = subContentId)

        //As per the Xapi Spec this MUST be an Agent (Communications - section 2.3)
        val xapiAgent = try {
            json.decodeFromString(XapiAgent.serializer(), xapiStateParams.agent)
        }catch(e: Throwable) {
            throw HttpApiException(400, "Agent is not valid: ${e.message}", e)
        }

        val agentActorUid = xapiAgent.identifierHash(xxStringHasher)
        val registrationUuid = xapiStateParams.registrationUuid

        val dataType = request.queryParam("dataType")

        return if(request.method == IHttpRequest.Companion.Method.POST) {
            val requestData = (request as IHttpRequestWithFormUrlEncodedData).bodyAsFormUrlEncodedDataMap()

            val stateEntity = StateEntity(
                seActorUid = agentActorUid,
                seHash = seHash,
                seActivityUid = xapiStateParams.activityUid(xxStringHasher),
                seStateId = xapiStateParams.stateId,
                seContentType = "application/json",
                seLastMod = systemTimeInMillis(),
                seContent = requestData["data"]?.firstOrNull(),
                seDeleted = false,
                seRegistrationHi = registrationUuid?.mostSignificantBits,
                seRegistrationLo = registrationUuid?.leastSignificantBits,
                seH5PPreloaded = (requestData["preload"]?.firstOrNull()?.toIntOrNull() ?: 0) != 0,
                seH5PSubContentId = subContentId,
            )

            (repo ?: db).stateEntityDao().upsertAsync(listOf(stateEntity))

            StringResponse(
                request = request,
                mimeType = "text/plain",
                responseCode = 200,
                body = ""
            )
        }else {
            /*
             * Handles by the JQuery .ajax success parameter (where dataType for .ajax has been set
             * to "json") as per
             * https://github.com/h5p/h5p-php-library/blob/4599291d7ce0cfb90edd188b181416f31514748e/js/h5p.js#L2361
             *
             * as per the jquery datatype docs, we will return null if we don't have anything
             */
            StringResponse(
                request = request,
                mimeType = "application/json",
                responseCode = 200,
                body = "null"
            )
        }
    }

}