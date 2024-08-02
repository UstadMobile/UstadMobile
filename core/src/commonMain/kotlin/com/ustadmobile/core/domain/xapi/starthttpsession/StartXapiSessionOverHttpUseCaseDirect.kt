package com.ustadmobile.core.domain.xapi.starthttpsession

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.ext.insertOrUpdateActorsIfNameChanged
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xapi.model.toActorEntity
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase.StartXapiSessionOverHttpResult
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.lib.util.randomString
import io.ktor.util.encodeBase64

/**
 * Start a Http Xapi session that will be recorded in the local database. Used on Android, desktop,
 * and server.
 */
class StartXapiSessionOverHttpUseCaseDirect(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val xxStringHasher: XXStringHasher,
    private val endpoint: Endpoint,
): StartXapiSessionOverHttpUseCase {

    override suspend fun invoke(
        xapiSession: XapiSessionEntity,
    ): StartXapiSessionOverHttpResult {
        val xseUid = db.doorPrimaryKeyManager.nextIdAsync(XapiSessionEntity.TABLE_ID)
        val auth = randomString(16)

        (repo ?: db).xapiSessionEntityDao().insertAsync(
            xapiSession
        )

        //Ensure that the actor entity is stored and linked to the personuid so that storing and
        //retrieving state works as expected.
        val agent = xapiSession.agent(endpoint)

        val actorEntity = agent.toActorEntity(
            xxHasher = xxStringHasher,
            knownActorUidToPersonUidMap = mapOf(
                agent.identifierHash(xxStringHasher) to xapiSession.xseAccountPersonUid
            )
        )

        (repo ?: db).actorDao().insertOrUpdateActorsIfNameChanged(listOf(actorEntity))

        return StartXapiSessionOverHttpResult(
            auth = "Basic " + "${xseUid}:$auth".encodeBase64(),
            httpUrl = getApiUrlUseCase("/api/xapi/")
        )
    }
}