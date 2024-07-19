package com.ustadmobile.core.domain.xapi.starthttpsession

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase.StartXapiSessionOverHttpResult
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.lib.util.randomString

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
            XapiSessionEntity(
                xseUid = xseUid,
                xseLastMod = systemTimeInMillis(),
                xseUsUid = xapiSession.userSessionUid,
                xseAccountPersonUid = xapiSession.accountPersonUid,
                xseAccountUsername = xapiSession.accountUsername,
                xseClazzUid = xapiSession.clazzUid,
                xseCbUid = xapiSession.cbUid,
                xseStartTime = systemTimeInMillis(),
                xseRegistrationHi = registrationUuid.mostSignificantBits,
                xseRegistrationLo = registrationUuid.leastSignificantBits,
                xseContentEntryUid = xapiSession.contentEntryUid,
                xseRootActivityId = xapiSession.rootActivityId,
                xseAuth = auth,
            )
        )

        return StartXapiSessionOverHttpResult(
            basicAuth = auth,
            httpUrl = getApiUrlUseCase("/api/xapi/")
        )
    }
}