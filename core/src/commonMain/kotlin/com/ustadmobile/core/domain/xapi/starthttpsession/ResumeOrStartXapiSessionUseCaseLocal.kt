package com.ustadmobile.core.domain.xapi.starthttpsession

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.lib.util.randomString

/**
 * Start or resume an Xapi session that will be recorded in the local database. Used on Android,
 * desktop, and server.
 */
class ResumeOrStartXapiSessionUseCaseLocal(
    private val accountManager: UstadAccountManager,
    private val activeDb: UmAppDatabase,
    private val activeRepo: UmAppDatabase?,
    private val xxStringHasher: XXStringHasher,
): ResumeOrStartXapiSessionUseCase {

    override suspend fun invoke(
        accountPersonUid: Long,
        actor: XapiActor,
        activityId: String,
        clazzUid: Long,
        cbUid: Long,
        contentEntryUid: Long
    ): XapiSessionEntity {
        val actorUid = actor.identifierHash(xxStringHasher)

        val pendingSession = (activeRepo ?: activeDb).xapiSessionEntityDao()
            .findPendingSessionByActorAndActivityUid(
                xseActorUid = actorUid,
                xseRootActivityUid = xxStringHasher.hash(activityId),
                requireNotCompleted = true
            )

        return if(pendingSession != null) {
            pendingSession
        }else {
            val registrationUuid = uuid4()

            XapiSessionEntity(
                xseUid = activeDb.doorPrimaryKeyManager.nextIdAsync(XapiSessionEntity.TABLE_ID),
                xseAccountPersonUid = accountManager.currentUserSession.userSession.usPersonUid,
                xseAccountUsername = accountManager.currentUserSession.person.username ?: "anonymous",
                xseActorUid = actorUid,
                xseClazzUid = clazzUid,
                xseCbUid = cbUid,
                xseContentEntryUid = contentEntryUid,
                xseRootActivityId = activityId,
                xseRootActivityUid = xxStringHasher.hash(activityId),
                xseRegistrationHi = registrationUuid.mostSignificantBits,
                xseRegistrationLo = registrationUuid.leastSignificantBits,
                xseAuth = randomString(16),
            ).also {
                (activeRepo ?: activeDb).xapiSessionEntityDao().insertAsync(it)
            }
        }
    }

}