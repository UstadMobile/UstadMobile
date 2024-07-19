package com.ustadmobile.core.domain.xapi.starthttpsession

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.lib.util.randomString
import io.ktor.util.encodeBase64

/**
 * Start a Http Xapi session that will be recorded in the local database. Used on Android, desktop,
 * and server.
 */
class StartXapiHttpSessionUseCaseLocal(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val endpoint: Endpoint,
): StartXapiHttpSessionUseCase {

    override suspend fun invoke(
        userSession: UserSession,
        accountUsername: String,
        clazzUid: Long,
        cbUid: Long,
        contentEntryUid: Long,
        rootActivityId: String?
    ): XapiSession {
        val registrationUuid = uuid4()
        val xseUid = db.doorPrimaryKeyManager.nextIdAsync(XapiSessionEntity.TABLE_ID)
        val auth = randomString(16)

        (repo ?: db).xapiSessionEntityDao().insertAsync(
            XapiSessionEntity(
                xseUid = xseUid,
                xseLastMod = systemTimeInMillis(),
                xseUsUid = userSession.usUid,
                xseAccountPersonUid = userSession.usPersonUid,
                xseAccountUsername = accountUsername,
                xseClazzUid = clazzUid,
                xseCbUid = cbUid,
                xseContentEntryUid = contentEntryUid,
                xseRootActivityId = rootActivityId,
                xseStartTime = systemTimeInMillis(),
                xseRegistrationHi = registrationUuid.mostSignificantBits,
                xseRegistrationLo = registrationUuid.leastSignificantBits,
                xseAuth = auth,
            )
        )

        return XapiSession(
            endpoint = endpoint,
            accountPersonUid = userSession.usPersonUid,
            accountUsername = accountUsername,
            clazzUid = clazzUid,
            cbUid = cbUid,
            contentEntryUid = contentEntryUid,
            auth = "Basic ${"$xseUid:$auth".encodeBase64()}",
            registrationUuid = uuid4().toString(),
        )
    }
}