package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import kotlinx.serialization.json.Json

class RetrieveXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
    private val xxHasher64Factory: XXHasher64Factory,
) {

    data class RetrieveXapiStateResult(
        val content: String,
        val lastModified: Long,
        val contentType: String,
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

        val hash = xapiStateParams.hash(xxHasher64Factory.newHasher(0L))

        val stateEntity = db.stateEntityDao().getByParams(
            accountPersonUid = xapiSession.accountPersonUid,
            agentActorUid = xapiAgent.identifierHash(xxStringHasher),
            seHash = hash,
        )

        return stateEntity?.let {
            RetrieveXapiStateResult(
                content = it.seContent!!,
                lastModified = it.seLastMod,
                contentType = it.seContentType!!
            )
        }
    }

}