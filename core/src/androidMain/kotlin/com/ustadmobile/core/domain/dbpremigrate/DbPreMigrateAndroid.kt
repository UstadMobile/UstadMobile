package com.ustadmobile.core.domain.dbpremigrate

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.MIGRATION_172_194
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.asRoomMigration
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.execSqlBatch
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// Method exceeds compiler instruction limit: 17343 in androidx.room.RoomOpenHelper$ValidationResult com.ustadmobile.core.db.UmAppDatabase_Impl$1.onValidateSchema(androidx.sqlite.db.SupportSQLiteDatabase
//
//https://stackoverflow.com/questions/54960389/method-exceeds-compiler-instruction-limit-message-on-well-under-64k-method

class DbPreMigrateAndroid(
    private val settings: Settings,
    private val context: Context,
    private val json: Json,
): DbPreMigrate {

    val migration = Migration(172, 194) { db ->
        val numPeople = db.query("SELECT COUNT(*) FROM Person")
        numPeople.moveToNext()
        val result = numPeople.getInt(0)
        println("Now $result in DB")
        numPeople.close()

        val stmts = buildList {

            add("ALTER TABLE CourseBlock ADD COLUMN cbClazzSourcedId TEXT")
            add("ALTER TABLE CourseBlock ADD COLUMN cbCreatedByAppId TEXT")
            add("ALTER TABLE CourseBlock ADD COLUMN cbMetadata TEXT")

            //187
            if(db.dbType() == DoorDbType.SQLITE) {
                add("ALTER TABLE CourseBlock RENAME to CourseBlock_OLD")
                add("CREATE TABLE IF NOT EXISTS CourseBlock (  cbType  INTEGER  NOT NULL , cbIndentLevel  INTEGER  NOT NULL , cbModuleParentBlockUid  INTEGER  NOT NULL , cbTitle  TEXT , cbDescription  TEXT , cbCompletionCriteria  INTEGER  NOT NULL , cbHideUntilDate  INTEGER  NOT NULL , cbDeadlineDate  INTEGER  NOT NULL , cbLateSubmissionPenalty  INTEGER  NOT NULL , cbGracePeriodDate  INTEGER  NOT NULL , cbMaxPoints  REAl , cbMinPoints  REAL , cbIndex  INTEGER  NOT NULL , cbClazzUid  INTEGER  NOT NULL , cbClazzSourcedId  TEXT , cbActive  INTEGER  NOT NULL , cbHidden  INTEGER  NOT NULL , cbEntityUid  INTEGER  NOT NULL , cbLct  INTEGER  NOT NULL , cbSourcedId  TEXT , cbMetadata  TEXT , cbCreatedByAppId  TEXT , cbUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                add("INSERT INTO CourseBlock (cbType, cbIndentLevel, cbModuleParentBlockUid, cbTitle, cbDescription, cbCompletionCriteria, cbHideUntilDate, cbDeadlineDate, cbLateSubmissionPenalty, cbGracePeriodDate, cbMaxPoints, cbMinPoints, cbIndex, cbClazzUid, cbClazzSourcedId, cbActive, cbHidden, cbEntityUid, cbLct, cbSourcedId, cbMetadata, cbCreatedByAppId, cbUid) SELECT cbType, cbIndentLevel, cbModuleParentBlockUid, cbTitle, cbDescription, cbCompletionCriteria, cbHideUntilDate, cbDeadlineDate, cbLateSubmissionPenalty, cbGracePeriodDate, cbMaxPoints, cbMinPoints, cbIndex, cbClazzUid, cbClazzSourcedId, cbActive, cbHidden, cbEntityUid, cbLct, cbSourcedId, cbMetadata, cbCreatedByAppId, cbUid FROM CourseBlock_OLD")
                add("DROP TABLE CourseBlock_OLD")
                add("CREATE INDEX idx_courseblock_cbclazzuid ON CourseBlock (cbClazzUid)")
                add("CREATE INDEX idx_courseblock_cbsourcedid ON CourseBlock (cbSourcedId)")
            }else {
                add("ALTER TABLE CourseBlock ALTER COLUMN cbMaxPoints TYPE FLOAT")
                add("ALTER TABLE CourseBlock ALTER COLUMN cbMaxPoints DROP NOT NULL")
                add("ALTER TABLE CourseBlock ALTER COLUMN cbMinPoints TYPE FLOAT")
                add("ALTER TABLE CourseBlock ALTER COLUMN cbMinPoints DROP NOT NULL")
            }

            //Update for replication to handle multipe primary keys
            val bigIntType = if(db.dbType() == DoorDbType.SQLITE) "INTEGER" else "BIGINT"
            (3..4).forEach {
                add("ALTER TABLE OutgoingReplication ADD COLUMN orPk$it $bigIntType NOT NULL DEFAULT 0")
            }
            if(db.dbType() == DoorDbType.SQLITE){
                //Changes the defaultvalue of orPk2
                add("ALTER TABLE OutgoingReplication RENAME to OutgoingReplication_OLD")
                add("CREATE TABLE IF NOT EXISTS OutgoingReplication (  destNodeId  INTEGER  NOT NULL , orPk1  INTEGER  NOT NULL , orPk2  INTEGER  NOT NULL  DEFAULT 0 , orPk3  INTEGER  NOT NULL  DEFAULT 0 , orPk4  INTEGER  NOT NULL  DEFAULT 0 , orTableId  INTEGER  NOT NULL , orUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                add("INSERT INTO OutgoingReplication (destNodeId, orPk1, orPk2, orPk3, orPk4, orTableId, orUid) SELECT destNodeId, orPk1, orPk2, orPk3, orPk4, orTableId, orUid FROM OutgoingReplication_OLD")
                add("DROP TABLE OutgoingReplication_OLD")
            }else {
                add("ALTER TABLE OutgoingReplication ALTER COLUMN orPk2 SET DEFAULT 0")
            }

            //drop old versions of Xapi tables
            listOf(
                "StudentResult", "StatementEntity", "AgentEntity", "VerbLangMapEntry", "XObjectEntity",
                "ContextXObjectStatementJoin", "VerbEntity"
            ).forEach {
                add("DROP TABLE IF EXISTS $it")
            }

            //was inside an if
                add("CREATE TABLE IF NOT EXISTS StudentResult (  srUid  INTEGER  PRIMARY KEY  NOT NULL , srSourcedId  TEXT , srCourseBlockUid  INTEGER  NOT NULL , srLineItemSourcedId  TEXT , srLineItemHref  TEXT , srClazzUid  INTEGER  NOT NULL , srAssignmentUid  INTEGER  NOT NULL , srStatus  INTEGER  NOT NULL , srMetaData  TEXT , srStudentPersonUid  INTEGER  NOT NULL , srStudentPersonSourcedId  TEXT , srStudentGroupId  INTEGER  NOT NULL , srMarkerPersonUid  INTEGER  NOT NULL , srMarkerGroupId  INTEGER  NOT NULL , srScoreStatus  INTEGER  NOT NULL , srScore  REAl  NOT NULL , srScoreDate  INTEGER  NOT NULL , srLastModified  INTEGER  NOT NULL , srComment  TEXT , srAppId  TEXT , srDeleted  INTEGER  NOT NULL )")

                add("CREATE TABLE IF NOT EXISTS ActivityEntity (  actUid  INTEGER  PRIMARY KEY  NOT NULL , actIdIri  TEXT , actType  TEXT , actMoreInfo  TEXT , actInteractionType  INTEGER  NOT NULL , actCorrectResponsePatterns  TEXT , actLct  INTEGER  NOT NULL )")
                add("CREATE TABLE IF NOT EXISTS ActivityExtensionEntity (  aeeActivityUid  INTEGER  NOT NULL , aeeKeyHash  INTEGER  NOT NULL , aeeKey  TEXT , aeeJson  TEXT , aeeLastMod  INTEGER  NOT NULL , aeeIsDeleted  INTEGER  NOT NULL , PRIMARY KEY (aeeActivityUid, aeeKeyHash) )")
                add("CREATE TABLE IF NOT EXISTS ActivityInteractionEntity (  aieActivityUid  INTEGER  NOT NULL , aieHash  INTEGER  NOT NULL , aieProp  INTEGER  NOT NULL , aieId  TEXT , aieLastMod  INTEGER  NOT NULL , aieIsDeleted  INTEGER  NOT NULL , PRIMARY KEY (aieActivityUid, aieHash) )")
                add("CREATE TABLE IF NOT EXISTS ActivityLangMapEntry (  almeActivityUid  INTEGER  NOT NULL , almeHash  INTEGER  NOT NULL , almeLangCode  TEXT , almeValue  TEXT , almeAieHash  INTEGER  NOT NULL , almeLastMod  INTEGER  NOT NULL , PRIMARY KEY (almeActivityUid, almeHash) )")
                add("CREATE TABLE IF NOT EXISTS ActorEntity (  actorPersonUid  INTEGER  NOT NULL , actorName  TEXT , actorMbox  TEXT , actorMbox_sha1sum  TEXT , actorOpenid  TEXT , actorAccountName  TEXT , actorAccountHomePage  TEXT , actorEtag  INTEGER  NOT NULL , actorLct  INTEGER  NOT NULL , actorObjectType  INTEGER  NOT NULL , actorUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                add("CREATE TABLE IF NOT EXISTS GroupMemberActorJoin (  gmajGroupActorUid  BIGINT  NOT NULL , gmajMemberActorUid  BIGINT  NOT NULL , gmajLastMod  BIGINT  NOT NULL , PRIMARY KEY (gmajGroupActorUid, gmajMemberActorUid) )")
                add("CREATE TABLE IF NOT EXISTS StatementContextActivityJoin (  scajFromStatementIdHi  INTEGER  NOT NULL , scajFromStatementIdLo  INTEGER  NOT NULL , scajToHash  INTEGER  NOT NULL , scajContextType  INTEGER  NOT NULL , scajToActivityUid  INTEGER  NOT NULL , scajToActivityId  TEXT , scajEtag  INTEGER  NOT NULL , PRIMARY KEY (scajFromStatementIdHi, scajFromStatementIdLo, scajToHash) )")
                add("CREATE TABLE IF NOT EXISTS StatementEntity (  statementIdHi  INTEGER  NOT NULL , statementIdLo  INTEGER  NOT NULL , statementActorPersonUid  INTEGER  NOT NULL , statementVerbUid  INTEGER  NOT NULL , statementObjectType  INTEGER  NOT NULL , statementObjectUid1  INTEGER  NOT NULL , statementObjectUid2  INTEGER  NOT NULL , statementActorUid  INTEGER  NOT NULL , authorityActorUid  INTEGER  NOT NULL , teamUid  INTEGER  NOT NULL , resultCompletion  INTEGER , resultSuccess  INTEGER , resultScoreScaled  REAl , resultScoreRaw  REAl , resultScoreMin  REAl , resultScoreMax  REAl , resultDuration  INTEGER , resultResponse  TEXT , timestamp  INTEGER  NOT NULL , stored  INTEGER  NOT NULL , contextRegistrationHi  INTEGER  NOT NULL , contextRegistrationLo  INTEGER  NOT NULL , contextPlatform  TEXT , contextStatementRefIdHi  INTEGER  NOT NULL , contextStatementRefIdLo  INTEGER  NOT NULL , contextInstructorActorUid  INTEGER  NOT NULL , statementLct  INTEGER  NOT NULL , extensionProgress  INTEGER , completionOrProgress  INTEGER  NOT NULL , statementContentEntryUid  INTEGER  NOT NULL , statementLearnerGroupUid  INTEGER  NOT NULL , statementClazzUid  INTEGER  NOT NULL , statementCbUid  INTEGER  NOT NULL , statementDoorNode  INTEGER  NOT NULL , isSubStatement  INTEGER  NOT NULL , PRIMARY KEY (statementIdHi, statementIdLo) )")
                add("CREATE TABLE IF NOT EXISTS StatementEntityJson (  stmtJsonIdHi  INTEGER  NOT NULL , stmtJsonIdLo  INTEGER  NOT NULL , stmtEtag  INTEGER  NOT NULL , fullStatement  TEXT , PRIMARY KEY (stmtJsonIdHi, stmtJsonIdLo) )")
                add("CREATE TABLE IF NOT EXISTS VerbEntity (  verbUid  INTEGER  PRIMARY KEY  NOT NULL , verbUrlId  TEXT , verbDeleted  INTEGER  NOT NULL , verbLct  INTEGER  NOT NULL )")
                add("CREATE TABLE IF NOT EXISTS VerbLangMapEntry (  vlmeVerbUid  INTEGER  NOT NULL , vlmeLangHash  INTEGER  NOT NULL , vlmeLangCode  TEXT , vlmeEntryString  TEXT , vlmeLastModified  INTEGER  NOT NULL , PRIMARY KEY (vlmeVerbUid, vlmeLangHash) )")
                add("CREATE TABLE IF NOT EXISTS XapiSessionEntity (  xseLastMod  INTEGER  NOT NULL , xseRegistrationHi  INTEGER  NOT NULL , xseRegistrationLo  INTEGER  NOT NULL , xseUsUid  INTEGER  NOT NULL , xseAccountPersonUid  INTEGER  NOT NULL , xseAccountUsername  TEXT , xseClazzUid  INTEGER  NOT NULL , xseCbUid  INTEGER  NOT NULL , xseContentEntryUid  INTEGER  NOT NULL , xseRootActivityId  TEXT , xseStartTime  INTEGER  NOT NULL , xseExpireTime  INTEGER  NOT NULL , xseAuth  TEXT , xseUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")


            //Indexes
            add("CREATE INDEX idx_actorentity_actorobjecttype ON ActorEntity (actorObjectType)")
            add("CREATE INDEX idx_actorentity_uid_personuid ON ActorEntity (actorPersonUid)")

            add("CREATE INDEX idx_stmt_actor_person ON StatementEntity (statementActorPersonUid)")
            add("CREATE INDEX idx_statement_clazz_person ON StatementEntity (statementClazzUid, statementActorPersonUid)")
            add("CREATE INDEX idx_statement_cbuid_actor ON StatementEntity (statementCbUid, statementActorUid)")

            add("CREATE INDEX idx_groupmemberactorjoin_gmajgroupactoruid ON GroupMemberActorJoin (gmajGroupActorUid)")
            add("CREATE INDEX idx_groupmemberactorjoin_gmajmemberactoruid ON GroupMemberActorJoin (gmajMemberActorUid)")

            add("DROP INDEX IF EXISTS idx_courseblock_cbsourcedid")

            //Add columns back to discussion post
            val (boolColType, boolDefaultVal) = if(db.dbType() == DoorDbType.SQLITE) {
                Pair("INTEGER", "0")
            }else {
                Pair("BOOL", "FALSE")
            }

            add("ALTER TABLE DiscussionPost ADD COLUMN discussionPostVisible INTEGER NOT NULL DEFAULT 0")
            add("ALTER TABLE DiscussionPost ADD COLUMN discussionPostArchive INTEGER NOT NULL DEFAULT 0")

            //Drop old tables
            listOf(
                "NetworkNode", "AccessToken", "ScrapeQueueItem", "ContainerEntry",
                "ContainerEntryFile", "LocallyAvailableContainer", "ContainerEtag",
                "ContainerImportJob", "Role", "XLangMapEntry", "School", "SchoolMember",
                "Chat", "ChatMember", "MessageRead", "StateEntity", "StateContentEntity",
                "Container"
            ).forEach {
                add("DROP TABLE IF EXISTS $it")
            }
        }

        db.execSqlBatch(stmts.toTypedArray())
    }

    override suspend fun invoke() {
        val dbSet = settings.getStringOrNull(SETTINGS_KEY_VERSION)
        if(dbSet == null) {
            val currentEndpointStr = settings.getStringOrNull(ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY)
            val initEndpoints: List<String> = settings.getStringOrNull(
                ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
            )?.let {
                json.decodeFromString(ListSerializer(String.serializer()), it)
            } ?: currentEndpointStr?.let { listOf(it) } ?: emptyList()

            initEndpoints.forEach {
                try {
                    val name = sanitizeDbNameFromUrl(it)
                    val db = Room.databaseBuilder(context, UmAppDatabase::class.java, name)
                        .addMigrations(migration)
                        .build()
                    db.personDao().findByUidAsync(0)
                    db.close()
                    Log.i("PreMigrate", "Pre-migrated db $name")
                }catch(e: Exception) {
                    Log.e("PreMigrate", "Something bad", e)
                    e.printStackTrace()
                }

            }

            settings[SETTINGS_KEY_VERSION] = "194"
        }
    }

    companion object {

        const val SETTINGS_KEY_VERSION = "dbVersion"

    }
}