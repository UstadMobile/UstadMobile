package com.ustadmobile.core.domain.xapi.session

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.lib.util.randomString
import io.github.aakira.napier.Napier

/**
 * Start or resume an Xapi session that will be recorded in the local database. Used on Android,
 * desktop, and server.
 */
class ResumeOrStartXapiSessionUseCaseLocal(
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
        val rootActivityUid = xxStringHasher.hash(activityId)

        val pendingSession = (activeRepo ?: activeDb).xapiSessionEntityDao()
            .findMostRecentSessionByActorAndActivity(
                accountPersonUid = accountPersonUid,
                actorUid = actorUid,
                xseRootActivityUid = rootActivityUid,
            )

        activeRepo?.also { repo ->
            //Load/validate state associated with this activity and actor
            try {
                repo.stateEntityDao().findByAgentAndActivity(
                    accountPersonUid = accountPersonUid,
                    actorUid = actorUid,
                    seActivityUid = rootActivityUid,
                    registrationUuidHi = pendingSession?.xseRegistrationHi,
                    registrationUuidLo = pendingSession?.xseRegistrationLo,
                    modifiedSince = 0
                )
            }catch(e: Throwable) {
                Napier.w("ResumeOrStartXapiSession: attempted to load state for actor/activity: failed", e)
            }
        }

        return if(pendingSession != null) {
            pendingSession
        }else {
            val registrationUuid = uuid4()

            XapiSessionEntity(
                xseUid = activeDb.doorPrimaryKeyManager.nextIdAsync(XapiSessionEntity.TABLE_ID),
                xseAccountPersonUid = accountPersonUid,
                xseAccountUsername = actor.account?.name ?: "anonymous",
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