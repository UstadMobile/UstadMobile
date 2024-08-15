package com.ustadmobile.core.domain.xapi.state

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toByteArray
import com.ustadmobile.door.DoorDatabaseCallbackStatementList
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.StateDeleteCommand
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.ktor.utils.io.core.toByteArray

/**
 * Delete Xapi State
 *
 * Xapi Delete requests can require the deletion of all state ids for a particular context (agent +
 * activityId). If the device is offline, and other state ids for the context exist, then this is a
 * problem.
 *
 * A trigger will fire when StateDeleteCommand is inserted or updated to action the delete command,
 * e.g. when a delete all state ids for context occurs offline, then the delete command will be
 * sent upstream when a connection is next available (as usual), and when it reaches the server the
 * trigger will delete any applicable StateEntity(s) on the upstream server.

 */
class DeleteXapiStateUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val xxStringHasher: XXStringHasher,
    private val xxHasher64Factory: XXHasher64Factory,
    private val learningSpace: LearningSpace,
) {

    class AddXapiStateAddTriggersCallback(): DoorDatabaseCallbackStatementList {
        override fun onCreate(db: DoorSqlDatabase): List<String> {
            return buildList {
                if(db.dbType() == DoorDbType.SQLITE) {
                    listOf("INSERT", "UPDATE").forEach { event ->
                        add("""
                            CREATE TRIGGER IF NOT EXISTS xapi_state_delete_trig_${event.lowercase().substring(0, 3)}
                            AFTER $event ON StateDeleteCommand
                            FOR EACH ROW
                            BEGIN
                            UPDATE StateEntity
                               SET seDeleted = 1,
                                   seLastMod = NEW.sdcLastMod
                             WHERE seActorUid = NEW.sdcActorUid
                               AND seActivityUid = NEW.sdcActivityUid
                               AND seLastMod < NEW.sdcLastMod
                               AND (    (      NEW.sdcRegistrationHi IS NULL 
                                           AND seRegistrationHi IS NULL
                                           AND NEW.sdcRegistrationLo IS NULL
                                           AND seRegistrationLo IS NULL)
                                     OR (     seRegistrationHi = NEW.sdcRegistrationHi
                                          AND seRegistrationLo = NEW.sdcRegistrationLo))
                               AND (    NEW.sdcStateId IS NULL
                                     OR seStateId = NEW.sdcStateId);
                            END         
                        """)
                    }
                }else {
                    add("""
                        CREATE OR REPLACE FUNCTION xapi_state_delete_fn() RETURNS TRIGGER AS ${'$'}${'$'}
                        BEGIN
                        UPDATE StateEntity
                           SET seDeleted = TRUE,
                               seLastMod = NEW.sdcLastMod
                         WHERE seActorUid = NEW.sdcActorUid
                           AND seActivityUid = NEW.sdcActivityUid
                           AND seLastMod < NEW.sdcLastMod
                           AND (    (      NEW.sdcRegistrationHi IS NULL 
                                       AND seRegistrationHi IS NULL
                                       AND NEW.sdcRegistrationLo IS NULL
                                       AND seRegistrationLo IS NULL)
                                 OR (     seRegistrationHi = NEW.sdcRegistrationHi
                                      AND seRegistrationLo = NEW.sdcRegistrationLo))
                           AND (    NEW.sdcStateId IS NULL
                                 OR seStateId = NEW.sdcStateId);
                         RETURN NEW;
                         END ${'$'}${'$'} LANGUAGE plpgsql
                    """)
                    add("""
                        CREATE TRIGGER xapi_state_delete_trig
                        AFTER INSERT OR UPDATE ON StateDeleteCommand
                        FOR EACH ROW
                        EXECUTE FUNCTION xapi_state_delete_fn();
                    """)
                }
            }
        }

        override fun onOpen(db: DoorSqlDatabase): List<String> {
            return emptyList()
        }
    }

    data class DeleteXapiStateRequest(
        val activityId: String,
        val agent: XapiAgent,
        val registration: String?,
        val stateId: String?,
    )

    fun DeleteXapiStateRequest.hash(): Long {
        val xxHasher64 = xxHasher64Factory.newHasher(0)
        xxHasher64.update(activityId.toByteArray())
        registration?.also {
            val uuid = uuidFrom(it)
            xxHasher64.update(uuid.mostSignificantBits.toByteArray())
            xxHasher64.update(uuid.leastSignificantBits.toByteArray())
        }

        stateId?.also {
            xxHasher64.update(it.toByteArray())
        }

        return xxHasher64.digest()
    }


    suspend operator fun invoke(
        request: DeleteXapiStateRequest,
        session: XapiSessionEntity,
    ) {
        val registrationUuid = request.registration?.let { uuidFrom(it) }

        val requestActorUid = request.agent.identifierHash(xxStringHasher)
        val sessionActorUid = session.agent(learningSpace).identifierHash(xxStringHasher)

        if(requestActorUid != sessionActorUid)
                throw HttpApiException(403, "Forbidden: Agent does not match session")

        (repo ?: db).stateDeleteCommandDao().insertAsync(
            StateDeleteCommand(
                sdcActorUid = request.agent.identifierHash(xxStringHasher),
                sdcHash = request.hash(),
                sdcActivityUid = xxStringHasher.hash(request.activityId),
                sdcStateId = request.stateId,
                sdcLastMod = systemTimeInMillis(),
                sdcRegistrationHi = registrationUuid?.mostSignificantBits,
                sdcRegistrationLo = registrationUuid?.leastSignificantBits,
            )
        )
    }

}