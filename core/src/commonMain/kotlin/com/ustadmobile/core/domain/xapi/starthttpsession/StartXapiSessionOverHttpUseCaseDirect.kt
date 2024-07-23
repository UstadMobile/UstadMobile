package com.ustadmobile.core.domain.xapi.starthttpsession

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase.StartXapiSessionOverHttpResult
import com.ustadmobile.core.domain.xapi.toXapiSessionEntity
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
): StartXapiSessionOverHttpUseCase {

    override suspend fun invoke(
        xapiSession: XapiSession,
    ): StartXapiSessionOverHttpResult {
        val registrationUuid = uuid4()
        val xseUid = db.doorPrimaryKeyManager.nextIdAsync(XapiSessionEntity.TABLE_ID)
        val auth = randomString(16)

        (repo ?: db).xapiSessionEntityDao().insertAsync(
            xapiSession.toXapiSessionEntity(xseUid, registrationUuid, auth)
        )

        return StartXapiSessionOverHttpResult(
            auth = "Basic " + "${xseUid}:$auth".encodeBase64(),
            httpUrl = getApiUrlUseCase("/api/xapi/")
        )
    }
}