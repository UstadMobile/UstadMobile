package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.ext.insertOrUpdateActorsIfNameChanged
import com.ustadmobile.core.domain.xapi.ext.insertOrUpdateIfLastModChanged
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xapi.model.toEntities
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 *
 */
class XapiStatementResource(
    private val db: UmAppDatabase,
    repo: UmAppDatabase?,
    private val xxHasher: XXStringHasher,
    private val endpoint: Endpoint,
    private val json: Json,
    private val hasherFactory: XXHasher64Factory,
    private val storeActivitiesUseCase: StoreActivitiesUseCase,
) {

    private val repoOrDb = repo ?: db

    @Suppress("unused") //Some here reserved for future use
    enum class Format {
        EXACT, IDS, CANONICAL
    }

    data class StatementStoreResult(
        val statementUuids: List<Uuid>,
    )

    private suspend fun storeStatements(
        statements: List<XapiStatement>,
        xapiSession: XapiSession,
    ): StatementStoreResult {
        val sessionActorUid = xapiSession.agent.identifierHash(xxHasher)

        //Ensure the known actor uid to person uid map has the person uid for the session actor uid
        val effectiveSession = if(
            !xapiSession.knownActorUidToPersonUidMap.containsKey(sessionActorUid)
        ) {
            xapiSession.copy(
                knownActorUidToPersonUidMap = xapiSession.knownActorUidToPersonUidMap + (sessionActorUid to xapiSession.accountPersonUid)
            )
        }else {
            xapiSession
        }

        val statementEntities = statements.flatMap { stmt ->
            val timeNowStr = Clock.System.now().toString()

            //Set properties to be set by LRS as per
            // https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#24-statement-properties
            // https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#231-statement-immutability
            val exactStatement = stmt.copy(
                stored = timeNowStr,
                timestamp = stmt.timestamp ?: timeNowStr,
                id = xapiRequireValidUuidOrNullAsString(stmt.id) ?: randomUuidAsString(),
                authority = XapiAgent(
                    account = XapiAccount(
                        name = effectiveSession.accountUsername,
                        homePage = effectiveSession.endpoint.url,
                    )
                )
            )

            exactStatement.toEntities(
                stringHasher = xxHasher,
                xapiSession = effectiveSession,
                exactJson = json.encodeToString(XapiStatement.serializer(), exactStatement),
                primaryKeyManager = db.doorPrimaryKeyManager,
                hasherFactory = hasherFactory,
                json = json,
                isSubStatement = false,
            )
        }

        repoOrDb.withDoorTransactionAsync {
            repoOrDb.statementDao().insertOrIgnoreListAsync(
                statementEntities.mapNotNull { it.statementEntity }
            )

            repoOrDb.statementEntityJsonDao().insertOrIgnoreListAsync(
                statementEntities.mapNotNull { it.statementEntityJson }
            )

            val actorEntities = statementEntities.flatMap { it.actorEntities ?: emptyList() }
            actorEntities.map { it.actor }
                .filter { it.actorObjectType == XapiEntityObjectTypeFlags.AGENT }
                .takeIf { it.isNotEmpty() }
                ?.also { agents ->
                    //Name is the only property that could be updated on the Agent. All other
                    //properties are identifiers
                    repoOrDb.actorDao().insertOrUpdateActorsIfNameChanged(agents)
                }

            val groupEntities = actorEntities.map { it.actor }
                .filter { it.actorObjectType == XapiEntityObjectTypeFlags.GROUP }

            val existingGroupActorHashes = db.actorDao().findUidAndEtagByListAsync(
                groupEntities.map { it.actorUid }
            )

            val allGroupMemberAgents = actorEntities.flatMap {
                it.groupMemberAgents
            }.associateBy { it.actorUid }

            groupEntities.forEach { groupActorEntity ->
                /* A group can be an identified group or anonymous group
                 * See XapiGroup.toGroupEntities for details on how this is handled.
                 */
                val existingEtagAndLct = existingGroupActorHashes.firstOrNull {
                    it.actorUid == groupActorEntity.actorUid
                }

                val groupIsNewOrUpdated = if(!groupActorEntity.isAnonymous()) {
                    groupActorEntity.actorEtag != existingEtagAndLct?.actorEtag
                }else {
                    true
                }

                val groupMemberJoins = actorEntities.flatMap {
                    it?.groupMemberJoins ?: emptyList()
                }.filter {
                    it.gmajGroupActorUid == groupActorEntity.actorUid
                }

                val groupMemberActors = groupMemberJoins.mapNotNull {
                    allGroupMemberAgents[it.gmajMemberActorUid]
                }

                //Just in case we don't have the actor entity locally, run insert or ignore
                repoOrDb.actorDao().insertOrUpdateActorsIfNameChanged(groupMemberActors)

                if(!groupIsNewOrUpdated && existingEtagAndLct != null) {
                    //Run only insert or ignore statements for GroupMemberJoin. Set the last mod
                    //time on all group member joins to match the group actor's last mod time.

                    //Do not update the ActorEntity for the group itself - it hasn't changed.

                    repoOrDb.groupMemberActorJoinDao().insertOrUpdateIfLastModChanged(
                        memberJoins = groupMemberJoins.map {
                            it.copy(gmajLastMod = existingEtagAndLct.actorLct)
                        },
                        lastModTime = existingEtagAndLct.actorLct,
                    )
                }else {
                    //Group is new or has been updated.
                    repoOrDb.actorDao().upsertListAsync(listOf(groupActorEntity))
                    repoOrDb.groupMemberActorJoinDao().upsertListAsync(groupMemberJoins)
                }
            }

            repoOrDb.verbDao().insertOrIgnoreAsync(
                statementEntities.mapNotNull { it.verbEntities?.verbEntity }
            )

            repoOrDb.verbLangMapEntryDao().upsertList(
                statementEntities.flatMap { it.verbEntities?.verbLangMapEntries ?: emptyList() }
            )

            storeActivitiesUseCase(statementEntities.flatMap { it.activityEntities ?: emptyList() })
        }

        return StatementStoreResult(
            statementUuids = statementEntities.mapNotNull { statementEntity ->
                statementEntity.statementEntity?.let { Uuid(it.statementIdHi, it.statementIdLo) }
            }
        )
    }


    /**
     * Put Statement API roughly as per the xAPI spec here:
     *
     */
    suspend fun put(
        statement: XapiStatement,
        statementIdParam: String,
        xapiSession: XapiSession,
    ): String {
        val storeResult = storeStatements(
            listOf(statement.copy(id = statementIdParam)), xapiSession
        )

        return storeResult.statementUuids.first().toString()
    }

    suspend fun post(
        statements: List<XapiStatement>,
        xapiSession: XapiSession
    ): List<Uuid> {
        return storeStatements(statements, xapiSession).statementUuids
    }

    /**
     * Get Statement API roughly as per the xAPI spec here:
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     *
     * NOTE: X-Experience-API-Consistent-Through header needs considered.
     */
    suspend fun get(
        xapiSession: XapiSession,
        statementId: String?,
        format: Format = Format.EXACT
    ): List<JsonObject> {
        val (statementIdHi, statementIdLo) = if(statementId != null){
            val uuid = uuidFrom(statementId)
            Pair(uuid.mostSignificantBits, uuid.leastSignificantBits)
        }else {
            Pair(0L, 0L)
        }

        val statements = repoOrDb.statementEntityJsonDao().getStatements(
            stmtJsonIdHi = statementIdHi,
            stmtJsonIdLo = statementIdLo,
        )

        return statements.mapNotNull {
            it.fullStatement?.let {
                json.decodeFromString(JsonObject.serializer(), it)
            }
        }
    }
}