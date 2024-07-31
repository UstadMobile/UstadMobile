package com.ustadmobile.core.domain.xapi.state

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.base64StringToByteArray

class RetrieveXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    xapiJson: XapiJson,
    private val xxStringHasher: XXStringHasher,
    private val xxHasher64Factory: XXHasher64Factory,
) {

    private val json = xapiJson.json

    sealed interface RetrieveXapiStateResult {
        val lastModified: Long
        val contentType: String
    }

    data class TextRetrieveXapiStateResult(
        val content: String,
        override val lastModified: Long,
        override val contentType: String,
    ): RetrieveXapiStateResult

    data class ByteRetrieveXapiStateResult(
        val content: ByteArray,
        override val lastModified: Long,
        override val contentType: String,
    ): RetrieveXapiStateResult {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ByteRetrieveXapiStateResult) return false

            if (!content.contentEquals(other.content)) return false
            if (lastModified != other.lastModified) return false
            if (contentType != other.contentType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = content.contentHashCode()
            result = 31 * result + lastModified.hashCode()
            result = 31 * result + contentType.hashCode()
            return result
        }
    }

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

        val stateEntity = db.stateEntityDao().findByActorAndHash(
            accountPersonUid = xapiSession.accountPersonUid,
            actorUid = xapiAgent.identifierHash(xxStringHasher),
            seHash = hash,
            includeDeleted = false,
        )

        return when {
            stateEntity == null -> null

            stateEntity.seContentType?.let {
                it.startsWith("text/") || it == "application/json"
            } == true -> {
                TextRetrieveXapiStateResult(
                    content = stateEntity.seContent!!,
                    lastModified = stateEntity.seLastMod,
                    contentType = stateEntity.seContentType!!
                )
            }

            else -> {
                ByteRetrieveXapiStateResult(
                    content = stateEntity.seContent!!.base64StringToByteArray(),
                    lastModified = stateEntity.seLastMod,
                    contentType = stateEntity.seContentType!!
                )
            }
        }
    }

}