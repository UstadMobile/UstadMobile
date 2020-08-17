package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.*
import com.ustadmobile.door.annotation.MinSyncVersion
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

@Database(entities = [NetworkNode::class, DownloadJobItemHistory::class,
    ClazzLog::class, ClazzLogAttendanceRecord::class, PersonField::class,
    PersonDetailPresenterField::class,
    Schedule::class, DateRange::class, HolidayCalendar::class, Holiday::class,
    ScheduledCheck::class,
    AuditLog::class, CustomField::class, CustomFieldValue::class, CustomFieldValueOption::class,
    Person::class, DownloadJob::class, DownloadJobItem::class, DownloadJobItemParentChildJoin::class,
    Clazz::class, ClazzMember::class, PersonCustomFieldValue::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    EntityRole::class, PersonGroup::class, PersonGroupMember::class,
    PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ContentEntryStatus::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, XObjectEntity::class, StatementEntity::class,
    ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class, XLangMapEntry::class,
    SyncNode::class, LocallyAvailableContainer::class, ContainerETag::class,
    SyncResult::class, School::class,
    TimeZoneEntity::class,
    SchoolMember::class, ClazzWork::class, ClazzWorkContentJoin::class, Comments::class,
    ClazzWorkQuestion::class, ClazzWorkQuestionOption::class, ClazzWorkSubmission::class,
    ClazzWorkQuestionResponse::class, ContentEntryProgress::class,
    Report::class, ReportFilter::class,
    DeviceSession::class, WorkSpace::class, ContainerUploadJob::class

    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_TRACKER_ENTITIES

], version = 37)
@MinSyncVersion(28)
abstract class UmAppDatabase : DoorDatabase(), SyncableDoorDatabase {

    /*
        Changes from 36:
        1. Added school uid to Clazz
        2. Added school Phone number to School
        3. Added schoolGender to School
        4. Added schoolHolidayCalendar to School
        5. Added SchoolMember and SchoolMemberDao
        6. Added ClazzWork, ClazzWorkContentJoin, Comments,ClazzWorkQuestion,ClazzWorkQuestionOption
        7. Added ContainerUploadJob
        Changes in 34:
        Added School and Assignment based entities
        Updated Clazz : added clazzFeatures and removed individual feature bits
     */


    var attachmentsDir: String? = null

    override val master: Boolean
        get() = false


    /**
     * Preload a few entities where we have fixed UIDs for fixed items (e.g. Xapi Verbs)
     */
    fun preload() {
        verbDao.initPreloadedVerbs()
    }

    @JsName("networkNodeDao")
    abstract val networkNodeDao: NetworkNodeDao

    @JsName("downloadJobDao")
    abstract val downloadJobDao: DownloadJobDao

    @JsName("downloadJobItemDao")
    abstract val downloadJobItemDao: DownloadJobItemDao

    @JsName("downloadJobItemParentChildJoinDao")
    abstract val downloadJobItemParentChildJoinDao: DownloadJobItemParentChildJoinDao

    @JsName("downloadJobItemHistoryDao")
    abstract val downloadJobItemHistoryDao: DownloadJobItemHistoryDao

    @JsName("personDao")
    abstract val personDao: PersonDao

    @JsName("clazzDao")
    abstract val clazzDao: ClazzDao

    @JsName("clazzMemberDao")
    abstract val clazzMemberDao: ClazzMemberDao

    @JsName("contentEntryDao")
    abstract val contentEntryDao: ContentEntryDao

    @JsName("contentEntryContentCategoryJoinDao")
    abstract val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao

    @JsName("contentEntryParentChildJoinDao")
    abstract val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao

    @JsName("contentEntryRelatedEntryJoinDao")
    abstract val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao

    // abstract val syncStatusDao: SyncStatusDao

    @JsName("contentCategorySchemaDao")
    abstract val contentCategorySchemaDao: ContentCategorySchemaDao

    @JsName("contentCategoryDao")
    abstract val contentCategoryDao: ContentCategoryDao

    @JsName("languageDao")
    abstract val languageDao: LanguageDao

    @JsName("languageVariantDao")
    abstract val languageVariantDao: LanguageVariantDao

    @JsName("scrapeQueueItemDao")
    abstract val scrapeQueueItemDao: ScrapeQueueItemDao

    @JsName("personAuthDao")
    abstract val personAuthDao: PersonAuthDao

    @JsName("accessTokenDao")
    abstract val accessTokenDao: AccessTokenDao

    @JsName("roleDao")
    abstract val roleDao: RoleDao

    @JsName("personGroupDao")
    abstract val personGroupDao: PersonGroupDao

    @JsName("personGroupMemberDao")
    abstract val personGroupMemberDao: PersonGroupMemberDao

    @JsName("entityRoleDao")
    abstract val entityRoleDao: EntityRoleDao

    @JsName("personPictureDao")
    abstract val personPictureDao: PersonPictureDao

    @JsName("scrapeRunDao")
    abstract val scrapeRunDao: ScrapeRunDao

    @JsName("contentEntryStatusDao")
    abstract val contentEntryStatusDao: ContentEntryStatusDao

    @JsName("connectivityStatusDao")
    abstract val connectivityStatusDao: ConnectivityStatusDao

    @JsName("containerDao")
    abstract val containerDao: ContainerDao

    @JsName("containerEntryDao")
    abstract val containerEntryDao: ContainerEntryDao

    @JsName("containerEntryFileDao")
    abstract val containerEntryFileDao: ContainerEntryFileDao

    @JsName("verbDao")
    abstract val verbDao: VerbDao

    @JsName("xObjectDao")
    abstract val xObjectDao: XObjectDao

    @JsName("reportDao")
    abstract val reportDao: ReportDao

    @JsName("reportFilterDao")
    abstract val reportFilterDao: ReportFilterDao

    @JsName("containerUploadJobDao")
    abstract val containerUploadJobDao: ContainerUploadJobDao

    @JsName("statementDao")
    abstract val statementDao: StatementDao

    @JsName("contextXObjectStatementJoinDao")
    abstract val contextXObjectStatementJoinDao: ContextXObjectStatementJoinDao

    @JsName("stateDao")
    abstract val stateDao: StateDao

    @JsName("stateContentDao")
    abstract val stateContentDao: StateContentDao

    @JsName("agentDao")
    abstract val agentDao: AgentDao

    @JsName("contentEntryProgressDao")
    abstract val contentEntryProgressDao: ContentEntryProgressDao

    abstract val syncresultDao: SyncResultDao


    abstract val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
    abstract val clazzLogDao: ClazzLogDao
    abstract val customFieldDao: CustomFieldDao
    abstract val customFieldValueDao: CustomFieldValueDao
    abstract val customFieldValueOptionDao: CustomFieldValueOptionDao

    abstract val personDetailPresenterFieldDao: PersonDetailPresenterFieldDao

    abstract val scheduleDao: ScheduleDao

    abstract val scheduledCheckDao: ScheduledCheckDao

    abstract val holidayCalendarDao: HolidayCalendarDao
    abstract val holidayDao: HolidayDao
    abstract val schoolDao: SchoolDao

    @JsName("xLangMapEntryDao")
    abstract val xLangMapEntryDao: XLangMapEntryDao

    abstract val locallyAvailableContainerDao: LocallyAvailableContainerDao

    @JsName("timeZoneEntityDao")
    abstract val timeZoneEntityDao: TimeZoneEntityDao

    @JsName("schoolMemberDao")
    abstract val schoolMemberDao: SchoolMemberDao

    @JsName("clazzWorkDao")
    abstract val clazzWorkDao: ClazzWorkDao

    @JsName("clazzWorkSubmissionDao")
    abstract val clazzWorkSubmissionDao: ClazzWorkSubmissionDao

    @JsName("clazzWorkContentJoinDao")
    abstract val clazzWorkContentJoinDao: ClazzWorkContentJoinDao

    @JsName("clazzWorkQuestionDao")
    abstract val clazzWorkQuestionDao: ClazzWorkQuestionDao

    @JsName("clazzWorkQuestionOptionDao")
    abstract val clazzWorkQuestionOptionDao: ClazzWorkQuestionOptionDao

    @JsName("commentsDao")
    abstract val commentsDao: CommentsDao

    @JsName("clazzWorkQuestionResponseDao")
    abstract val clazzWorkQuestionResponseDao: ClazzWorkQuestionResponseDao

    @JsName("syncNodeDao")
    abstract val syncNodeDao: SyncNodeDao

    @JsName("deviceSessionDao")
    abstract val deviceSessionDao: DeviceSessionDao

    @JsName("workSpaceDao")
    abstract val workSpaceDao: WorkSpaceDao

    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_SYNCDAO


    companion object {

        const val TAG_DB = 2

        const val TAG_REPO = 4


        @Volatile
        private var instance: UmAppDatabase? = null

        private val namedInstances = mutableMapOf<String, UmAppDatabase>()

        /**
         * For use by other projects using this app as a library. By calling setInstance before
         * any other usage (e.g. in the Android Application class) a child class of this database (eg.
         * with additional entities) can be used.
         *
         * @param instance
         */
        @Synchronized
        @JsName("setInstance")
        fun setInstance(instance: UmAppDatabase) {
            UmAppDatabase.instance = instance
        }

        /**
         * For use by other projects using this app as a library. By calling setInstance before
         * any other usage (e.g. in the Android Application class) a child class of this database (eg.
         * with additional entities) can be used.
         *
         * @param instance
         * @param dbName
         */
        @Synchronized
        @JsName("setInstanceWithName")
        fun setInstance(instance: UmAppDatabase, dbName: String) {
            namedInstances[dbName] = instance
        }

        @JsName("getInstance")
        fun getInstance(context: Any) = lazy { getInstance(context, "UmAppDatabase") }.value

        @JsName("getInstanceWithDbName")
        @Synchronized
        fun getInstance(context: Any, dbName: String): UmAppDatabase {
            var db = namedInstances[dbName]

            if (db == null) {
                var builder = DatabaseBuilder.databaseBuilder(
                        context, UmAppDatabase::class, dbName)
                builder = addMigrations(builder)
                //db = addCallbacks(builder).build()
                db = builder.build()
                namedInstances[dbName] = db
            }

            return db
        }

        val MIGRATION_25_26 = object : DoorMigration(25, 26) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("ALTER TABLE ContentEntry DROP COLUMN imported, ADD COLUMN status INTEGER NOT NULL DEFAULT 1")
            }
        }


        val MIGRATION_26_27 = object : DoorMigration(26, 27) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS LocallyAvailableContainer (  laContainerUid  BIGINT  PRIMARY KEY  NOT NULL )")
            }
        }


        val MIGRATION_27_28 = object : DoorMigration(27, 28) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("DROP TABLE EntryStatusResponse")
            }
        }

        val MIGRATION_29_30 = object : DoorMigration(29, 30) {
            override fun migrate(database: DoorSqlDatabase) {

                if (database.dbType() == DoorDbType.SQLITE) {

                    database.execSQL("CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoin (  contextActivityFlag  INTEGER , contextStatementUid  BIGINT , contextXObjectUid  BIGINT , verbMasterChangeSeqNum  BIGINT , verbLocalChangeSeqNum  BIGINT , verbLastChangedBy  INTEGER , contextXObjectStatementJoinUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE TRIGGER IF NOT EXISTS UPD_66
                    |AFTER UPDATE ON ContextXObjectStatementJoin FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.verbMasterChangeSeqNum = 0 
                    |OR OLD.verbMasterChangeSeqNum = NEW.verbMasterChangeSeqNum
                    |)
                    |ELSE
                    |(NEW.verbLocalChangeSeqNum = 0  
                    |OR OLD.verbLocalChangeSeqNum = NEW.verbLocalChangeSeqNum
                    |) END)
                    |BEGIN 
                    |UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
                    |ELSE (SELECT MAX(MAX(verbLocalChangeSeqNum), OLD.verbLocalChangeSeqNum) + 1 FROM ContextXObjectStatementJoin) END),
                    |verbMasterChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(MAX(verbMasterChangeSeqNum), OLD.verbMasterChangeSeqNum) + 1 FROM ContextXObjectStatementJoin)
                    |ELSE NEW.verbMasterChangeSeqNum END)
                    |WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid
                    |; END
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER IF NOT EXISTS INS_66
                    |AFTER INSERT ON ContextXObjectStatementJoin FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.verbMasterChangeSeqNum = 0 
                    |
                    |)
                    |ELSE
                    |(NEW.verbLocalChangeSeqNum = 0  
                    |
                    |) END)
                    |BEGIN 
                    |UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
                    |ELSE (SELECT MAX(verbLocalChangeSeqNum) + 1 FROM ContextXObjectStatementJoin) END),
                    |verbMasterChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(verbMasterChangeSeqNum) + 1 FROM ContextXObjectStatementJoin)
                    |ELSE NEW.verbMasterChangeSeqNum END)
                    |WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid
                    |; END
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX  IF NOT EXISTS index_ContextXObjectStatementJoin_trk_clientId_epk_rx_csn 
                    |ON ContextXObjectStatementJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                } else if (database.dbType() == DoorDbType.POSTGRES) {

                    database.execSQL("CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoin (  contextActivityFlag  INTEGER , contextStatementUid  BIGINT , contextXObjectUid  BIGINT , verbMasterChangeSeqNum  BIGINT , verbLocalChangeSeqNum  BIGINT , verbLastChangedBy  INTEGER , contextXObjectStatementJoinUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContextXObjectStatementJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContextXObjectStatementJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_66_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContextXObjectStatementJoin_lcsn_seq') END),
                    | verbMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContextXObjectStatementJoin_mcsn_seq') 
                    | ELSE NEW.verbMasterChangeSeqNum END)
                    | WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""DROP TRIGGER IF EXISTS inccsn_66_trig ON ContextXObjectStatementJoin""".trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_66_trig 
                    |AFTER UPDATE OR INSERT ON ContextXObjectStatementJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_66_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX IF NOT EXISTS index_ContextXObjectStatementJoin_trk_clientId_epk_rx_csn 
                    |ON ContextXObjectStatementJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                }
            }
        }

        val MIGRATION_32_33 = object : DoorMigration(32, 33) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("""ALTER TABLE ScrapeQueueItem ADD COLUMN errorCode INTEGER NOT NULL DEFAULT 0""".trimMargin())
            }
        }

        val MIGRATION_33_34 = object : DoorMigration(33, 34) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("""ALTER TABLE ScrapeQueueItem ADD COLUMN priority INTEGER NOT NULL DEFAULT 1""".trimMargin())
            }
        }

        val MIGRATION_35_36 = object : DoorMigration(35, 36) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("ALTER TABLE StatementEntity ALTER COLUMN resultScoreScaled TYPE FLOAT")
            }
        }

        /**
         * Fix SQLite update triggers, add locallyavailablecontainer and move to 30
         */
        val MIGRATION_29_30_TRIGGERS = object : DoorMigration(29, 30) {
            override fun migrate(database: DoorSqlDatabase) {

                if (database.dbType() == DoorDbType.SQLITE) {

                    database.execSQL("CREATE TABLE IF NOT EXISTS LocallyAvailableContainer ( " +
                            " laContainerUid  INTEGER PRIMARY KEY  NOT NULL )")

                    database.execSQL("DROP TRIGGER IF EXISTS UPD_14")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_14")
                    database.execSQL("""
                    |CREATE TRIGGER UPD_14
                    |AFTER UPDATE ON ClazzLog FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.clazzLogMSQN = 0 
                    |OR OLD.clazzLogMSQN = NEW.clazzLogMSQN
                    |)
                    |ELSE
                    |(NEW.clazzLogLCSN = 0  
                    |OR OLD.clazzLogLCSN = NEW.clazzLogLCSN
                    |) END)
                    |BEGIN 
                    |UPDATE ClazzLog SET clazzLogLCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
                    |ELSE (SELECT MAX(MAX(clazzLogLCSN), OLD.clazzLogLCSN) + 1 FROM ClazzLog) END),
                    |clazzLogMSQN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(MAX(clazzLogMSQN), OLD.clazzLogMSQN) + 1 FROM ClazzLog)
                    |ELSE NEW.clazzLogMSQN END)
                    |WHERE clazzLogUid = NEW.clazzLogUid
                    |; END
                    """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_14
        |AFTER INSERT ON ClazzLog FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzLogMSQN = 0 
        |
        |)
        |ELSE
        |(NEW.clazzLogLCSN = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ClazzLog SET clazzLogLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
        |ELSE (SELECT MAX(clazzLogLCSN) + 1 FROM ClazzLog) END),
        |clazzLogMSQN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzLogMSQN) + 1 FROM ClazzLog)
        |ELSE NEW.clazzLogMSQN END)
        |WHERE clazzLogUid = NEW.clazzLogUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_15")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_15")
                    database.execSQL("""
        |CREATE TRIGGER UPD_15
        |AFTER UPDATE ON ClazzLogAttendanceRecord FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0 
        |OR OLD.clazzLogAttendanceRecordMasterChangeSeqNum = NEW.clazzLogAttendanceRecordMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0  
        |OR OLD.clazzLogAttendanceRecordLocalChangeSeqNum = NEW.clazzLogAttendanceRecordLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(clazzLogAttendanceRecordLocalChangeSeqNum), OLD.clazzLogAttendanceRecordLocalChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord) END),
        |clazzLogAttendanceRecordMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(clazzLogAttendanceRecordMasterChangeSeqNum), OLD.clazzLogAttendanceRecordMasterChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord)
        |ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
        |WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_15
        |AFTER INSERT ON ClazzLogAttendanceRecord FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
        |ELSE (SELECT MAX(clazzLogAttendanceRecordLocalChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord) END),
        |clazzLogAttendanceRecordMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzLogAttendanceRecordMasterChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord)
        |ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
        |WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_121")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_121")
                    database.execSQL("""
        |CREATE TRIGGER UPD_121
        |AFTER UPDATE ON FeedEntry FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.feedEntryMasterChangeSeqNum = 0 
        |OR OLD.feedEntryMasterChangeSeqNum = NEW.feedEntryMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.feedEntryLocalChangeSeqNum = 0  
        |OR OLD.feedEntryLocalChangeSeqNum = NEW.feedEntryLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE FeedEntry SET feedEntryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.feedEntryLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(feedEntryLocalChangeSeqNum), OLD.feedEntryLocalChangeSeqNum) + 1 FROM FeedEntry) END),
        |feedEntryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(feedEntryMasterChangeSeqNum), OLD.feedEntryMasterChangeSeqNum) + 1 FROM FeedEntry)
        |ELSE NEW.feedEntryMasterChangeSeqNum END)
        |WHERE feedEntryUid = NEW.feedEntryUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_121
        |AFTER INSERT ON FeedEntry FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.feedEntryMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.feedEntryLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE FeedEntry SET feedEntryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.feedEntryLocalChangeSeqNum 
        |ELSE (SELECT MAX(feedEntryLocalChangeSeqNum) + 1 FROM FeedEntry) END),
        |feedEntryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(feedEntryMasterChangeSeqNum) + 1 FROM FeedEntry)
        |ELSE NEW.feedEntryMasterChangeSeqNum END)
        |WHERE feedEntryUid = NEW.feedEntryUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_20")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_20")
                    database.execSQL("""
        |CREATE TRIGGER UPD_20
        |AFTER UPDATE ON PersonField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personFieldMasterChangeSeqNum = 0 
        |OR OLD.personFieldMasterChangeSeqNum = NEW.personFieldMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.personFieldLocalChangeSeqNum = 0  
        |OR OLD.personFieldLocalChangeSeqNum = NEW.personFieldLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE PersonField SET personFieldLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personFieldLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(personFieldLocalChangeSeqNum), OLD.personFieldLocalChangeSeqNum) + 1 FROM PersonField) END),
        |personFieldMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(personFieldMasterChangeSeqNum), OLD.personFieldMasterChangeSeqNum) + 1 FROM PersonField)
        |ELSE NEW.personFieldMasterChangeSeqNum END)
        |WHERE personCustomFieldUid = NEW.personCustomFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_20
        |AFTER INSERT ON PersonField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personFieldMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.personFieldLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonField SET personFieldLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personFieldLocalChangeSeqNum 
        |ELSE (SELECT MAX(personFieldLocalChangeSeqNum) + 1 FROM PersonField) END),
        |personFieldMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(personFieldMasterChangeSeqNum) + 1 FROM PersonField)
        |ELSE NEW.personFieldMasterChangeSeqNum END)
        |WHERE personCustomFieldUid = NEW.personCustomFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_19")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_19")
                    database.execSQL("""
        |CREATE TRIGGER UPD_19
        |AFTER UPDATE ON PersonDetailPresenterField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personDetailPresenterFieldMasterChangeSeqNum = 0 
        |OR OLD.personDetailPresenterFieldMasterChangeSeqNum = NEW.personDetailPresenterFieldMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.personDetailPresenterFieldLocalChangeSeqNum = 0  
        |OR OLD.personDetailPresenterFieldLocalChangeSeqNum = NEW.personDetailPresenterFieldLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE PersonDetailPresenterField SET personDetailPresenterFieldLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(personDetailPresenterFieldLocalChangeSeqNum), OLD.personDetailPresenterFieldLocalChangeSeqNum) + 1 FROM PersonDetailPresenterField) END),
        |personDetailPresenterFieldMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(personDetailPresenterFieldMasterChangeSeqNum), OLD.personDetailPresenterFieldMasterChangeSeqNum) + 1 FROM PersonDetailPresenterField)
        |ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END)
        |WHERE personDetailPresenterFieldUid = NEW.personDetailPresenterFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_19
        |AFTER INSERT ON PersonDetailPresenterField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personDetailPresenterFieldMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.personDetailPresenterFieldLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonDetailPresenterField SET personDetailPresenterFieldLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum 
        |ELSE (SELECT MAX(personDetailPresenterFieldLocalChangeSeqNum) + 1 FROM PersonDetailPresenterField) END),
        |personDetailPresenterFieldMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(personDetailPresenterFieldMasterChangeSeqNum) + 1 FROM PersonDetailPresenterField)
        |ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END)
        |WHERE personDetailPresenterFieldUid = NEW.personDetailPresenterFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_22")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_22")
                    database.execSQL("""
        |CREATE TRIGGER UPD_22
        |AFTER UPDATE ON SelQuestion FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionMasterChangeSeqNum = 0 
        |OR OLD.selQuestionMasterChangeSeqNum = NEW.selQuestionMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionLocalChangeSeqNum = 0  
        |OR OLD.selQuestionLocalChangeSeqNum = NEW.selQuestionLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionLocalChangeSeqNum), OLD.selQuestionLocalChangeSeqNum) + 1 FROM SelQuestion) END),
        |selQuestionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionMasterChangeSeqNum), OLD.selQuestionMasterChangeSeqNum) + 1 FROM SelQuestion)
        |ELSE NEW.selQuestionMasterChangeSeqNum END)
        |WHERE selQuestionUid = NEW.selQuestionUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_22
        |AFTER INSERT ON SelQuestion FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionLocalChangeSeqNum) + 1 FROM SelQuestion) END),
        |selQuestionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionMasterChangeSeqNum) + 1 FROM SelQuestion)
        |ELSE NEW.selQuestionMasterChangeSeqNum END)
        |WHERE selQuestionUid = NEW.selQuestionUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_23")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_23")
                    database.execSQL("""
        |CREATE TRIGGER UPD_23
        |AFTER UPDATE ON SelQuestionResponse FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionResponseMasterChangeSeqNum = 0 
        |OR OLD.selQuestionResponseMasterChangeSeqNum = NEW.selQuestionResponseMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionResponseLocalChangeSeqNum = 0  
        |OR OLD.selQuestionResponseLocalChangeSeqNum = NEW.selQuestionResponseLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionResponseLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionResponseLocalChangeSeqNum), OLD.selQuestionResponseLocalChangeSeqNum) + 1 FROM SelQuestionResponse) END),
        |selQuestionResponseMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionResponseMasterChangeSeqNum), OLD.selQuestionResponseMasterChangeSeqNum) + 1 FROM SelQuestionResponse)
        |ELSE NEW.selQuestionResponseMasterChangeSeqNum END)
        |WHERE selQuestionResponseUid = NEW.selQuestionResponseUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_23
        |AFTER INSERT ON SelQuestionResponse FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionResponseMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionResponseLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionResponseLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionResponseLocalChangeSeqNum) + 1 FROM SelQuestionResponse) END),
        |selQuestionResponseMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionResponseMasterChangeSeqNum) + 1 FROM SelQuestionResponse)
        |ELSE NEW.selQuestionResponseMasterChangeSeqNum END)
        |WHERE selQuestionResponseUid = NEW.selQuestionResponseUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_24")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_24")
                    database.execSQL("""
        |CREATE TRIGGER UPD_24
        |AFTER UPDATE ON SelQuestionResponseNomination FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selqrnMCSN = 0 
        |OR OLD.selqrnMCSN = NEW.selqrnMCSN
        |)
        |ELSE
        |(NEW.selqrnMCSNLCSN = 0  
        |OR OLD.selqrnMCSNLCSN = NEW.selqrnMCSNLCSN
        |) END)
        |BEGIN 
        |UPDATE SelQuestionResponseNomination SET selqrnMCSNLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selqrnMCSNLCSN 
        |ELSE (SELECT MAX(MAX(selqrnMCSNLCSN), OLD.selqrnMCSNLCSN) + 1 FROM SelQuestionResponseNomination) END),
        |selqrnMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selqrnMCSN), OLD.selqrnMCSN) + 1 FROM SelQuestionResponseNomination)
        |ELSE NEW.selqrnMCSN END)
        |WHERE selqrnUid = NEW.selqrnUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_24
        |AFTER INSERT ON SelQuestionResponseNomination FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selqrnMCSN = 0 
        |
        |)
        |ELSE
        |(NEW.selqrnMCSNLCSN = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionResponseNomination SET selqrnMCSNLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selqrnMCSNLCSN 
        |ELSE (SELECT MAX(selqrnMCSNLCSN) + 1 FROM SelQuestionResponseNomination) END),
        |selqrnMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selqrnMCSN) + 1 FROM SelQuestionResponseNomination)
        |ELSE NEW.selqrnMCSN END)
        |WHERE selqrnUid = NEW.selqrnUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_25")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_25")
                    database.execSQL("""
        |CREATE TRIGGER UPD_25
        |AFTER UPDATE ON SelQuestionSet FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetMasterChangeSeqNum = 0 
        |OR OLD.selQuestionSetMasterChangeSeqNum = NEW.selQuestionSetMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionSetLocalChangeSeqNum = 0  
        |OR OLD.selQuestionSetLocalChangeSeqNum = NEW.selQuestionSetLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionSetLocalChangeSeqNum), OLD.selQuestionSetLocalChangeSeqNum) + 1 FROM SelQuestionSet) END),
        |selQuestionSetMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionSetMasterChangeSeqNum), OLD.selQuestionSetMasterChangeSeqNum) + 1 FROM SelQuestionSet)
        |ELSE NEW.selQuestionSetMasterChangeSeqNum END)
        |WHERE selQuestionSetUid = NEW.selQuestionSetUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_25
        |AFTER INSERT ON SelQuestionSet FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionSetLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionSetLocalChangeSeqNum) + 1 FROM SelQuestionSet) END),
        |selQuestionSetMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionSetMasterChangeSeqNum) + 1 FROM SelQuestionSet)
        |ELSE NEW.selQuestionSetMasterChangeSeqNum END)
        |WHERE selQuestionSetUid = NEW.selQuestionSetUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_26")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_26")
                    database.execSQL("""
        |CREATE TRIGGER UPD_26
        |AFTER UPDATE ON SelQuestionSetRecognition FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetRecognitionMasterChangeSeqNum = 0 
        |OR OLD.selQuestionSetRecognitionMasterChangeSeqNum = NEW.selQuestionSetRecognitionMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionSetRecognitionLocalChangeSeqNum = 0  
        |OR OLD.selQuestionSetRecognitionLocalChangeSeqNum = NEW.selQuestionSetRecognitionLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionSetRecognitionLocalChangeSeqNum), OLD.selQuestionSetRecognitionLocalChangeSeqNum) + 1 FROM SelQuestionSetRecognition) END),
        |selQuestionSetRecognitionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionSetRecognitionMasterChangeSeqNum), OLD.selQuestionSetRecognitionMasterChangeSeqNum) + 1 FROM SelQuestionSetRecognition)
        |ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END)
        |WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_26
        |AFTER INSERT ON SelQuestionSetRecognition FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetRecognitionMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionSetRecognitionLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionSetRecognitionLocalChangeSeqNum) + 1 FROM SelQuestionSetRecognition) END),
        |selQuestionSetRecognitionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionSetRecognitionMasterChangeSeqNum) + 1 FROM SelQuestionSetRecognition)
        |ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END)
        |WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_27")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_27")
                    database.execSQL("""
        |CREATE TRIGGER UPD_27
        |AFTER UPDATE ON SelQuestionSetResponse FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetResponseMasterChangeSeqNum = 0 
        |OR OLD.selQuestionSetResponseMasterChangeSeqNum = NEW.selQuestionSetResponseMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionSetResponseLocalChangeSeqNum = 0  
        |OR OLD.selQuestionSetResponseLocalChangeSeqNum = NEW.selQuestionSetResponseLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetResponseLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionSetResponseLocalChangeSeqNum), OLD.selQuestionSetResponseLocalChangeSeqNum) + 1 FROM SelQuestionSetResponse) END),
        |selQuestionSetResponseMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionSetResponseMasterChangeSeqNum), OLD.selQuestionSetResponseMasterChangeSeqNum) + 1 FROM SelQuestionSetResponse)
        |ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END)
        |WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_27
        |AFTER INSERT ON SelQuestionSetResponse FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionSetResponseMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionSetResponseLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetResponseLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionSetResponseLocalChangeSeqNum) + 1 FROM SelQuestionSetResponse) END),
        |selQuestionSetResponseMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionSetResponseMasterChangeSeqNum) + 1 FROM SelQuestionSetResponse)
        |ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END)
        |WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_21")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_21")
                    database.execSQL("""
        |CREATE TRIGGER UPD_21
        |AFTER UPDATE ON Schedule FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.scheduleMasterChangeSeqNum = 0 
        |OR OLD.scheduleMasterChangeSeqNum = NEW.scheduleMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.scheduleLocalChangeSeqNum = 0  
        |OR OLD.scheduleLocalChangeSeqNum = NEW.scheduleLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE Schedule SET scheduleLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduleLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(scheduleLocalChangeSeqNum), OLD.scheduleLocalChangeSeqNum) + 1 FROM Schedule) END),
        |scheduleMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(scheduleMasterChangeSeqNum), OLD.scheduleMasterChangeSeqNum) + 1 FROM Schedule)
        |ELSE NEW.scheduleMasterChangeSeqNum END)
        |WHERE scheduleUid = NEW.scheduleUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_21
        |AFTER INSERT ON Schedule FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.scheduleMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.scheduleLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Schedule SET scheduleLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduleLocalChangeSeqNum 
        |ELSE (SELECT MAX(scheduleLocalChangeSeqNum) + 1 FROM Schedule) END),
        |scheduleMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(scheduleMasterChangeSeqNum) + 1 FROM Schedule)
        |ELSE NEW.scheduleMasterChangeSeqNum END)
        |WHERE scheduleUid = NEW.scheduleUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_17")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_17")
                    database.execSQL("""
        |CREATE TRIGGER UPD_17
        |AFTER UPDATE ON DateRange FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.dateRangeMasterChangeSeqNum = 0 
        |OR OLD.dateRangeMasterChangeSeqNum = NEW.dateRangeMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.dateRangeLocalChangeSeqNum = 0  
        |OR OLD.dateRangeLocalChangeSeqNum = NEW.dateRangeLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE DateRange SET dateRangeLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.dateRangeLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(dateRangeLocalChangeSeqNum), OLD.dateRangeLocalChangeSeqNum) + 1 FROM DateRange) END),
        |dateRangeMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(dateRangeMasterChangeSeqNum), OLD.dateRangeMasterChangeSeqNum) + 1 FROM DateRange)
        |ELSE NEW.dateRangeMasterChangeSeqNum END)
        |WHERE dateRangeUid = NEW.dateRangeUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_17
        |AFTER INSERT ON DateRange FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.dateRangeMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.dateRangeLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE DateRange SET dateRangeLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.dateRangeLocalChangeSeqNum 
        |ELSE (SELECT MAX(dateRangeLocalChangeSeqNum) + 1 FROM DateRange) END),
        |dateRangeMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(dateRangeMasterChangeSeqNum) + 1 FROM DateRange)
        |ELSE NEW.dateRangeMasterChangeSeqNum END)
        |WHERE dateRangeUid = NEW.dateRangeUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_28")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_28")
                    database.execSQL("""
        |CREATE TRIGGER UPD_28
        |AFTER UPDATE ON UMCalendar FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.umCalendarMasterChangeSeqNum = 0 
        |OR OLD.umCalendarMasterChangeSeqNum = NEW.umCalendarMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.umCalendarLocalChangeSeqNum = 0  
        |OR OLD.umCalendarLocalChangeSeqNum = NEW.umCalendarLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE UMCalendar SET umCalendarLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(umCalendarLocalChangeSeqNum), OLD.umCalendarLocalChangeSeqNum) + 1 FROM UMCalendar) END),
        |umCalendarMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(umCalendarMasterChangeSeqNum), OLD.umCalendarMasterChangeSeqNum) + 1 FROM UMCalendar)
        |ELSE NEW.umCalendarMasterChangeSeqNum END)
        |WHERE umCalendarUid = NEW.umCalendarUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_28
        |AFTER INSERT ON UMCalendar FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.umCalendarMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.umCalendarLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE UMCalendar SET umCalendarLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
        |ELSE (SELECT MAX(umCalendarLocalChangeSeqNum) + 1 FROM UMCalendar) END),
        |umCalendarMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(umCalendarMasterChangeSeqNum) + 1 FROM UMCalendar)
        |ELSE NEW.umCalendarMasterChangeSeqNum END)
        |WHERE umCalendarUid = NEW.umCalendarUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_11")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_11")
                    database.execSQL("""
        |CREATE TRIGGER UPD_11
        |AFTER UPDATE ON ClazzActivity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzActivityMasterChangeSeqNum = 0 
        |OR OLD.clazzActivityMasterChangeSeqNum = NEW.clazzActivityMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.clazzActivityLocalChangeSeqNum = 0  
        |OR OLD.clazzActivityLocalChangeSeqNum = NEW.clazzActivityLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ClazzActivity SET clazzActivityLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(clazzActivityLocalChangeSeqNum), OLD.clazzActivityLocalChangeSeqNum) + 1 FROM ClazzActivity) END),
        |clazzActivityMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(clazzActivityMasterChangeSeqNum), OLD.clazzActivityMasterChangeSeqNum) + 1 FROM ClazzActivity)
        |ELSE NEW.clazzActivityMasterChangeSeqNum END)
        |WHERE clazzActivityUid = NEW.clazzActivityUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_11
        |AFTER INSERT ON ClazzActivity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzActivityMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.clazzActivityLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ClazzActivity SET clazzActivityLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityLocalChangeSeqNum 
        |ELSE (SELECT MAX(clazzActivityLocalChangeSeqNum) + 1 FROM ClazzActivity) END),
        |clazzActivityMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzActivityMasterChangeSeqNum) + 1 FROM ClazzActivity)
        |ELSE NEW.clazzActivityMasterChangeSeqNum END)
        |WHERE clazzActivityUid = NEW.clazzActivityUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_32")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_32")
                    database.execSQL("""
        |CREATE TRIGGER UPD_32
        |AFTER UPDATE ON ClazzActivityChange FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzActivityChangeMasterChangeSeqNum = 0 
        |OR OLD.clazzActivityChangeMasterChangeSeqNum = NEW.clazzActivityChangeMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.clazzActivityChangeLocalChangeSeqNum = 0  
        |OR OLD.clazzActivityChangeLocalChangeSeqNum = NEW.clazzActivityChangeLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ClazzActivityChange SET clazzActivityChangeLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityChangeLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(clazzActivityChangeLocalChangeSeqNum), OLD.clazzActivityChangeLocalChangeSeqNum) + 1 FROM ClazzActivityChange) END),
        |clazzActivityChangeMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(clazzActivityChangeMasterChangeSeqNum), OLD.clazzActivityChangeMasterChangeSeqNum) + 1 FROM ClazzActivityChange)
        |ELSE NEW.clazzActivityChangeMasterChangeSeqNum END)
        |WHERE clazzActivityChangeUid = NEW.clazzActivityChangeUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_32
        |AFTER INSERT ON ClazzActivityChange FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzActivityChangeMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.clazzActivityChangeLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ClazzActivityChange SET clazzActivityChangeLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityChangeLocalChangeSeqNum 
        |ELSE (SELECT MAX(clazzActivityChangeLocalChangeSeqNum) + 1 FROM ClazzActivityChange) END),
        |clazzActivityChangeMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzActivityChangeMasterChangeSeqNum) + 1 FROM ClazzActivityChange)
        |ELSE NEW.clazzActivityChangeMasterChangeSeqNum END)
        |WHERE clazzActivityChangeUid = NEW.clazzActivityChangeUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_52")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_52")
                    database.execSQL("""
        |CREATE TRIGGER UPD_52
        |AFTER UPDATE ON SelQuestionOption FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionOptionMasterChangeSeqNum = 0 
        |OR OLD.selQuestionOptionMasterChangeSeqNum = NEW.selQuestionOptionMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.selQuestionOptionLocalChangeSeqNum = 0  
        |OR OLD.selQuestionOptionLocalChangeSeqNum = NEW.selQuestionOptionLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionOptionLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(selQuestionOptionLocalChangeSeqNum), OLD.selQuestionOptionLocalChangeSeqNum) + 1 FROM SelQuestionOption) END),
        |selQuestionOptionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(selQuestionOptionMasterChangeSeqNum), OLD.selQuestionOptionMasterChangeSeqNum) + 1 FROM SelQuestionOption)
        |ELSE NEW.selQuestionOptionMasterChangeSeqNum END)
        |WHERE selQuestionOptionUid = NEW.selQuestionOptionUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_52
        |AFTER INSERT ON SelQuestionOption FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.selQuestionOptionMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.selQuestionOptionLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionOptionLocalChangeSeqNum 
        |ELSE (SELECT MAX(selQuestionOptionLocalChangeSeqNum) + 1 FROM SelQuestionOption) END),
        |selQuestionOptionMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(selQuestionOptionMasterChangeSeqNum) + 1 FROM SelQuestionOption)
        |ELSE NEW.selQuestionOptionMasterChangeSeqNum END)
        |WHERE selQuestionOptionUid = NEW.selQuestionOptionUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_173")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_173")
                    database.execSQL("""
        |CREATE TRIGGER UPD_173
        |AFTER UPDATE ON ScheduledCheck FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.scheduledCheckMasterCsn = 0 
        |OR OLD.scheduledCheckMasterCsn = NEW.scheduledCheckMasterCsn
        |)
        |ELSE
        |(NEW.scheduledCheckLocalCsn = 0  
        |OR OLD.scheduledCheckLocalCsn = NEW.scheduledCheckLocalCsn
        |) END)
        |BEGIN 
        |UPDATE ScheduledCheck SET scheduledCheckLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduledCheckLocalCsn 
        |ELSE (SELECT MAX(MAX(scheduledCheckLocalCsn), OLD.scheduledCheckLocalCsn) + 1 FROM ScheduledCheck) END),
        |scheduledCheckMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(scheduledCheckMasterCsn), OLD.scheduledCheckMasterCsn) + 1 FROM ScheduledCheck)
        |ELSE NEW.scheduledCheckMasterCsn END)
        |WHERE scheduledCheckUid = NEW.scheduledCheckUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_173
        |AFTER INSERT ON ScheduledCheck FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.scheduledCheckMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.scheduledCheckLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ScheduledCheck SET scheduledCheckLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduledCheckLocalCsn 
        |ELSE (SELECT MAX(scheduledCheckLocalCsn) + 1 FROM ScheduledCheck) END),
        |scheduledCheckMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(scheduledCheckMasterCsn) + 1 FROM ScheduledCheck)
        |ELSE NEW.scheduledCheckMasterCsn END)
        |WHERE scheduledCheckUid = NEW.scheduledCheckUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_53")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_53")
                    database.execSQL("""
        |CREATE TRIGGER UPD_53
        |AFTER UPDATE ON AuditLog FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.auditLogMasterChangeSeqNum = 0 
        |OR OLD.auditLogMasterChangeSeqNum = NEW.auditLogMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.auditLogLocalChangeSeqNum = 0  
        |OR OLD.auditLogLocalChangeSeqNum = NEW.auditLogLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE AuditLog SET auditLogLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.auditLogLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(auditLogLocalChangeSeqNum), OLD.auditLogLocalChangeSeqNum) + 1 FROM AuditLog) END),
        |auditLogMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(auditLogMasterChangeSeqNum), OLD.auditLogMasterChangeSeqNum) + 1 FROM AuditLog)
        |ELSE NEW.auditLogMasterChangeSeqNum END)
        |WHERE auditLogUid = NEW.auditLogUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_53
        |AFTER INSERT ON AuditLog FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.auditLogMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.auditLogLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE AuditLog SET auditLogLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.auditLogLocalChangeSeqNum 
        |ELSE (SELECT MAX(auditLogLocalChangeSeqNum) + 1 FROM AuditLog) END),
        |auditLogMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(auditLogMasterChangeSeqNum) + 1 FROM AuditLog)
        |ELSE NEW.auditLogMasterChangeSeqNum END)
        |WHERE auditLogUid = NEW.auditLogUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_56")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_56")
                    database.execSQL("""
        |CREATE TRIGGER UPD_56
        |AFTER UPDATE ON CustomField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldMCSN = 0 
        |OR OLD.customFieldMCSN = NEW.customFieldMCSN
        |)
        |ELSE
        |(NEW.customFieldLCSN = 0  
        |OR OLD.customFieldLCSN = NEW.customFieldLCSN
        |) END)
        |BEGIN 
        |UPDATE CustomField SET customFieldLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldLCSN 
        |ELSE (SELECT MAX(MAX(customFieldLCSN), OLD.customFieldLCSN) + 1 FROM CustomField) END),
        |customFieldMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(customFieldMCSN), OLD.customFieldMCSN) + 1 FROM CustomField)
        |ELSE NEW.customFieldMCSN END)
        |WHERE customFieldUid = NEW.customFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_56
        |AFTER INSERT ON CustomField FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldMCSN = 0 
        |
        |)
        |ELSE
        |(NEW.customFieldLCSN = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE CustomField SET customFieldLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldLCSN 
        |ELSE (SELECT MAX(customFieldLCSN) + 1 FROM CustomField) END),
        |customFieldMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(customFieldMCSN) + 1 FROM CustomField)
        |ELSE NEW.customFieldMCSN END)
        |WHERE customFieldUid = NEW.customFieldUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_57")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_57")
                    database.execSQL("""
        |CREATE TRIGGER UPD_57
        |AFTER UPDATE ON CustomFieldValue FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldValueMCSN = 0 
        |OR OLD.customFieldValueMCSN = NEW.customFieldValueMCSN
        |)
        |ELSE
        |(NEW.customFieldValueLCSN = 0  
        |OR OLD.customFieldValueLCSN = NEW.customFieldValueLCSN
        |) END)
        |BEGIN 
        |UPDATE CustomFieldValue SET customFieldValueLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueLCSN 
        |ELSE (SELECT MAX(MAX(customFieldValueLCSN), OLD.customFieldValueLCSN) + 1 FROM CustomFieldValue) END),
        |customFieldValueMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(customFieldValueMCSN), OLD.customFieldValueMCSN) + 1 FROM CustomFieldValue)
        |ELSE NEW.customFieldValueMCSN END)
        |WHERE customFieldValueUid = NEW.customFieldValueUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_57
        |AFTER INSERT ON CustomFieldValue FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldValueMCSN = 0 
        |
        |)
        |ELSE
        |(NEW.customFieldValueLCSN = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE CustomFieldValue SET customFieldValueLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueLCSN 
        |ELSE (SELECT MAX(customFieldValueLCSN) + 1 FROM CustomFieldValue) END),
        |customFieldValueMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(customFieldValueMCSN) + 1 FROM CustomFieldValue)
        |ELSE NEW.customFieldValueMCSN END)
        |WHERE customFieldValueUid = NEW.customFieldValueUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_55")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_55")
                    database.execSQL("""
        |CREATE TRIGGER UPD_55
        |AFTER UPDATE ON CustomFieldValueOption FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldValueOptionMCSN = 0 
        |OR OLD.customFieldValueOptionMCSN = NEW.customFieldValueOptionMCSN
        |)
        |ELSE
        |(NEW.customFieldValueOptionLCSN = 0  
        |OR OLD.customFieldValueOptionLCSN = NEW.customFieldValueOptionLCSN
        |) END)
        |BEGIN 
        |UPDATE CustomFieldValueOption SET customFieldValueOptionLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueOptionLCSN 
        |ELSE (SELECT MAX(MAX(customFieldValueOptionLCSN), OLD.customFieldValueOptionLCSN) + 1 FROM CustomFieldValueOption) END),
        |customFieldValueOptionMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(customFieldValueOptionMCSN), OLD.customFieldValueOptionMCSN) + 1 FROM CustomFieldValueOption)
        |ELSE NEW.customFieldValueOptionMCSN END)
        |WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_55
        |AFTER INSERT ON CustomFieldValueOption FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.customFieldValueOptionMCSN = 0 
        |
        |)
        |ELSE
        |(NEW.customFieldValueOptionLCSN = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE CustomFieldValueOption SET customFieldValueOptionLCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueOptionLCSN 
        |ELSE (SELECT MAX(customFieldValueOptionLCSN) + 1 FROM CustomFieldValueOption) END),
        |customFieldValueOptionMCSN = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(customFieldValueOptionMCSN) + 1 FROM CustomFieldValueOption)
        |ELSE NEW.customFieldValueOptionMCSN END)
        |WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_9")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_9")
                    database.execSQL("""
        |CREATE TRIGGER UPD_9
        |AFTER UPDATE ON Person FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personMasterChangeSeqNum = 0 
        |OR OLD.personMasterChangeSeqNum = NEW.personMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.personLocalChangeSeqNum = 0  
        |OR OLD.personLocalChangeSeqNum = NEW.personLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE Person SET personLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(personLocalChangeSeqNum), OLD.personLocalChangeSeqNum) + 1 FROM Person) END),
        |personMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(personMasterChangeSeqNum), OLD.personMasterChangeSeqNum) + 1 FROM Person)
        |ELSE NEW.personMasterChangeSeqNum END)
        |WHERE personUid = NEW.personUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_9
        |AFTER INSERT ON Person FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.personLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Person SET personLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personLocalChangeSeqNum 
        |ELSE (SELECT MAX(personLocalChangeSeqNum) + 1 FROM Person) END),
        |personMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(personMasterChangeSeqNum) + 1 FROM Person)
        |ELSE NEW.personMasterChangeSeqNum END)
        |WHERE personUid = NEW.personUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_6")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_6")
                    database.execSQL("""
        |CREATE TRIGGER UPD_6
        |AFTER UPDATE ON Clazz FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzMasterChangeSeqNum = 0 
        |OR OLD.clazzMasterChangeSeqNum = NEW.clazzMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.clazzLocalChangeSeqNum = 0  
        |OR OLD.clazzLocalChangeSeqNum = NEW.clazzLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE Clazz SET clazzLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(clazzLocalChangeSeqNum), OLD.clazzLocalChangeSeqNum) + 1 FROM Clazz) END),
        |clazzMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(clazzMasterChangeSeqNum), OLD.clazzMasterChangeSeqNum) + 1 FROM Clazz)
        |ELSE NEW.clazzMasterChangeSeqNum END)
        |WHERE clazzUid = NEW.clazzUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_6
        |AFTER INSERT ON Clazz FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.clazzLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Clazz SET clazzLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLocalChangeSeqNum 
        |ELSE (SELECT MAX(clazzLocalChangeSeqNum) + 1 FROM Clazz) END),
        |clazzMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzMasterChangeSeqNum) + 1 FROM Clazz)
        |ELSE NEW.clazzMasterChangeSeqNum END)
        |WHERE clazzUid = NEW.clazzUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_65")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_65")
                    database.execSQL("""
        |CREATE TRIGGER UPD_65
        |AFTER UPDATE ON ClazzMember FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzMemberMasterChangeSeqNum = 0 
        |OR OLD.clazzMemberMasterChangeSeqNum = NEW.clazzMemberMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.clazzMemberLocalChangeSeqNum = 0  
        |OR OLD.clazzMemberLocalChangeSeqNum = NEW.clazzMemberLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzMemberLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(clazzMemberLocalChangeSeqNum), OLD.clazzMemberLocalChangeSeqNum) + 1 FROM ClazzMember) END),
        |clazzMemberMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(clazzMemberMasterChangeSeqNum), OLD.clazzMemberMasterChangeSeqNum) + 1 FROM ClazzMember)
        |ELSE NEW.clazzMemberMasterChangeSeqNum END)
        |WHERE clazzMemberUid = NEW.clazzMemberUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_65
        |AFTER INSERT ON ClazzMember FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.clazzMemberMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.clazzMemberLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzMemberLocalChangeSeqNum 
        |ELSE (SELECT MAX(clazzMemberLocalChangeSeqNum) + 1 FROM ClazzMember) END),
        |clazzMemberMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(clazzMemberMasterChangeSeqNum) + 1 FROM ClazzMember)
        |ELSE NEW.clazzMemberMasterChangeSeqNum END)
        |WHERE clazzMemberUid = NEW.clazzMemberUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_178")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_178")
                    database.execSQL("""
        |CREATE TRIGGER UPD_178
        |AFTER UPDATE ON PersonCustomFieldValue FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personCustomFieldValueMasterChangeSeqNum = 0 
        |OR OLD.personCustomFieldValueMasterChangeSeqNum = NEW.personCustomFieldValueMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.personCustomFieldValueLocalChangeSeqNum = 0  
        |OR OLD.personCustomFieldValueLocalChangeSeqNum = NEW.personCustomFieldValueLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE PersonCustomFieldValue SET personCustomFieldValueLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personCustomFieldValueLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(personCustomFieldValueLocalChangeSeqNum), OLD.personCustomFieldValueLocalChangeSeqNum) + 1 FROM PersonCustomFieldValue) END),
        |personCustomFieldValueMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(personCustomFieldValueMasterChangeSeqNum), OLD.personCustomFieldValueMasterChangeSeqNum) + 1 FROM PersonCustomFieldValue)
        |ELSE NEW.personCustomFieldValueMasterChangeSeqNum END)
        |WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid
        |; END
        """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER INS_178
                    |AFTER INSERT ON PersonCustomFieldValue FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.personCustomFieldValueMasterChangeSeqNum = 0 
                    |
                    |)
                    |ELSE
                    |(NEW.personCustomFieldValueLocalChangeSeqNum = 0  
                    |
                    |) END)
                    |BEGIN 
                    |UPDATE PersonCustomFieldValue SET personCustomFieldValueLocalChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personCustomFieldValueLocalChangeSeqNum 
                    |ELSE (SELECT MAX(personCustomFieldValueLocalChangeSeqNum) + 1 FROM PersonCustomFieldValue) END),
                    |personCustomFieldValueMasterChangeSeqNum = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(personCustomFieldValueMasterChangeSeqNum) + 1 FROM PersonCustomFieldValue)
                    |ELSE NEW.personCustomFieldValueMasterChangeSeqNum END)
                    |WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid
                    |; END
                    """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_42")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_42")
                    database.execSQL("""
        |CREATE TRIGGER UPD_42
        |AFTER UPDATE ON ContentEntry FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentEntryMasterChangeSeqNum = 0 
        |OR OLD.contentEntryMasterChangeSeqNum = NEW.contentEntryMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.contentEntryLocalChangeSeqNum = 0  
        |OR OLD.contentEntryLocalChangeSeqNum = NEW.contentEntryLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(contentEntryLocalChangeSeqNum), OLD.contentEntryLocalChangeSeqNum) + 1 FROM ContentEntry) END),
        |contentEntryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(contentEntryMasterChangeSeqNum), OLD.contentEntryMasterChangeSeqNum) + 1 FROM ContentEntry)
        |ELSE NEW.contentEntryMasterChangeSeqNum END)
        |WHERE contentEntryUid = NEW.contentEntryUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_42
        |AFTER INSERT ON ContentEntry FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentEntryMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.contentEntryLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryLocalChangeSeqNum 
        |ELSE (SELECT MAX(contentEntryLocalChangeSeqNum) + 1 FROM ContentEntry) END),
        |contentEntryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(contentEntryMasterChangeSeqNum) + 1 FROM ContentEntry)
        |ELSE NEW.contentEntryMasterChangeSeqNum END)
        |WHERE contentEntryUid = NEW.contentEntryUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_3")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_3")
                    database.execSQL("""
        |CREATE TRIGGER UPD_3
        |AFTER UPDATE ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.ceccjMasterChangeSeqNum = 0 
        |OR OLD.ceccjMasterChangeSeqNum = NEW.ceccjMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.ceccjLocalChangeSeqNum = 0  
        |OR OLD.ceccjLocalChangeSeqNum = NEW.ceccjLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.ceccjLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(ceccjLocalChangeSeqNum), OLD.ceccjLocalChangeSeqNum) + 1 FROM ContentEntryContentCategoryJoin) END),
        |ceccjMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(ceccjMasterChangeSeqNum), OLD.ceccjMasterChangeSeqNum) + 1 FROM ContentEntryContentCategoryJoin)
        |ELSE NEW.ceccjMasterChangeSeqNum END)
        |WHERE ceccjUid = NEW.ceccjUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_3
        |AFTER INSERT ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.ceccjMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.ceccjLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.ceccjLocalChangeSeqNum 
        |ELSE (SELECT MAX(ceccjLocalChangeSeqNum) + 1 FROM ContentEntryContentCategoryJoin) END),
        |ceccjMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(ceccjMasterChangeSeqNum) + 1 FROM ContentEntryContentCategoryJoin)
        |ELSE NEW.ceccjMasterChangeSeqNum END)
        |WHERE ceccjUid = NEW.ceccjUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_7")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_7")
                    database.execSQL("""
        |CREATE TRIGGER UPD_7
        |AFTER UPDATE ON ContentEntryParentChildJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cepcjMasterChangeSeqNum = 0 
        |OR OLD.cepcjMasterChangeSeqNum = NEW.cepcjMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.cepcjLocalChangeSeqNum = 0  
        |OR OLD.cepcjLocalChangeSeqNum = NEW.cepcjLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cepcjLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(cepcjLocalChangeSeqNum), OLD.cepcjLocalChangeSeqNum) + 1 FROM ContentEntryParentChildJoin) END),
        |cepcjMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(cepcjMasterChangeSeqNum), OLD.cepcjMasterChangeSeqNum) + 1 FROM ContentEntryParentChildJoin)
        |ELSE NEW.cepcjMasterChangeSeqNum END)
        |WHERE cepcjUid = NEW.cepcjUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_7
        |AFTER INSERT ON ContentEntryParentChildJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cepcjMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.cepcjLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cepcjLocalChangeSeqNum 
        |ELSE (SELECT MAX(cepcjLocalChangeSeqNum) + 1 FROM ContentEntryParentChildJoin) END),
        |cepcjMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(cepcjMasterChangeSeqNum) + 1 FROM ContentEntryParentChildJoin)
        |ELSE NEW.cepcjMasterChangeSeqNum END)
        |WHERE cepcjUid = NEW.cepcjUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_8")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_8")
                    database.execSQL("""
        |CREATE TRIGGER UPD_8
        |AFTER UPDATE ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cerejMasterChangeSeqNum = 0 
        |OR OLD.cerejMasterChangeSeqNum = NEW.cerejMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.cerejLocalChangeSeqNum = 0  
        |OR OLD.cerejLocalChangeSeqNum = NEW.cerejLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cerejLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(cerejLocalChangeSeqNum), OLD.cerejLocalChangeSeqNum) + 1 FROM ContentEntryRelatedEntryJoin) END),
        |cerejMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(cerejMasterChangeSeqNum), OLD.cerejMasterChangeSeqNum) + 1 FROM ContentEntryRelatedEntryJoin)
        |ELSE NEW.cerejMasterChangeSeqNum END)
        |WHERE cerejUid = NEW.cerejUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_8
        |AFTER INSERT ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cerejMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.cerejLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cerejLocalChangeSeqNum 
        |ELSE (SELECT MAX(cerejLocalChangeSeqNum) + 1 FROM ContentEntryRelatedEntryJoin) END),
        |cerejMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(cerejMasterChangeSeqNum) + 1 FROM ContentEntryRelatedEntryJoin)
        |ELSE NEW.cerejMasterChangeSeqNum END)
        |WHERE cerejUid = NEW.cerejUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_2")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_2")
                    database.execSQL("""
        |CREATE TRIGGER UPD_2
        |AFTER UPDATE ON ContentCategorySchema FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentCategorySchemaMasterChangeSeqNum = 0 
        |OR OLD.contentCategorySchemaMasterChangeSeqNum = NEW.contentCategorySchemaMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.contentCategorySchemaLocalChangeSeqNum = 0  
        |OR OLD.contentCategorySchemaLocalChangeSeqNum = NEW.contentCategorySchemaLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategorySchemaLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(contentCategorySchemaLocalChangeSeqNum), OLD.contentCategorySchemaLocalChangeSeqNum) + 1 FROM ContentCategorySchema) END),
        |contentCategorySchemaMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(contentCategorySchemaMasterChangeSeqNum), OLD.contentCategorySchemaMasterChangeSeqNum) + 1 FROM ContentCategorySchema)
        |ELSE NEW.contentCategorySchemaMasterChangeSeqNum END)
        |WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_2
        |AFTER INSERT ON ContentCategorySchema FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentCategorySchemaMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.contentCategorySchemaLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategorySchemaLocalChangeSeqNum 
        |ELSE (SELECT MAX(contentCategorySchemaLocalChangeSeqNum) + 1 FROM ContentCategorySchema) END),
        |contentCategorySchemaMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(contentCategorySchemaMasterChangeSeqNum) + 1 FROM ContentCategorySchema)
        |ELSE NEW.contentCategorySchemaMasterChangeSeqNum END)
        |WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_1")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_1")
                    database.execSQL("""
        |CREATE TRIGGER UPD_1
        |AFTER UPDATE ON ContentCategory FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentCategoryMasterChangeSeqNum = 0 
        |OR OLD.contentCategoryMasterChangeSeqNum = NEW.contentCategoryMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.contentCategoryLocalChangeSeqNum = 0  
        |OR OLD.contentCategoryLocalChangeSeqNum = NEW.contentCategoryLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategoryLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(contentCategoryLocalChangeSeqNum), OLD.contentCategoryLocalChangeSeqNum) + 1 FROM ContentCategory) END),
        |contentCategoryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(contentCategoryMasterChangeSeqNum), OLD.contentCategoryMasterChangeSeqNum) + 1 FROM ContentCategory)
        |ELSE NEW.contentCategoryMasterChangeSeqNum END)
        |WHERE contentCategoryUid = NEW.contentCategoryUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_1
        |AFTER INSERT ON ContentCategory FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.contentCategoryMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.contentCategoryLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategoryLocalChangeSeqNum 
        |ELSE (SELECT MAX(contentCategoryLocalChangeSeqNum) + 1 FROM ContentCategory) END),
        |contentCategoryMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(contentCategoryMasterChangeSeqNum) + 1 FROM ContentCategory)
        |ELSE NEW.contentCategoryMasterChangeSeqNum END)
        |WHERE contentCategoryUid = NEW.contentCategoryUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_13")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_13")
                    database.execSQL("""
        |CREATE TRIGGER UPD_13
        |AFTER UPDATE ON Language FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.langMasterChangeSeqNum = 0 
        |OR OLD.langMasterChangeSeqNum = NEW.langMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.langLocalChangeSeqNum = 0  
        |OR OLD.langLocalChangeSeqNum = NEW.langLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE Language SET langLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(langLocalChangeSeqNum), OLD.langLocalChangeSeqNum) + 1 FROM Language) END),
        |langMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(langMasterChangeSeqNum), OLD.langMasterChangeSeqNum) + 1 FROM Language)
        |ELSE NEW.langMasterChangeSeqNum END)
        |WHERE langUid = NEW.langUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_13
        |AFTER INSERT ON Language FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.langMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.langLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Language SET langLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langLocalChangeSeqNum 
        |ELSE (SELECT MAX(langLocalChangeSeqNum) + 1 FROM Language) END),
        |langMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(langMasterChangeSeqNum) + 1 FROM Language)
        |ELSE NEW.langMasterChangeSeqNum END)
        |WHERE langUid = NEW.langUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_10")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_10")
                    database.execSQL("""
        |CREATE TRIGGER UPD_10
        |AFTER UPDATE ON LanguageVariant FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.langVariantMasterChangeSeqNum = 0 
        |OR OLD.langVariantMasterChangeSeqNum = NEW.langVariantMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.langVariantLocalChangeSeqNum = 0  
        |OR OLD.langVariantLocalChangeSeqNum = NEW.langVariantLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langVariantLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(langVariantLocalChangeSeqNum), OLD.langVariantLocalChangeSeqNum) + 1 FROM LanguageVariant) END),
        |langVariantMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(langVariantMasterChangeSeqNum), OLD.langVariantMasterChangeSeqNum) + 1 FROM LanguageVariant)
        |ELSE NEW.langVariantMasterChangeSeqNum END)
        |WHERE langVariantUid = NEW.langVariantUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_10
        |AFTER INSERT ON LanguageVariant FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.langVariantMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.langVariantLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langVariantLocalChangeSeqNum 
        |ELSE (SELECT MAX(langVariantLocalChangeSeqNum) + 1 FROM LanguageVariant) END),
        |langVariantMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(langVariantMasterChangeSeqNum) + 1 FROM LanguageVariant)
        |ELSE NEW.langVariantMasterChangeSeqNum END)
        |WHERE langVariantUid = NEW.langVariantUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_45")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_45")
                    database.execSQL("""
        |CREATE TRIGGER UPD_45
        |AFTER UPDATE ON Role FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.roleMasterCsn = 0 
        |OR OLD.roleMasterCsn = NEW.roleMasterCsn
        |)
        |ELSE
        |(NEW.roleLocalCsn = 0  
        |OR OLD.roleLocalCsn = NEW.roleLocalCsn
        |) END)
        |BEGIN 
        |UPDATE Role SET roleLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.roleLocalCsn 
        |ELSE (SELECT MAX(MAX(roleLocalCsn), OLD.roleLocalCsn) + 1 FROM Role) END),
        |roleMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(roleMasterCsn), OLD.roleMasterCsn) + 1 FROM Role)
        |ELSE NEW.roleMasterCsn END)
        |WHERE roleUid = NEW.roleUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_45
        |AFTER INSERT ON Role FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.roleMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.roleLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Role SET roleLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.roleLocalCsn 
        |ELSE (SELECT MAX(roleLocalCsn) + 1 FROM Role) END),
        |roleMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(roleMasterCsn) + 1 FROM Role)
        |ELSE NEW.roleMasterCsn END)
        |WHERE roleUid = NEW.roleUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_47")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_47")
                    database.execSQL("""
        |CREATE TRIGGER UPD_47
        |AFTER UPDATE ON EntityRole FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.erMasterCsn = 0 
        |OR OLD.erMasterCsn = NEW.erMasterCsn
        |)
        |ELSE
        |(NEW.erLocalCsn = 0  
        |OR OLD.erLocalCsn = NEW.erLocalCsn
        |) END)
        |BEGIN 
        |UPDATE EntityRole SET erLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.erLocalCsn 
        |ELSE (SELECT MAX(MAX(erLocalCsn), OLD.erLocalCsn) + 1 FROM EntityRole) END),
        |erMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(erMasterCsn), OLD.erMasterCsn) + 1 FROM EntityRole)
        |ELSE NEW.erMasterCsn END)
        |WHERE erUid = NEW.erUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_47
        |AFTER INSERT ON EntityRole FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.erMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.erLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE EntityRole SET erLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.erLocalCsn 
        |ELSE (SELECT MAX(erLocalCsn) + 1 FROM EntityRole) END),
        |erMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(erMasterCsn) + 1 FROM EntityRole)
        |ELSE NEW.erMasterCsn END)
        |WHERE erUid = NEW.erUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_43")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_43")
                    database.execSQL("""
        |CREATE TRIGGER UPD_43
        |AFTER UPDATE ON PersonGroup FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.groupMasterCsn = 0 
        |OR OLD.groupMasterCsn = NEW.groupMasterCsn
        |)
        |ELSE
        |(NEW.groupLocalCsn = 0  
        |OR OLD.groupLocalCsn = NEW.groupLocalCsn
        |) END)
        |BEGIN 
        |UPDATE PersonGroup SET groupLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupLocalCsn 
        |ELSE (SELECT MAX(MAX(groupLocalCsn), OLD.groupLocalCsn) + 1 FROM PersonGroup) END),
        |groupMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(groupMasterCsn), OLD.groupMasterCsn) + 1 FROM PersonGroup)
        |ELSE NEW.groupMasterCsn END)
        |WHERE groupUid = NEW.groupUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_43
        |AFTER INSERT ON PersonGroup FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.groupMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.groupLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonGroup SET groupLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupLocalCsn 
        |ELSE (SELECT MAX(groupLocalCsn) + 1 FROM PersonGroup) END),
        |groupMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(groupMasterCsn) + 1 FROM PersonGroup)
        |ELSE NEW.groupMasterCsn END)
        |WHERE groupUid = NEW.groupUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_44")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_44")
                    database.execSQL("""
        |CREATE TRIGGER UPD_44
        |AFTER UPDATE ON PersonGroupMember FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.groupMemberMasterCsn = 0 
        |OR OLD.groupMemberMasterCsn = NEW.groupMemberMasterCsn
        |)
        |ELSE
        |(NEW.groupMemberLocalCsn = 0  
        |OR OLD.groupMemberLocalCsn = NEW.groupMemberLocalCsn
        |) END)
        |BEGIN 
        |UPDATE PersonGroupMember SET groupMemberLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupMemberLocalCsn 
        |ELSE (SELECT MAX(MAX(groupMemberLocalCsn), OLD.groupMemberLocalCsn) + 1 FROM PersonGroupMember) END),
        |groupMemberMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(groupMemberMasterCsn), OLD.groupMemberMasterCsn) + 1 FROM PersonGroupMember)
        |ELSE NEW.groupMemberMasterCsn END)
        |WHERE groupMemberUid = NEW.groupMemberUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_44
        |AFTER INSERT ON PersonGroupMember FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.groupMemberMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.groupMemberLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonGroupMember SET groupMemberLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupMemberLocalCsn 
        |ELSE (SELECT MAX(groupMemberLocalCsn) + 1 FROM PersonGroupMember) END),
        |groupMemberMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(groupMemberMasterCsn) + 1 FROM PersonGroupMember)
        |ELSE NEW.groupMemberMasterCsn END)
        |WHERE groupMemberUid = NEW.groupMemberUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_29")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_29")
                    database.execSQL("""
        |CREATE TRIGGER UPD_29
        |AFTER UPDATE ON Location FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.locationMasterChangeSeqNum = 0 
        |OR OLD.locationMasterChangeSeqNum = NEW.locationMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.locationLocalChangeSeqNum = 0  
        |OR OLD.locationLocalChangeSeqNum = NEW.locationLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE Location SET locationLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.locationLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(locationLocalChangeSeqNum), OLD.locationLocalChangeSeqNum) + 1 FROM Location) END),
        |locationMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(locationMasterChangeSeqNum), OLD.locationMasterChangeSeqNum) + 1 FROM Location)
        |ELSE NEW.locationMasterChangeSeqNum END)
        |WHERE locationUid = NEW.locationUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_29
        |AFTER INSERT ON Location FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.locationMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.locationLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Location SET locationLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.locationLocalChangeSeqNum 
        |ELSE (SELECT MAX(locationLocalChangeSeqNum) + 1 FROM Location) END),
        |locationMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(locationMasterChangeSeqNum) + 1 FROM Location)
        |ELSE NEW.locationMasterChangeSeqNum END)
        |WHERE locationUid = NEW.locationUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_48")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_48")
                    database.execSQL("""
        |CREATE TRIGGER UPD_48
        |AFTER UPDATE ON PersonLocationJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.plMasterCsn = 0 
        |OR OLD.plMasterCsn = NEW.plMasterCsn
        |)
        |ELSE
        |(NEW.plLocalCsn = 0  
        |OR OLD.plLocalCsn = NEW.plLocalCsn
        |) END)
        |BEGIN 
        |UPDATE PersonLocationJoin SET plLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.plLocalCsn 
        |ELSE (SELECT MAX(MAX(plLocalCsn), OLD.plLocalCsn) + 1 FROM PersonLocationJoin) END),
        |plMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(plMasterCsn), OLD.plMasterCsn) + 1 FROM PersonLocationJoin)
        |ELSE NEW.plMasterCsn END)
        |WHERE personLocationUid = NEW.personLocationUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_48
        |AFTER INSERT ON PersonLocationJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.plMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.plLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonLocationJoin SET plLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.plLocalCsn 
        |ELSE (SELECT MAX(plLocalCsn) + 1 FROM PersonLocationJoin) END),
        |plMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(plMasterCsn) + 1 FROM PersonLocationJoin)
        |ELSE NEW.plMasterCsn END)
        |WHERE personLocationUid = NEW.personLocationUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_50")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_50")
                    database.execSQL("""
        |CREATE TRIGGER UPD_50
        |AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personPictureMasterCsn = 0 
        |OR OLD.personPictureMasterCsn = NEW.personPictureMasterCsn
        |)
        |ELSE
        |(NEW.personPictureLocalCsn = 0  
        |OR OLD.personPictureLocalCsn = NEW.personPictureLocalCsn
        |) END)
        |BEGIN 
        |UPDATE PersonPicture SET personPictureLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personPictureLocalCsn 
        |ELSE (SELECT MAX(MAX(personPictureLocalCsn), OLD.personPictureLocalCsn) + 1 FROM PersonPicture) END),
        |personPictureMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(personPictureMasterCsn), OLD.personPictureMasterCsn) + 1 FROM PersonPicture)
        |ELSE NEW.personPictureMasterCsn END)
        |WHERE personPictureUid = NEW.personPictureUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_50
        |AFTER INSERT ON PersonPicture FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.personPictureMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.personPictureLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE PersonPicture SET personPictureLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personPictureLocalCsn 
        |ELSE (SELECT MAX(personPictureLocalCsn) + 1 FROM PersonPicture) END),
        |personPictureMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(personPictureMasterCsn) + 1 FROM PersonPicture)
        |ELSE NEW.personPictureMasterCsn END)
        |WHERE personPictureUid = NEW.personPictureUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_51")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_51")
                    database.execSQL("""
        |CREATE TRIGGER UPD_51
        |AFTER UPDATE ON Container FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cntMasterCsn = 0 
        |OR OLD.cntMasterCsn = NEW.cntMasterCsn
        |)
        |ELSE
        |(NEW.cntLocalCsn = 0  
        |OR OLD.cntLocalCsn = NEW.cntLocalCsn
        |) END)
        |BEGIN 
        |UPDATE Container SET cntLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cntLocalCsn 
        |ELSE (SELECT MAX(MAX(cntLocalCsn), OLD.cntLocalCsn) + 1 FROM Container) END),
        |cntMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(cntMasterCsn), OLD.cntMasterCsn) + 1 FROM Container)
        |ELSE NEW.cntMasterCsn END)
        |WHERE containerUid = NEW.containerUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_51
        |AFTER INSERT ON Container FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.cntMasterCsn = 0 
        |
        |)
        |ELSE
        |(NEW.cntLocalCsn = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE Container SET cntLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cntLocalCsn 
        |ELSE (SELECT MAX(cntLocalCsn) + 1 FROM Container) END),
        |cntMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(cntMasterCsn) + 1 FROM Container)
        |ELSE NEW.cntMasterCsn END)
        |WHERE containerUid = NEW.containerUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_62")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_62")
                    database.execSQL("""
        |CREATE TRIGGER UPD_62
        |AFTER UPDATE ON VerbEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.verbMasterChangeSeqNum = 0 
        |OR OLD.verbMasterChangeSeqNum = NEW.verbMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.verbLocalChangeSeqNum = 0  
        |OR OLD.verbLocalChangeSeqNum = NEW.verbLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE VerbEntity SET verbLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(verbLocalChangeSeqNum), OLD.verbLocalChangeSeqNum) + 1 FROM VerbEntity) END),
        |verbMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(verbMasterChangeSeqNum), OLD.verbMasterChangeSeqNum) + 1 FROM VerbEntity)
        |ELSE NEW.verbMasterChangeSeqNum END)
        |WHERE verbUid = NEW.verbUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_62
        |AFTER INSERT ON VerbEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.verbMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.verbLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE VerbEntity SET verbLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
        |ELSE (SELECT MAX(verbLocalChangeSeqNum) + 1 FROM VerbEntity) END),
        |verbMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(verbMasterChangeSeqNum) + 1 FROM VerbEntity)
        |ELSE NEW.verbMasterChangeSeqNum END)
        |WHERE verbUid = NEW.verbUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_64")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_64")
                    database.execSQL("""
        |CREATE TRIGGER UPD_64
        |AFTER UPDATE ON XObjectEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.xObjectMasterChangeSeqNum = 0 
        |OR OLD.xObjectMasterChangeSeqNum = NEW.xObjectMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.xObjectocalChangeSeqNum = 0  
        |OR OLD.xObjectocalChangeSeqNum = NEW.xObjectocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE XObjectEntity SET xObjectocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.xObjectocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(xObjectocalChangeSeqNum), OLD.xObjectocalChangeSeqNum) + 1 FROM XObjectEntity) END),
        |xObjectMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(xObjectMasterChangeSeqNum), OLD.xObjectMasterChangeSeqNum) + 1 FROM XObjectEntity)
        |ELSE NEW.xObjectMasterChangeSeqNum END)
        |WHERE xObjectUid = NEW.xObjectUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_64
        |AFTER INSERT ON XObjectEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.xObjectMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.xObjectocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE XObjectEntity SET xObjectocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.xObjectocalChangeSeqNum 
        |ELSE (SELECT MAX(xObjectocalChangeSeqNum) + 1 FROM XObjectEntity) END),
        |xObjectMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(xObjectMasterChangeSeqNum) + 1 FROM XObjectEntity)
        |ELSE NEW.xObjectMasterChangeSeqNum END)
        |WHERE xObjectUid = NEW.xObjectUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_60")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_60")
                    database.execSQL("""
        |CREATE TRIGGER UPD_60
        |AFTER UPDATE ON StatementEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.statementMasterChangeSeqNum = 0 
        |OR OLD.statementMasterChangeSeqNum = NEW.statementMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.statementLocalChangeSeqNum = 0  
        |OR OLD.statementLocalChangeSeqNum = NEW.statementLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE StatementEntity SET statementLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(statementLocalChangeSeqNum), OLD.statementLocalChangeSeqNum) + 1 FROM StatementEntity) END),
        |statementMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(statementMasterChangeSeqNum), OLD.statementMasterChangeSeqNum) + 1 FROM StatementEntity)
        |ELSE NEW.statementMasterChangeSeqNum END)
        |WHERE statementUid = NEW.statementUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_60
        |AFTER INSERT ON StatementEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.statementMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.statementLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE StatementEntity SET statementLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
        |ELSE (SELECT MAX(statementLocalChangeSeqNum) + 1 FROM StatementEntity) END),
        |statementMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(statementMasterChangeSeqNum) + 1 FROM StatementEntity)
        |ELSE NEW.statementMasterChangeSeqNum END)
        |WHERE statementUid = NEW.statementUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_66")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_66")
                    database.execSQL("""
        |CREATE TRIGGER UPD_66
        |AFTER UPDATE ON ContextXObjectStatementJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.verbMasterChangeSeqNum = 0 
        |OR OLD.verbMasterChangeSeqNum = NEW.verbMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.verbLocalChangeSeqNum = 0  
        |OR OLD.verbLocalChangeSeqNum = NEW.verbLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(verbLocalChangeSeqNum), OLD.verbLocalChangeSeqNum) + 1 FROM ContextXObjectStatementJoin) END),
        |verbMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(verbMasterChangeSeqNum), OLD.verbMasterChangeSeqNum) + 1 FROM ContextXObjectStatementJoin)
        |ELSE NEW.verbMasterChangeSeqNum END)
        |WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_66
        |AFTER INSERT ON ContextXObjectStatementJoin FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.verbMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.verbLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
        |ELSE (SELECT MAX(verbLocalChangeSeqNum) + 1 FROM ContextXObjectStatementJoin) END),
        |verbMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(verbMasterChangeSeqNum) + 1 FROM ContextXObjectStatementJoin)
        |ELSE NEW.verbMasterChangeSeqNum END)
        |WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_68")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_68")
                    database.execSQL("""
        |CREATE TRIGGER UPD_68
        |AFTER UPDATE ON AgentEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.statementMasterChangeSeqNum = 0 
        |OR OLD.statementMasterChangeSeqNum = NEW.statementMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.statementLocalChangeSeqNum = 0  
        |OR OLD.statementLocalChangeSeqNum = NEW.statementLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE AgentEntity SET statementLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(statementLocalChangeSeqNum), OLD.statementLocalChangeSeqNum) + 1 FROM AgentEntity) END),
        |statementMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(statementMasterChangeSeqNum), OLD.statementMasterChangeSeqNum) + 1 FROM AgentEntity)
        |ELSE NEW.statementMasterChangeSeqNum END)
        |WHERE agentUid = NEW.agentUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_68
        |AFTER INSERT ON AgentEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.statementMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.statementLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE AgentEntity SET statementLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
        |ELSE (SELECT MAX(statementLocalChangeSeqNum) + 1 FROM AgentEntity) END),
        |statementMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(statementMasterChangeSeqNum) + 1 FROM AgentEntity)
        |ELSE NEW.statementMasterChangeSeqNum END)
        |WHERE agentUid = NEW.agentUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_70")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_70")
                    database.execSQL("""
        |CREATE TRIGGER UPD_70
        |AFTER UPDATE ON StateEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.stateMasterChangeSeqNum = 0 
        |OR OLD.stateMasterChangeSeqNum = NEW.stateMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.stateLocalChangeSeqNum = 0  
        |OR OLD.stateLocalChangeSeqNum = NEW.stateLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE StateEntity SET stateLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(stateLocalChangeSeqNum), OLD.stateLocalChangeSeqNum) + 1 FROM StateEntity) END),
        |stateMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(stateMasterChangeSeqNum), OLD.stateMasterChangeSeqNum) + 1 FROM StateEntity)
        |ELSE NEW.stateMasterChangeSeqNum END)
        |WHERE stateUid = NEW.stateUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_70
        |AFTER INSERT ON StateEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.stateMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.stateLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE StateEntity SET stateLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateLocalChangeSeqNum 
        |ELSE (SELECT MAX(stateLocalChangeSeqNum) + 1 FROM StateEntity) END),
        |stateMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(stateMasterChangeSeqNum) + 1 FROM StateEntity)
        |ELSE NEW.stateMasterChangeSeqNum END)
        |WHERE stateUid = NEW.stateUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_72")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_72")
                    database.execSQL("""
        |CREATE TRIGGER UPD_72
        |AFTER UPDATE ON StateContentEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.stateContentMasterChangeSeqNum = 0 
        |OR OLD.stateContentMasterChangeSeqNum = NEW.stateContentMasterChangeSeqNum
        |)
        |ELSE
        |(NEW.stateContentLocalChangeSeqNum = 0  
        |OR OLD.stateContentLocalChangeSeqNum = NEW.stateContentLocalChangeSeqNum
        |) END)
        |BEGIN 
        |UPDATE StateContentEntity SET stateContentLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateContentLocalChangeSeqNum 
        |ELSE (SELECT MAX(MAX(stateContentLocalChangeSeqNum), OLD.stateContentLocalChangeSeqNum) + 1 FROM StateContentEntity) END),
        |stateContentMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(stateContentMasterChangeSeqNum), OLD.stateContentMasterChangeSeqNum) + 1 FROM StateContentEntity)
        |ELSE NEW.stateContentMasterChangeSeqNum END)
        |WHERE stateContentUid = NEW.stateContentUid
        |; END
        """.trimMargin())
                    database.execSQL("""
        |CREATE TRIGGER INS_72
        |AFTER INSERT ON StateContentEntity FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.stateContentMasterChangeSeqNum = 0 
        |
        |)
        |ELSE
        |(NEW.stateContentLocalChangeSeqNum = 0  
        |
        |) END)
        |BEGIN 
        |UPDATE StateContentEntity SET stateContentLocalChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateContentLocalChangeSeqNum 
        |ELSE (SELECT MAX(stateContentLocalChangeSeqNum) + 1 FROM StateContentEntity) END),
        |stateContentMasterChangeSeqNum = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(stateContentMasterChangeSeqNum) + 1 FROM StateContentEntity)
        |ELSE NEW.stateContentMasterChangeSeqNum END)
        |WHERE stateContentUid = NEW.stateContentUid
        |; END
        """.trimMargin())
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_74")
                    database.execSQL("DROP TRIGGER IF EXISTS INS_74")
                    database.execSQL("""
        |CREATE TRIGGER UPD_74
        |AFTER UPDATE ON XLangMapEntry FOR EACH ROW WHEN
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(NEW.statementLangMapMasterCsn = 0 
        |OR OLD.statementLangMapMasterCsn = NEW.statementLangMapMasterCsn
        |)
        |ELSE
        |(NEW.statementLangMapLocalCsn = 0  
        |OR OLD.statementLangMapLocalCsn = NEW.statementLangMapLocalCsn
        |) END)
        |BEGIN 
        |UPDATE XLangMapEntry SET statementLangMapLocalCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLangMapLocalCsn 
        |ELSE (SELECT MAX(MAX(statementLangMapLocalCsn), OLD.statementLangMapLocalCsn) + 1 FROM XLangMapEntry) END),
        |statementLangMapMasterCsn = 
        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
        |(SELECT MAX(MAX(statementLangMapMasterCsn), OLD.statementLangMapMasterCsn) + 1 FROM XLangMapEntry)
        |ELSE NEW.statementLangMapMasterCsn END)
        |WHERE statementLangMapUid = NEW.statementLangMapUid
        |; END
        """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER INS_74
                    |AFTER INSERT ON XLangMapEntry FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.statementLangMapMasterCsn = 0 
                    |
                    |)
                    |ELSE
                    |(NEW.statementLangMapLocalCsn = 0  
                    |
                    |) END)
                    |BEGIN 
                    |UPDATE XLangMapEntry SET statementLangMapLocalCsn = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLangMapLocalCsn 
                    |ELSE (SELECT MAX(statementLangMapLocalCsn) + 1 FROM XLangMapEntry) END),
                    |statementLangMapMasterCsn = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(statementLangMapMasterCsn) + 1 FROM XLangMapEntry)
                    |ELSE NEW.statementLangMapMasterCsn END)
                    |WHERE statementLangMapUid = NEW.statementLangMapUid
                    |; END
                    """.trimMargin())
                }
            }
        }

        val MIGRATION_30_31 = object : DoorMigration(30, 31) {
            override fun migrate(database: DoorSqlDatabase) {
                if (database.dbType() == DoorDbType.SQLITE) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContainerETag (  ceContainerUid  BIGINT  PRIMARY KEY  NOT NULL , cetag  TEXT )")
                } else if (database.dbType() == DoorDbType.POSTGRES) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContainerETag (  ceContainerUid  BIGINT  PRIMARY KEY  NOT NULL , cetag  TEXT )")
                }
            }
        }

        val MIGRATION_31_32 = object : DoorMigration(31, 32) {
            override fun migrate(database: DoorSqlDatabase) {
                if (database.dbType() == DoorDbType.SQLITE) {
                    database.execSQL("""CREATE TABLE IF NOT EXISTS SyncResult (  
                        |tableId  INTEGER NOT NULL, status  INTEGER NOT NULL, localCsn  INTEGER NOT NULL, 
                        |remoteCsn  INTEGER NOT NULL, syncType  INTEGER NOT NULL, timestamp  INTEGER NOT NULL, 
                        |sent  INTEGER NOT NULL, received  INTEGER NOT NULL, 
                        |srUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""".trimMargin())
                } else if (database.dbType() == DoorDbType.POSTGRES) {
                    database.execSQL("""CREATE TABLE IF NOT EXISTS SyncResult (
                        |  tableId  INTEGER , status  INTEGER , localCsn  INTEGER , 
                        |  remoteCsn  INTEGER , syncType  INTEGER , 
                        |  timestamp  BIGINT , sent  INTEGER , received  INTEGER , 
                        |  srUid  SERIAL  PRIMARY KEY  NOT NULL )""".trimMargin())
                }
            }
        }

        /**
         * Add fields required for class and school codes for students to join a class or school
         */
        val MIGRATION_36_37 = object : DoorMigration(36, 37) {
            override fun migrate(database: DoorSqlDatabase) {

                if (database.dbType() == DoorDbType.SQLITE) {

                    database.execSQL("ALTER TABLE ClazzLog RENAME to ClazzLog_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog (  clazzLogClazzUid  BIGINT , logDate  BIGINT , timeRecorded  BIGINT , clazzLogDone  BOOL , cancellationNote  TEXT , clazzLogCancelled  BOOL , clazzLogNumPresent  INTEGER , clazzLogNumAbsent  INTEGER , clazzLogNumPartial  INTEGER , clazzLogScheduleUid  BIGINT , clazzLogStatusFlag  INTEGER , clazzLogMSQN  BIGINT , clazzLogLCSN  BIGINT , clazzLogLCB  INTEGER , clazzLogUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("INSERT INTO ClazzLog (clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, cancellationNote, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogStatusFlag, clazzLogMSQN, clazzLogLCSN, clazzLogLCB) SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, 0, '', 0, 0, 0, 0, 0, 0, 0, 0, 0 FROM ClazzLog_OLD")
                    database.execSQL("DROP TABLE ClazzLog_OLD")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_14
                          |AFTER UPDATE ON ClazzLog FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzLogMSQN = 0 
                          |OR OLD.clazzLogMSQN = NEW.clazzLogMSQN
                          |)
                          |ELSE
                          |(NEW.clazzLogLCSN = 0  
                          |OR OLD.clazzLogLCSN = NEW.clazzLogLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzLog SET clazzLogLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
                          |ELSE (SELECT MAX(MAX(clazzLogLCSN), OLD.clazzLogLCSN) + 1 FROM ClazzLog) END),
                          |clazzLogMSQN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzLogMSQN), OLD.clazzLogMSQN) + 1 FROM ClazzLog)
                          |ELSE NEW.clazzLogMSQN END)
                          |WHERE clazzLogUid = NEW.clazzLogUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_14
                          |AFTER INSERT ON ClazzLog FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzLogMSQN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzLogLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzLog SET clazzLogLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
                          |ELSE (SELECT MAX(clazzLogLCSN) + 1 FROM ClazzLog) END),
                          |clazzLogMSQN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzLogMSQN) + 1 FROM ClazzLog)
                          |ELSE NEW.clazzLogMSQN END)
                          |WHERE clazzLogUid = NEW.clazzLogUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzLog_trk_clientId_epk_rx_csn 
                          |ON ClazzLog_trk (clientId, epk, rx, csn)
                          """.trimMargin())




                    database.execSQL("ALTER TABLE ClazzLogAttendanceRecord RENAME to ClazzLogAttendanceRecord_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord (  clazzLogAttendanceRecordClazzLogUid  BIGINT , clazzLogAttendanceRecordClazzMemberUid  BIGINT , attendanceStatus  INTEGER , clazzLogAttendanceRecordMasterChangeSeqNum  BIGINT , clazzLogAttendanceRecordLocalChangeSeqNum  BIGINT , clazzLogAttendanceRecordLastChangedBy  INTEGER , clazzLogAttendanceRecordUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("INSERT INTO ClazzLogAttendanceRecord (clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy) SELECT clazzLogAttendanceRecordUid, clazzLogClazzLogUid, studentClazzMemberUid, attendanceStatus, 0, 0, 0 FROM ClazzLogAttendanceRecord_OLD")
                    database.execSQL("DROP TABLE ClazzLogAttendanceRecord_OLD")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_15
                          |AFTER UPDATE ON ClazzLogAttendanceRecord FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0 
                          |OR OLD.clazzLogAttendanceRecordMasterChangeSeqNum = NEW.clazzLogAttendanceRecordMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0  
                          |OR OLD.clazzLogAttendanceRecordLocalChangeSeqNum = NEW.clazzLogAttendanceRecordLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(clazzLogAttendanceRecordLocalChangeSeqNum), OLD.clazzLogAttendanceRecordLocalChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord) END),
                          |clazzLogAttendanceRecordMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzLogAttendanceRecordMasterChangeSeqNum), OLD.clazzLogAttendanceRecordMasterChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord)
                          |ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
                          |WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_15
                          |AFTER INSERT ON ClazzLogAttendanceRecord FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
                          |ELSE (SELECT MAX(clazzLogAttendanceRecordLocalChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord) END),
                          |clazzLogAttendanceRecordMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzLogAttendanceRecordMasterChangeSeqNum) + 1 FROM ClazzLogAttendanceRecord)
                          |ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
                          |WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn 
                          |ON ClazzLogAttendanceRecord_trk (clientId, epk, rx, csn)
                          """.trimMargin())

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWork (  clazzWorkCreatorPersonUid  BIGINT , clazzWorkClazzUid  BIGINT , clazzWorkTitle  TEXT , clazzWorkCreatedDate  BIGINT , clazzWorkStartDateTime  BIGINT , clazzWorkStartTime  BIGINT , clazzWorkDueTime  BIGINT , clazzWorkDueDateTime  BIGINT , clazzWorkSubmissionType  INTEGER , clazzWorkCommentsEnabled  BOOL , clazzWorkMaximumScore  INTEGER , clazzWorkInstructions  TEXT , clazzWorkActive  BOOL , clazzWorkLocalChangeSeqNum  BIGINT , clazzWorkMasterChangeSeqNum  BIGINT , clazzWorkLastChangedBy  INTEGER , clazzWorkUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_201
                          |AFTER UPDATE ON ClazzWork FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkMasterChangeSeqNum = 0 
                          |OR OLD.clazzWorkMasterChangeSeqNum = NEW.clazzWorkMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.clazzWorkLocalChangeSeqNum = 0  
                          |OR OLD.clazzWorkLocalChangeSeqNum = NEW.clazzWorkLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWork SET clazzWorkLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(clazzWorkLocalChangeSeqNum), OLD.clazzWorkLocalChangeSeqNum) + 1 FROM ClazzWork) END),
                          |clazzWorkMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkMasterChangeSeqNum), OLD.clazzWorkMasterChangeSeqNum) + 1 FROM ClazzWork)
                          |ELSE NEW.clazzWorkMasterChangeSeqNum END)
                          |WHERE clazzWorkUid = NEW.clazzWorkUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_201
                          |AFTER INSERT ON ClazzWork FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWork SET clazzWorkLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkLocalChangeSeqNum 
                          |ELSE (SELECT MAX(clazzWorkLocalChangeSeqNum) + 1 FROM ClazzWork) END),
                          |clazzWorkMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkMasterChangeSeqNum) + 1 FROM ClazzWork)
                          |ELSE NEW.clazzWorkMasterChangeSeqNum END)
                          |WHERE clazzWorkUid = NEW.clazzWorkUid
                          |; END
          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWork_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWork_trk_clientId_epk_rx_csn 
                          |ON ClazzWork_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWork for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestion (  clazzWorkQuestionText  TEXT , clazzWorkQuestionClazzWorkUid  BIGINT , clazzWorkQuestionIndex  INTEGER , clazzWorkQuestionType  INTEGER , clazzWorkQuestionActive  BOOL , clazzWorkQuestionMCSN  BIGINT , clazzWorkQuestionLCSN  BIGINT , clazzWorkQuestionLCB  INTEGER , clazzWorkQuestionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_202
                          |AFTER UPDATE ON ClazzWorkQuestion FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionMCSN = 0 
                          |OR OLD.clazzWorkQuestionMCSN = NEW.clazzWorkQuestionMCSN
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionLCSN = 0  
                          |OR OLD.clazzWorkQuestionLCSN = NEW.clazzWorkQuestionLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestion SET clazzWorkQuestionLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionLCSN 
                          |ELSE (SELECT MAX(MAX(clazzWorkQuestionLCSN), OLD.clazzWorkQuestionLCSN) + 1 FROM ClazzWorkQuestion) END),
                          |clazzWorkQuestionMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkQuestionMCSN), OLD.clazzWorkQuestionMCSN) + 1 FROM ClazzWorkQuestion)
                          |ELSE NEW.clazzWorkQuestionMCSN END)
                          |WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_202
                          |AFTER INSERT ON ClazzWorkQuestion FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionMCSN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestion SET clazzWorkQuestionLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionLCSN 
                          |ELSE (SELECT MAX(clazzWorkQuestionLCSN) + 1 FROM ClazzWorkQuestion) END),
                          |clazzWorkQuestionMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkQuestionMCSN) + 1 FROM ClazzWorkQuestion)
                          |ELSE NEW.clazzWorkQuestionMCSN END)
                          |WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestion_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestion_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestion_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestion for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionOption (  clazzWorkQuestionOptionText  TEXT , clazzWorkQuestionOptionQuestionUid  BIGINT , clazzWorkQuestionOptionMasterChangeSeqNum  BIGINT , clazzWorkQuestionOptionLocalChangeSeqNum  BIGINT , clazzWorkQuestionOptionLastChangedBy  INTEGER , clazzWorkQuestionOptionActive  BOOL , clazzWorkQuestionOptionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_203
                          |AFTER UPDATE ON ClazzWorkQuestionOption FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionOptionMasterChangeSeqNum = 0 
                          |OR OLD.clazzWorkQuestionOptionMasterChangeSeqNum = NEW.clazzWorkQuestionOptionMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionOptionLocalChangeSeqNum = 0  
                          |OR OLD.clazzWorkQuestionOptionLocalChangeSeqNum = NEW.clazzWorkQuestionOptionLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionOptionLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(clazzWorkQuestionOptionLocalChangeSeqNum), OLD.clazzWorkQuestionOptionLocalChangeSeqNum) + 1 FROM ClazzWorkQuestionOption) END),
                          |clazzWorkQuestionOptionMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkQuestionOptionMasterChangeSeqNum), OLD.clazzWorkQuestionOptionMasterChangeSeqNum) + 1 FROM ClazzWorkQuestionOption)
                          |ELSE NEW.clazzWorkQuestionOptionMasterChangeSeqNum END)
                          |WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_203
                          |AFTER INSERT ON ClazzWorkQuestionOption FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionOptionMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionOptionLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionOptionLocalChangeSeqNum 
                          |ELSE (SELECT MAX(clazzWorkQuestionOptionLocalChangeSeqNum) + 1 FROM ClazzWorkQuestionOption) END),
                          |clazzWorkQuestionOptionMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkQuestionOptionMasterChangeSeqNum) + 1 FROM ClazzWorkQuestionOption)
                          |ELSE NEW.clazzWorkQuestionOptionMasterChangeSeqNum END)
                          |WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid
                          |; END
          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionOption_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestionOption_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestionOption_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestionOption for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse (  clazzWorkQuestionResponseClazzWorkUid  BIGINT , clazzWorkQuestionResponseQuestionUid  BIGINT , clazzWorkQuestionResponseText  TEXT , clazzWorkQuestionResponseOptionSelected  BIGINT , clazzWorkQuestionResponsePersonUid  BIGINT , clazzWorkQuestionResponseClazzMemberUid  BIGINT , clazzWorkQuestionResponseInactive  BOOL , clazzWorkQuestionResponseDateResponded  BIGINT , clazzWorkQuestionResponseMCSN  BIGINT , clazzWorkQuestionResponseLCSN  BIGINT , clazzWorkQuestionResponseLCB  INTEGER , clazzWorkQuestionResponseUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_209
                          |AFTER UPDATE ON ClazzWorkQuestionResponse FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionResponseMCSN = 0 
                          |OR OLD.clazzWorkQuestionResponseMCSN = NEW.clazzWorkQuestionResponseMCSN
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionResponseLCSN = 0  
                          |OR OLD.clazzWorkQuestionResponseLCSN = NEW.clazzWorkQuestionResponseLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestionResponse SET clazzWorkQuestionResponseLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionResponseLCSN 
                          |ELSE (SELECT MAX(MAX(clazzWorkQuestionResponseLCSN), OLD.clazzWorkQuestionResponseLCSN) + 1 FROM ClazzWorkQuestionResponse) END),
                          |clazzWorkQuestionResponseMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkQuestionResponseMCSN), OLD.clazzWorkQuestionResponseMCSN) + 1 FROM ClazzWorkQuestionResponse)
                          |ELSE NEW.clazzWorkQuestionResponseMCSN END)
                          |WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_209
                          |AFTER INSERT ON ClazzWorkQuestionResponse FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkQuestionResponseMCSN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkQuestionResponseLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkQuestionResponse SET clazzWorkQuestionResponseLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionResponseLCSN 
                          |ELSE (SELECT MAX(clazzWorkQuestionResponseLCSN) + 1 FROM ClazzWorkQuestionResponse) END),
                          |clazzWorkQuestionResponseMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkQuestionResponseMCSN) + 1 FROM ClazzWorkQuestionResponse)
                          |ELSE NEW.clazzWorkQuestionResponseMCSN END)
                          |WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestionResponse_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestionResponse_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestionResponse for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission (  clazzWorkSubmissionClazzWorkUid  BIGINT , clazzWorkSubmissionClazzMemberUid  BIGINT , clazzWorkSubmissionMarkerClazzMemberUid  BIGINT , clazzWorkSubmissionMarkerPersonUid  BIGINT , clazzWorkSubmissionPersonUid  BIGINT , clazzWorkSubmissionInactive  BOOL , clazzWorkSubmissionDateTimeStarted  BIGINT , clazzWorkSubmissionDateTimeUpdated  BIGINT , clazzWorkSubmissionDateTimeFinished  BIGINT , clazzWorkSubmissionDateTimeMarked  BIGINT , clazzWorkSubmissionText  TEXT , clazzWorkSubmissionScore  INTEGER , clazzWorkSubmissionMCSN  BIGINT , clazzWorkSubmissionLCSN  BIGINT , clazzWorkSubmissionLCB  INTEGER , clazzWorkSubmissionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_206
                          |AFTER UPDATE ON ClazzWorkSubmission FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkSubmissionMCSN = 0 
                          |OR OLD.clazzWorkSubmissionMCSN = NEW.clazzWorkSubmissionMCSN
                          |)
                          |ELSE
                          |(NEW.clazzWorkSubmissionLCSN = 0  
                          |OR OLD.clazzWorkSubmissionLCSN = NEW.clazzWorkSubmissionLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkSubmission SET clazzWorkSubmissionLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkSubmissionLCSN 
                          |ELSE (SELECT MAX(MAX(clazzWorkSubmissionLCSN), OLD.clazzWorkSubmissionLCSN) + 1 FROM ClazzWorkSubmission) END),
                          |clazzWorkSubmissionMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkSubmissionMCSN), OLD.clazzWorkSubmissionMCSN) + 1 FROM ClazzWorkSubmission)
                          |ELSE NEW.clazzWorkSubmissionMCSN END)
                          |WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_206
                          |AFTER INSERT ON ClazzWorkSubmission FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkSubmissionMCSN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkSubmissionLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkSubmission SET clazzWorkSubmissionLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkSubmissionLCSN 
                          |ELSE (SELECT MAX(clazzWorkSubmissionLCSN) + 1 FROM ClazzWorkSubmission) END),
                          |clazzWorkSubmissionMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkSubmissionMCSN) + 1 FROM ClazzWorkSubmission)
                          |ELSE NEW.clazzWorkSubmissionMCSN END)
                          |WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkSubmission_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkSubmission_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkSubmission for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS Comments (  commentsText  TEXT , commentsEntityType  INTEGER , commentsEntityUid  BIGINT , commentsPublic  BOOL , commentsStatus  INTEGER , commentsPersonUid  BIGINT , commentsToPersonUid  BIGINT , commentsFlagged  BOOL , commentsInActive  BOOL , commentsDateTimeAdded  BIGINT , commentsDateTimeUpdated  BIGINT , commentsMCSN  BIGINT , commentsLCSN  BIGINT , commentsLCB  INTEGER , commentsUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_208
                          |AFTER UPDATE ON Comments FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.commentsMCSN = 0 
                          |OR OLD.commentsMCSN = NEW.commentsMCSN
                          |)
                          |ELSE
                          |(NEW.commentsLCSN = 0  
                          |OR OLD.commentsLCSN = NEW.commentsLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE Comments SET commentsLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.commentsLCSN 
                          |ELSE (SELECT MAX(MAX(commentsLCSN), OLD.commentsLCSN) + 1 FROM Comments) END),
                          |commentsMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(commentsMCSN), OLD.commentsMCSN) + 1 FROM Comments)
                          |ELSE NEW.commentsMCSN END)
                          |WHERE commentsUid = NEW.commentsUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_208
                          |AFTER INSERT ON Comments FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.commentsMCSN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.commentsLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE Comments SET commentsLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.commentsLCSN 
                          |ELSE (SELECT MAX(commentsLCSN) + 1 FROM Comments) END),
                          |commentsMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(commentsMCSN) + 1 FROM Comments)
                          |ELSE NEW.commentsMCSN END)
                          |WHERE commentsUid = NEW.commentsUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Comments_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Comments_trk_clientId_epk_rx_csn 
                          |ON Comments_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Comments for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS ContainerUploadJob (  cujContainerUid  BIGINT , sessionId  TEXT , jobStatus  INTEGER , bytesSoFar  BIGINT , contentLength  BIGINT , containerEntryFileUids  TEXT , cujUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    //End: Create table ContainerUploadJob for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryProgress (  contentEntryProgressActive  BOOL , contentEntryProgressContentEntryUid  BIGINT , contentEntryProgressPersonUid  BIGINT , contentEntryProgressProgress  INTEGER , contentEntryProgressStatusFlag  INTEGER , contentEntryProgressLocalChangeSeqNum  BIGINT , contentEntryProgressMasterChangeSeqNum  BIGINT , contentEntryProgressLastChangedBy  INTEGER , contentEntryProgressUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_210
                          |AFTER UPDATE ON ContentEntryProgress FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.contentEntryProgressMasterChangeSeqNum = 0 
                          |OR OLD.contentEntryProgressMasterChangeSeqNum = NEW.contentEntryProgressMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.contentEntryProgressLocalChangeSeqNum = 0  
                          |OR OLD.contentEntryProgressLocalChangeSeqNum = NEW.contentEntryProgressLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE ContentEntryProgress SET contentEntryProgressLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryProgressLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(contentEntryProgressLocalChangeSeqNum), OLD.contentEntryProgressLocalChangeSeqNum) + 1 FROM ContentEntryProgress) END),
                          |contentEntryProgressMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(contentEntryProgressMasterChangeSeqNum), OLD.contentEntryProgressMasterChangeSeqNum) + 1 FROM ContentEntryProgress)
                          |ELSE NEW.contentEntryProgressMasterChangeSeqNum END)
                          |WHERE contentEntryProgressUid = NEW.contentEntryProgressUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_210
                          |AFTER INSERT ON ContentEntryProgress FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.contentEntryProgressMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.contentEntryProgressLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ContentEntryProgress SET contentEntryProgressLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryProgressLocalChangeSeqNum 
                          |ELSE (SELECT MAX(contentEntryProgressLocalChangeSeqNum) + 1 FROM ContentEntryProgress) END),
                          |contentEntryProgressMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(contentEntryProgressMasterChangeSeqNum) + 1 FROM ContentEntryProgress)
                          |ELSE NEW.contentEntryProgressMasterChangeSeqNum END)
                          |WHERE contentEntryProgressUid = NEW.contentEntryProgressUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryProgress_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ContentEntryProgress_trk_clientId_epk_rx_csn 
                          |ON ContentEntryProgress_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ContentEntryProgress for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS DeviceSession (  dsDeviceId  INTEGER , dsPersonUid  BIGINT , expires  BIGINT , deviceSessionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    //End: Create table DeviceSession for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS TimeZoneEntity (  id  TEXT  PRIMARY KEY  NOT NULL , rawOffset  INTEGER )")
                    //End: Create table TimeZoneEntity for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS Holiday (  holMasterCsn  BIGINT , holLocalCsn  BIGINT , holLastModBy  INTEGER , holActive  BOOL , holHolidayCalendarUid  BIGINT , holStartTime  BIGINT , holEndTime  BIGINT , holName  TEXT , holUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_99
                          |AFTER UPDATE ON Holiday FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.holMasterCsn = 0 
                          |OR OLD.holMasterCsn = NEW.holMasterCsn
                          |)
                          |ELSE
                          |(NEW.holLocalCsn = 0  
                          |OR OLD.holLocalCsn = NEW.holLocalCsn
                          |) END)
                          |BEGIN 
                          |UPDATE Holiday SET holLocalCsn = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.holLocalCsn 
                          |ELSE (SELECT MAX(MAX(holLocalCsn), OLD.holLocalCsn) + 1 FROM Holiday) END),
                          |holMasterCsn = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(holMasterCsn), OLD.holMasterCsn) + 1 FROM Holiday)
                          |ELSE NEW.holMasterCsn END)
                          |WHERE holUid = NEW.holUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_99
                          |AFTER INSERT ON Holiday FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.holMasterCsn = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.holLocalCsn = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE Holiday SET holLocalCsn = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.holLocalCsn 
                          |ELSE (SELECT MAX(holLocalCsn) + 1 FROM Holiday) END),
                          |holMasterCsn = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(holMasterCsn) + 1 FROM Holiday)
                          |ELSE NEW.holMasterCsn END)
                          |WHERE holUid = NEW.holUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Holiday_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Holiday_trk_clientId_epk_rx_csn 
                          |ON Holiday_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Holiday for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS HolidayCalendar (  umCalendarName  TEXT , umCalendarCategory  INTEGER , umCalendarActive  BOOL , umCalendarMasterChangeSeqNum  BIGINT , umCalendarLocalChangeSeqNum  BIGINT , umCalendarLastChangedBy  INTEGER , umCalendarUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_28
                          |AFTER UPDATE ON HolidayCalendar FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.umCalendarMasterChangeSeqNum = 0 
                          |OR OLD.umCalendarMasterChangeSeqNum = NEW.umCalendarMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.umCalendarLocalChangeSeqNum = 0  
                          |OR OLD.umCalendarLocalChangeSeqNum = NEW.umCalendarLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE HolidayCalendar SET umCalendarLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(umCalendarLocalChangeSeqNum), OLD.umCalendarLocalChangeSeqNum) + 1 FROM HolidayCalendar) END),
                          |umCalendarMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(umCalendarMasterChangeSeqNum), OLD.umCalendarMasterChangeSeqNum) + 1 FROM HolidayCalendar)
                          |ELSE NEW.umCalendarMasterChangeSeqNum END)
                          |WHERE umCalendarUid = NEW.umCalendarUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_28
                          |AFTER INSERT ON HolidayCalendar FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.umCalendarMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.umCalendarLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE HolidayCalendar SET umCalendarLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
                          |ELSE (SELECT MAX(umCalendarLocalChangeSeqNum) + 1 FROM HolidayCalendar) END),
                          |umCalendarMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(umCalendarMasterChangeSeqNum) + 1 FROM HolidayCalendar)
                          |ELSE NEW.umCalendarMasterChangeSeqNum END)
                          |WHERE umCalendarUid = NEW.umCalendarUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS HolidayCalendar_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_HolidayCalendar_trk_clientId_epk_rx_csn 
                          |ON HolidayCalendar_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table HolidayCalendar for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS WorkSpace (  name  TEXT , guestLogin  BOOL , registrationAllowed  BOOL , uid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    //End: Create table WorkSpace for SQLite



                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkContentJoin (  clazzWorkContentJoinContentUid  BIGINT , clazzWorkContentJoinClazzWorkUid  BIGINT , clazzWorkContentJoinInactive  BOOL , clazzWorkContentJoinDateAdded  BIGINT , clazzWorkContentJoinMCSN  BIGINT , clazzWorkContentJoinLCSN  BIGINT , clazzWorkContentJoinLCB  INTEGER , clazzWorkContentJoinUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_204
                          |AFTER UPDATE ON ClazzWorkContentJoin FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkContentJoinMCSN = 0 
                          |OR OLD.clazzWorkContentJoinMCSN = NEW.clazzWorkContentJoinMCSN
                          |)
                          |ELSE
                          |(NEW.clazzWorkContentJoinLCSN = 0  
                          |OR OLD.clazzWorkContentJoinLCSN = NEW.clazzWorkContentJoinLCSN
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkContentJoinLCSN 
                          |ELSE (SELECT MAX(MAX(clazzWorkContentJoinLCSN), OLD.clazzWorkContentJoinLCSN) + 1 FROM ClazzWorkContentJoin) END),
                          |clazzWorkContentJoinMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(clazzWorkContentJoinMCSN), OLD.clazzWorkContentJoinMCSN) + 1 FROM ClazzWorkContentJoin)
                          |ELSE NEW.clazzWorkContentJoinMCSN END)
                          |WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_204
                          |AFTER INSERT ON ClazzWorkContentJoin FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.clazzWorkContentJoinMCSN = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.clazzWorkContentJoinLCSN = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinLCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkContentJoinLCSN 
                          |ELSE (SELECT MAX(clazzWorkContentJoinLCSN) + 1 FROM ClazzWorkContentJoin) END),
                          |clazzWorkContentJoinMCSN = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(clazzWorkContentJoinMCSN) + 1 FROM ClazzWorkContentJoin)
                          |ELSE NEW.clazzWorkContentJoinMCSN END)
                          |WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkContentJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkContentJoin_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkContentJoin_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkContentJoin for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS Report (  reportOwnerUid  BIGINT , chartType  INTEGER , xAxis  INTEGER , yAxis  INTEGER , subGroup  INTEGER , fromDate  BIGINT , toDate  BIGINT , reportTitle  TEXT , reportInactive  BOOL , reportMasterChangeSeqNum  BIGINT , reportLocalChangeSeqNum  BIGINT , reportLastChangedBy  INTEGER , reportUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_101
                          |AFTER UPDATE ON Report FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.reportMasterChangeSeqNum = 0 
                          |OR OLD.reportMasterChangeSeqNum = NEW.reportMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.reportLocalChangeSeqNum = 0  
                          |OR OLD.reportLocalChangeSeqNum = NEW.reportLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE Report SET reportLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(reportLocalChangeSeqNum), OLD.reportLocalChangeSeqNum) + 1 FROM Report) END),
                          |reportMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(reportMasterChangeSeqNum), OLD.reportMasterChangeSeqNum) + 1 FROM Report)
                          |ELSE NEW.reportMasterChangeSeqNum END)
                          |WHERE reportUid = NEW.reportUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_101
                          |AFTER INSERT ON Report FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.reportMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.reportLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE Report SET reportLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportLocalChangeSeqNum 
                          |ELSE (SELECT MAX(reportLocalChangeSeqNum) + 1 FROM Report) END),
                          |reportMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(reportMasterChangeSeqNum) + 1 FROM Report)
                          |ELSE NEW.reportMasterChangeSeqNum END)
                          |WHERE reportUid = NEW.reportUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Report_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Report_trk_clientId_epk_rx_csn 
                          |ON Report_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Report for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS ReportFilter (  reportFilterReportUid  BIGINT , entityUid  BIGINT , entityType  INTEGER , filterInactive  BOOL , reportFilterMasterChangeSeqNum  BIGINT , reportFilterLocalChangeSeqNum  BIGINT , reportFilterLastChangedBy  INTEGER , reportFilterUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_102
                          |AFTER UPDATE ON ReportFilter FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.reportFilterMasterChangeSeqNum = 0 
                          |OR OLD.reportFilterMasterChangeSeqNum = NEW.reportFilterMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.reportFilterLocalChangeSeqNum = 0  
                          |OR OLD.reportFilterLocalChangeSeqNum = NEW.reportFilterLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE ReportFilter SET reportFilterLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportFilterLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(reportFilterLocalChangeSeqNum), OLD.reportFilterLocalChangeSeqNum) + 1 FROM ReportFilter) END),
                          |reportFilterMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(reportFilterMasterChangeSeqNum), OLD.reportFilterMasterChangeSeqNum) + 1 FROM ReportFilter)
                          |ELSE NEW.reportFilterMasterChangeSeqNum END)
                          |WHERE reportFilterUid = NEW.reportFilterUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_102
                          |AFTER INSERT ON ReportFilter FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.reportFilterMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.reportFilterLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE ReportFilter SET reportFilterLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportFilterLocalChangeSeqNum 
                          |ELSE (SELECT MAX(reportFilterLocalChangeSeqNum) + 1 FROM ReportFilter) END),
                          |reportFilterMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(reportFilterMasterChangeSeqNum) + 1 FROM ReportFilter)
                          |ELSE NEW.reportFilterMasterChangeSeqNum END)
                          |WHERE reportFilterUid = NEW.reportFilterUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ReportFilter_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ReportFilter_trk_clientId_epk_rx_csn 
                          |ON ReportFilter_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ReportFilter for SQLite


                    database.execSQL("CREATE TABLE IF NOT EXISTS School (  schoolName  TEXT , schoolDesc  TEXT , schoolAddress  TEXT , schoolActive  BOOL , schoolPhoneNumber  TEXT , schoolGender  INTEGER , schoolHolidayCalendarUid  BIGINT , schoolFeatures  BIGINT , schoolLocationLong  DOUBLE PRECISION , schoolLocationLatt  DOUBLE PRECISION , schoolEmailAddress  TEXT , schoolTeachersPersonGroupUid  BIGINT , schoolStudentsPersonGroupUid  BIGINT , schoolPendingStudentsPersonGroupUid  BIGINT , schoolCode  TEXT , schoolMasterChangeSeqNum  BIGINT , schoolLocalChangeSeqNum  BIGINT , schoolLastChangedBy  INTEGER , schoolTimeZone  TEXT , schoolUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_164
                          |AFTER UPDATE ON School FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.schoolMasterChangeSeqNum = 0 
                          |OR OLD.schoolMasterChangeSeqNum = NEW.schoolMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.schoolLocalChangeSeqNum = 0  
                          |OR OLD.schoolLocalChangeSeqNum = NEW.schoolLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE School SET schoolLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(schoolLocalChangeSeqNum), OLD.schoolLocalChangeSeqNum) + 1 FROM School) END),
                          |schoolMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(schoolMasterChangeSeqNum), OLD.schoolMasterChangeSeqNum) + 1 FROM School)
                          |ELSE NEW.schoolMasterChangeSeqNum END)
                          |WHERE schoolUid = NEW.schoolUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_164
                          |AFTER INSERT ON School FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.schoolMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.schoolLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE School SET schoolLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolLocalChangeSeqNum 
                          |ELSE (SELECT MAX(schoolLocalChangeSeqNum) + 1 FROM School) END),
                          |schoolMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(schoolMasterChangeSeqNum) + 1 FROM School)
                          |ELSE NEW.schoolMasterChangeSeqNum END)
                          |WHERE schoolUid = NEW.schoolUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS School_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_School_trk_clientId_epk_rx_csn 
                          |ON School_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table School for SQLite

                    database.execSQL("CREATE TABLE IF NOT EXISTS SchoolMember (  schoolMemberPersonUid  BIGINT , schoolMemberSchoolUid  BIGINT , schoolMemberJoinDate  BIGINT , schoolMemberLeftDate  BIGINT , schoolMemberRole  INTEGER , schoolMemberActive  BOOL , schoolMemberLocalChangeSeqNum  BIGINT , schoolMemberMasterChangeSeqNum  BIGINT , schoolMemberLastChangedBy  INTEGER , schoolMemberUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE TRIGGER UPD_200
                          |AFTER UPDATE ON SchoolMember FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.schoolMemberMasterChangeSeqNum = 0 
                          |OR OLD.schoolMemberMasterChangeSeqNum = NEW.schoolMemberMasterChangeSeqNum
                          |)
                          |ELSE
                          |(NEW.schoolMemberLocalChangeSeqNum = 0  
                          |OR OLD.schoolMemberLocalChangeSeqNum = NEW.schoolMemberLocalChangeSeqNum
                          |) END)
                          |BEGIN 
                          |UPDATE SchoolMember SET schoolMemberLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolMemberLocalChangeSeqNum 
                          |ELSE (SELECT MAX(MAX(schoolMemberLocalChangeSeqNum), OLD.schoolMemberLocalChangeSeqNum) + 1 FROM SchoolMember) END),
                          |schoolMemberMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(MAX(schoolMemberMasterChangeSeqNum), OLD.schoolMemberMasterChangeSeqNum) + 1 FROM SchoolMember)
                          |ELSE NEW.schoolMemberMasterChangeSeqNum END)
                          |WHERE schoolMemberUid = NEW.schoolMemberUid
                          |; END
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER INS_200
                          |AFTER INSERT ON SchoolMember FOR EACH ROW WHEN
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(NEW.schoolMemberMasterChangeSeqNum = 0 
                          |
                          |)
                          |ELSE
                          |(NEW.schoolMemberLocalChangeSeqNum = 0  
                          |
                          |) END)
                          |BEGIN 
                          |UPDATE SchoolMember SET schoolMemberLocalChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolMemberLocalChangeSeqNum 
                          |ELSE (SELECT MAX(schoolMemberLocalChangeSeqNum) + 1 FROM SchoolMember) END),
                          |schoolMemberMasterChangeSeqNum = 
                          |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                          |(SELECT MAX(schoolMemberMasterChangeSeqNum) + 1 FROM SchoolMember)
                          |ELSE NEW.schoolMemberMasterChangeSeqNum END)
                          |WHERE schoolMemberUid = NEW.schoolMemberUid
                          |; END
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS SchoolMember_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_SchoolMember_trk_clientId_epk_rx_csn 
                          |ON SchoolMember_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table SchoolMember for SQLite


                } else if (database.dbType() == DoorDbType.POSTGRES) {

                    database.execSQL("ALTER TABLE ClazzLog RENAME to ClazzLog_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog (  clazzLogClazzUid  BIGINT , logDate  BIGINT , timeRecorded  BIGINT , clazzLogDone  BOOL , cancellationNote  TEXT , clazzLogCancelled  BOOL , clazzLogNumPresent  INTEGER , clazzLogNumAbsent  INTEGER , clazzLogNumPartial  INTEGER , clazzLogScheduleUid  BIGINT , clazzLogStatusFlag  INTEGER , clazzLogMSQN  BIGINT , clazzLogLCSN  BIGINT , clazzLogLCB  INTEGER , clazzLogUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ClazzLog (clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, cancellationNote, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogStatusFlag, clazzLogMSQN, clazzLogLCSN, clazzLogLCB) SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, false, '', false, 0, 0, 0, 0, 0, 0, 0, 0 FROM ClazzLog_OLD")
                    database.execSQL("DROP TABLE ClazzLog_OLD")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzLog_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzLog_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_14_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzLog SET clazzLogLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
                          | ELSE NEXTVAL('ClazzLog_lcsn_seq') END),
                          | clazzLogMSQN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzLog_mcsn_seq') 
                          | ELSE NEW.clazzLogMSQN END)
                          | WHERE clazzLogUid = NEW.clazzLogUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_14_trig 
                          |AFTER UPDATE OR INSERT ON ClazzLog 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_14_fn()
                          """.trimMargin())
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_14_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_14")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzLog_trk_clientId_epk_rx_csn 
                          |ON ClazzLog_trk (clientId, epk, rx, csn)
                          """.trimMargin())


                    database.execSQL("ALTER TABLE ClazzLogAttendanceRecord RENAME to ClazzLogAttendanceRecord_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord (  clazzLogAttendanceRecordClazzLogUid  BIGINT , clazzLogAttendanceRecordClazzMemberUid  BIGINT , attendanceStatus  INTEGER , clazzLogAttendanceRecordMasterChangeSeqNum  BIGINT , clazzLogAttendanceRecordLocalChangeSeqNum  BIGINT , clazzLogAttendanceRecordLastChangedBy  INTEGER , clazzLogAttendanceRecordUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ClazzLogAttendanceRecord (clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy) SELECT clazzLogAttendanceRecordUid, clazzLogClazzLogUid, studentClazzMemberUid, attendanceStatus, 0, 0, 0 FROM ClazzLogAttendanceRecord_OLD")
                    database.execSQL("DROP TABLE ClazzLogAttendanceRecord_OLD")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzLogAttendanceRecord_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzLogAttendanceRecord_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_15_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
                          | ELSE NEXTVAL('ClazzLogAttendanceRecord_lcsn_seq') END),
                          | clazzLogAttendanceRecordMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzLogAttendanceRecord_mcsn_seq') 
                          | ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
                          | WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_15_trig 
                          |AFTER UPDATE OR INSERT ON ClazzLogAttendanceRecord 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_15_fn()
                          """.trimMargin())
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_15_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_15")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn 
                          |ON ClazzLogAttendanceRecord_trk (clientId, epk, rx, csn)
                          """.trimMargin())

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWork (  clazzWorkCreatorPersonUid  BIGINT , clazzWorkClazzUid  BIGINT , clazzWorkTitle  TEXT , clazzWorkCreatedDate  BIGINT , clazzWorkStartDateTime  BIGINT , clazzWorkStartTime  BIGINT , clazzWorkDueTime  BIGINT , clazzWorkDueDateTime  BIGINT , clazzWorkSubmissionType  INTEGER , clazzWorkCommentsEnabled  BOOL , clazzWorkMaximumScore  INTEGER , clazzWorkInstructions  TEXT , clazzWorkActive  BOOL , clazzWorkLocalChangeSeqNum  BIGINT , clazzWorkMasterChangeSeqNum  BIGINT , clazzWorkLastChangedBy  INTEGER , clazzWorkUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWork_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWork_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_201_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWork SET clazzWorkLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkLocalChangeSeqNum 
                          | ELSE NEXTVAL('ClazzWork_lcsn_seq') END),
                          | clazzWorkMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWork_mcsn_seq') 
                          | ELSE NEW.clazzWorkMasterChangeSeqNum END)
                          | WHERE clazzWorkUid = NEW.clazzWorkUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_201_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWork 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_201_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWork_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWork_trk_clientId_epk_rx_csn 
                          |ON ClazzWork_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWork for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestion (  clazzWorkQuestionText  TEXT , clazzWorkQuestionClazzWorkUid  BIGINT , clazzWorkQuestionIndex  INTEGER , clazzWorkQuestionType  INTEGER , clazzWorkQuestionActive  BOOL , clazzWorkQuestionMCSN  BIGINT , clazzWorkQuestionLCSN  BIGINT , clazzWorkQuestionLCB  INTEGER , clazzWorkQuestionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestion_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestion_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_202_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWorkQuestion SET clazzWorkQuestionLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionLCSN 
                          | ELSE NEXTVAL('ClazzWorkQuestion_lcsn_seq') END),
                          | clazzWorkQuestionMCSN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWorkQuestion_mcsn_seq') 
                          | ELSE NEW.clazzWorkQuestionMCSN END)
                          | WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_202_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWorkQuestion 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_202_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestion_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestion_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestion_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestion for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionOption (  clazzWorkQuestionOptionText  TEXT , clazzWorkQuestionOptionQuestionUid  BIGINT , clazzWorkQuestionOptionMasterChangeSeqNum  BIGINT , clazzWorkQuestionOptionLocalChangeSeqNum  BIGINT , clazzWorkQuestionOptionLastChangedBy  INTEGER , clazzWorkQuestionOptionActive  BOOL , clazzWorkQuestionOptionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestionOption_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestionOption_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_203_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionOptionLocalChangeSeqNum 
                          | ELSE NEXTVAL('ClazzWorkQuestionOption_lcsn_seq') END),
                          | clazzWorkQuestionOptionMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWorkQuestionOption_mcsn_seq') 
                          | ELSE NEW.clazzWorkQuestionOptionMasterChangeSeqNum END)
                          | WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_203_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWorkQuestionOption 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_203_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionOption_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestionOption_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestionOption_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestionOption for PostgreSQL


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse (  clazzWorkQuestionResponseClazzWorkUid  BIGINT , clazzWorkQuestionResponseQuestionUid  BIGINT , clazzWorkQuestionResponseText  TEXT , clazzWorkQuestionResponseOptionSelected  BIGINT , clazzWorkQuestionResponsePersonUid  BIGINT , clazzWorkQuestionResponseClazzMemberUid  BIGINT , clazzWorkQuestionResponseInactive  BOOL , clazzWorkQuestionResponseDateResponded  BIGINT , clazzWorkQuestionResponseMCSN  BIGINT , clazzWorkQuestionResponseLCSN  BIGINT , clazzWorkQuestionResponseLCB  INTEGER , clazzWorkQuestionResponseUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestionResponse_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkQuestionResponse_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_209_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWorkQuestionResponse SET clazzWorkQuestionResponseLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionResponseLCSN 
                          | ELSE NEXTVAL('ClazzWorkQuestionResponse_lcsn_seq') END),
                          | clazzWorkQuestionResponseMCSN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWorkQuestionResponse_mcsn_seq') 
                          | ELSE NEW.clazzWorkQuestionResponseMCSN END)
                          | WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_209_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWorkQuestionResponse 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_209_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkQuestionResponse_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkQuestionResponse_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkQuestionResponse for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission (  clazzWorkSubmissionClazzWorkUid  BIGINT , clazzWorkSubmissionClazzMemberUid  BIGINT , clazzWorkSubmissionMarkerClazzMemberUid  BIGINT , clazzWorkSubmissionMarkerPersonUid  BIGINT , clazzWorkSubmissionPersonUid  BIGINT , clazzWorkSubmissionInactive  BOOL , clazzWorkSubmissionDateTimeStarted  BIGINT , clazzWorkSubmissionDateTimeUpdated  BIGINT , clazzWorkSubmissionDateTimeFinished  BIGINT , clazzWorkSubmissionDateTimeMarked  BIGINT , clazzWorkSubmissionText  TEXT , clazzWorkSubmissionScore  INTEGER , clazzWorkSubmissionMCSN  BIGINT , clazzWorkSubmissionLCSN  BIGINT , clazzWorkSubmissionLCB  INTEGER , clazzWorkSubmissionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkSubmission_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkSubmission_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_206_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWorkSubmission SET clazzWorkSubmissionLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkSubmissionLCSN 
                          | ELSE NEXTVAL('ClazzWorkSubmission_lcsn_seq') END),
                          | clazzWorkSubmissionMCSN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWorkSubmission_mcsn_seq') 
                          | ELSE NEW.clazzWorkSubmissionMCSN END)
                          | WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_206_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWorkSubmission 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_206_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkSubmission_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkSubmission_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkSubmission for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS Comments (  commentsText  TEXT , commentsEntityType  INTEGER , commentsEntityUid  BIGINT , commentsPublic  BOOL , commentsStatus  INTEGER , commentsPersonUid  BIGINT , commentsToPersonUid  BIGINT , commentsFlagged  BOOL , commentsInActive  BOOL , commentsDateTimeAdded  BIGINT , commentsDateTimeUpdated  BIGINT , commentsMCSN  BIGINT , commentsLCSN  BIGINT , commentsLCB  INTEGER , commentsUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Comments_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Comments_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_208_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE Comments SET commentsLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.commentsLCSN 
                          | ELSE NEXTVAL('Comments_lcsn_seq') END),
                          | commentsMCSN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('Comments_mcsn_seq') 
                          | ELSE NEW.commentsMCSN END)
                          | WHERE commentsUid = NEW.commentsUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_208_trig 
                          |AFTER UPDATE OR INSERT ON Comments 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_208_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Comments_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Comments_trk_clientId_epk_rx_csn 
                          |ON Comments_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Comments for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ContainerUploadJob (  cujContainerUid  BIGINT , sessionId  TEXT , jobStatus  INTEGER , bytesSoFar  BIGINT , contentLength  BIGINT , containerEntryFileUids  TEXT , cujUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    //End: Create table ContainerUploadJob for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryProgress (  contentEntryProgressActive  BOOL , contentEntryProgressContentEntryUid  BIGINT , contentEntryProgressPersonUid  BIGINT , contentEntryProgressProgress  INTEGER , contentEntryProgressStatusFlag  INTEGER , contentEntryProgressLocalChangeSeqNum  BIGINT , contentEntryProgressMasterChangeSeqNum  BIGINT , contentEntryProgressLastChangedBy  INTEGER , contentEntryProgressUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryProgress_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryProgress_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_210_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ContentEntryProgress SET contentEntryProgressLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryProgressLocalChangeSeqNum 
                          | ELSE NEXTVAL('ContentEntryProgress_lcsn_seq') END),
                          | contentEntryProgressMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ContentEntryProgress_mcsn_seq') 
                          | ELSE NEW.contentEntryProgressMasterChangeSeqNum END)
                          | WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_210_trig 
                          |AFTER UPDATE OR INSERT ON ContentEntryProgress 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_210_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryProgress_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ContentEntryProgress_trk_clientId_epk_rx_csn 
                          |ON ContentEntryProgress_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ContentEntryProgress for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS DeviceSession (  dsDeviceId  INTEGER , dsPersonUid  BIGINT , expires  BIGINT , deviceSessionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    //End: Create table DeviceSession for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS TimeZoneEntity (  id  TEXT  PRIMARY KEY  NOT NULL , rawOffset  INTEGER )")
                    //End: Create table TimeZoneEntity for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS Holiday (  holMasterCsn  BIGINT , holLocalCsn  BIGINT , holLastModBy  INTEGER , holActive  BOOL , holHolidayCalendarUid  BIGINT , holStartTime  BIGINT , holEndTime  BIGINT , holName  TEXT , holUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Holiday_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Holiday_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_99_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE Holiday SET holLocalCsn =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.holLocalCsn 
                          | ELSE NEXTVAL('Holiday_lcsn_seq') END),
                          | holMasterCsn = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('Holiday_mcsn_seq') 
                          | ELSE NEW.holMasterCsn END)
                          | WHERE holUid = NEW.holUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_99_trig 
                          |AFTER UPDATE OR INSERT ON Holiday 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_99_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Holiday_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Holiday_trk_clientId_epk_rx_csn 
                          |ON Holiday_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Holiday for PostgreSQL


                    database.execSQL("CREATE TABLE IF NOT EXISTS HolidayCalendar (  umCalendarName  TEXT , umCalendarCategory  INTEGER , umCalendarActive  BOOL , umCalendarMasterChangeSeqNum  BIGINT , umCalendarLocalChangeSeqNum  BIGINT , umCalendarLastChangedBy  INTEGER , umCalendarUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS HolidayCalendar_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS HolidayCalendar_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_28_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE HolidayCalendar SET umCalendarLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
                          | ELSE NEXTVAL('HolidayCalendar_lcsn_seq') END),
                          | umCalendarMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('HolidayCalendar_mcsn_seq') 
                          | ELSE NEW.umCalendarMasterChangeSeqNum END)
                          | WHERE umCalendarUid = NEW.umCalendarUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_28_trig 
                          |AFTER UPDATE OR INSERT ON HolidayCalendar 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_28_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS HolidayCalendar_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_HolidayCalendar_trk_clientId_epk_rx_csn 
                          |ON HolidayCalendar_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table HolidayCalendar for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS WorkSpace (  name  TEXT , guestLogin  BOOL , registrationAllowed  BOOL , uid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    //End: Create table WorkSpace for PostgreSQL


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkContentJoin (  clazzWorkContentJoinContentUid  BIGINT , clazzWorkContentJoinClazzWorkUid  BIGINT , clazzWorkContentJoinInactive  BOOL , clazzWorkContentJoinDateAdded  BIGINT , clazzWorkContentJoinMCSN  BIGINT , clazzWorkContentJoinLCSN  BIGINT , clazzWorkContentJoinLCB  INTEGER , clazzWorkContentJoinUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkContentJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzWorkContentJoin_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_204_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinLCSN =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkContentJoinLCSN 
                          | ELSE NEXTVAL('ClazzWorkContentJoin_lcsn_seq') END),
                          | clazzWorkContentJoinMCSN = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ClazzWorkContentJoin_mcsn_seq') 
                          | ELSE NEW.clazzWorkContentJoinMCSN END)
                          | WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_204_trig 
                          |AFTER UPDATE OR INSERT ON ClazzWorkContentJoin 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_204_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkContentJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ClazzWorkContentJoin_trk_clientId_epk_rx_csn 
                          |ON ClazzWorkContentJoin_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ClazzWorkContentJoin for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS Report (  reportOwnerUid  BIGINT , chartType  INTEGER , xAxis  INTEGER , yAxis  INTEGER , subGroup  INTEGER , fromDate  BIGINT , toDate  BIGINT , reportTitle  TEXT , reportInactive  BOOL , reportMasterChangeSeqNum  BIGINT , reportLocalChangeSeqNum  BIGINT , reportLastChangedBy  INTEGER , reportUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Report_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Report_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_101_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE Report SET reportLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportLocalChangeSeqNum 
                          | ELSE NEXTVAL('Report_lcsn_seq') END),
                          | reportMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('Report_mcsn_seq') 
                          | ELSE NEW.reportMasterChangeSeqNum END)
                          | WHERE reportUid = NEW.reportUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_101_trig 
                          |AFTER UPDATE OR INSERT ON Report 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_101_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Report_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_Report_trk_clientId_epk_rx_csn 
                          |ON Report_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table Report for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS ReportFilter (  reportFilterReportUid  BIGINT , entityUid  BIGINT , entityType  INTEGER , filterInactive  BOOL , reportFilterMasterChangeSeqNum  BIGINT , reportFilterLocalChangeSeqNum  BIGINT , reportFilterLastChangedBy  INTEGER , reportFilterUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ReportFilter_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ReportFilter_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_102_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE ReportFilter SET reportFilterLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportFilterLocalChangeSeqNum 
                          | ELSE NEXTVAL('ReportFilter_lcsn_seq') END),
                          | reportFilterMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('ReportFilter_mcsn_seq') 
                          | ELSE NEW.reportFilterMasterChangeSeqNum END)
                          | WHERE reportFilterUid = NEW.reportFilterUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_102_trig 
                          |AFTER UPDATE OR INSERT ON ReportFilter 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_102_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ReportFilter_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_ReportFilter_trk_clientId_epk_rx_csn 
                          |ON ReportFilter_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table ReportFilter for PostgreSQL


                    database.execSQL("CREATE TABLE IF NOT EXISTS School (  schoolName  TEXT , schoolDesc  TEXT , schoolAddress  TEXT , schoolActive  BOOL , schoolPhoneNumber  TEXT , schoolGender  INTEGER , schoolHolidayCalendarUid  BIGINT , schoolFeatures  BIGINT , schoolLocationLong  DOUBLE PRECISION , schoolLocationLatt  DOUBLE PRECISION , schoolEmailAddress  TEXT , schoolTeachersPersonGroupUid  BIGINT , schoolStudentsPersonGroupUid  BIGINT , schoolPendingStudentsPersonGroupUid  BIGINT , schoolCode  TEXT , schoolMasterChangeSeqNum  BIGINT , schoolLocalChangeSeqNum  BIGINT , schoolLastChangedBy  INTEGER , schoolTimeZone  TEXT , schoolUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS School_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS School_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_164_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE School SET schoolLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolLocalChangeSeqNum 
                          | ELSE NEXTVAL('School_lcsn_seq') END),
                          | schoolMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('School_mcsn_seq') 
                          | ELSE NEW.schoolMasterChangeSeqNum END)
                          | WHERE schoolUid = NEW.schoolUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_164_trig 
                          |AFTER UPDATE OR INSERT ON School 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_164_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS School_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_School_trk_clientId_epk_rx_csn 
                          |ON School_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table School for PostgreSQL

                    database.execSQL("CREATE TABLE IF NOT EXISTS SchoolMember (  schoolMemberPersonUid  BIGINT , schoolMemberSchoolUid  BIGINT , schoolMemberJoinDate  BIGINT , schoolMemberLeftDate  BIGINT , schoolMemberRole  INTEGER , schoolMemberActive  BOOL , schoolMemberLocalChangeSeqNum  BIGINT , schoolMemberMasterChangeSeqNum  BIGINT , schoolMemberLastChangedBy  INTEGER , schoolMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SchoolMember_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SchoolMember_lcsn_seq")
                    database.execSQL("""
                          |CREATE OR REPLACE FUNCTION 
                          | inccsn_200_fn() RETURNS trigger AS ${'$'}${'$'}
                          | BEGIN  
                          | UPDATE SchoolMember SET schoolMemberLocalChangeSeqNum =
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolMemberLocalChangeSeqNum 
                          | ELSE NEXTVAL('SchoolMember_lcsn_seq') END),
                          | schoolMemberMasterChangeSeqNum = 
                          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                          | THEN NEXTVAL('SchoolMember_mcsn_seq') 
                          | ELSE NEW.schoolMemberMasterChangeSeqNum END)
                          | WHERE schoolMemberUid = NEW.schoolMemberUid;
                          | RETURN null;
                          | END ${'$'}${'$'}
                          | LANGUAGE plpgsql
                          """.trimMargin())
                    database.execSQL("""
                          |CREATE TRIGGER inccsn_200_trig 
                          |AFTER UPDATE OR INSERT ON SchoolMember 
                          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                          |EXECUTE PROCEDURE inccsn_200_fn()
                          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS SchoolMember_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                          |CREATE 
                          | INDEX index_SchoolMember_trk_clientId_epk_rx_csn 
                          |ON SchoolMember_trk (clientId, epk, rx, csn)
                          """.trimMargin())
                    //End: Create table SchoolMember for PostgreSQL


                }


                database.execSQL("""ALTER TABLE Clazz 
                        ADD COLUMN clazzPendingStudentsPersonGroupUid BIGINT DEFAULT 0 NOT NULL,
                        ADD COLUMN clazzStudentsPersonGroupUid BIGINT DEFAULT 0 NOT NULL, 
                        ADD COLUMN clazzTeachersPersonGroupUid  BIGINT DEFAULT 0 NOT NULL,
                        ADD COLUMN clazzHolidayUMCalendarUid  BIGINT DEFAULT 0 NOT NULL, 
                        ADD COLUMN clazzScheuleUMCalendarUid  BIGINT DEFAULT 0 NOT NULL, 
                        ADD COLUMN isClazzActive  BOOL,
                        ADD COLUMN clazzLocationUid  BIGINT DEFAULT 0 NOT NULL, 
                        ADD COLUMN clazzStartTime  BIGINT DEFAULT 0 NOT NULL, 
                        ADD COLUMN clazzEndTime  BIGINT DEFAULT 0 NOT NULL,
                        ADD COLUMN clazzFeatures  BIGINT, 
                        ADD COLUMN clazzSchoolUid  BIGINT DEFAULT 0 NOT NULL,
                        ADD COLUMN clazzCode TEXT, 
                        ADD COLUMN clazzDesc TEXT, 
                        ADD COLUMN clazzTimeZone TEXT,
                        ADD COLUMN clazzCode TEXT""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzMember 
                        RENAME COLUMN dateJoined to clazzMemberDateJoined, 
                        RENAME COLUMN dateLeft to clazzMemberDateLeft, 
                        RENAME COLUMN role to clazzMemberRole, 
                        ADD COLUMN clazzMemberActive  BOOL, 
                        ADD COLUMN clazzMemberAttendancePercentage FLOAT
                    """.trimMargin())

                database.execSQL("""ALTER TABLE Person 
                        ADD COLUMN dateOfBirth BIGINT DEFAULT 0 NOT NULL,   
                        ADD COLUMN personAddress TEXT, 
                        ADD COLUMN personNotes TEXT, 
                        ADD COLUMN fatherName TEXT,
                        ADD COLUMN motherName TEXT, 
                        ADD COLUMN personAddress TEXT, 
                        ADD COLUMN motherNum TEXT, 
                        ADD COLUMN personOrgId TEXT,     
                        ADD COLUMN fatherNumber TEXT""".trimMargin())


                database.execSQL("""ALTER TABLE PersonAuth 
                        ADD COLUMN personAuthStatus INTEGER DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("""ALTER TABLE PersonGroup 
                        ADD COLUMN groupActive BOOL""".trimMargin())

                database.execSQL("""ALTER TABLE PersonGroupMember 
                        ADD COLUMN groupMemberActive BOOL""".trimMargin())

                database.execSQL("""ALTER TABLE PersonPicture 
                        ADD COLUMN personPictureActive BOOL, 
                        ADD COLUMN picTimestamp BIGINT DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("""ALTER TABLE Role 
                        ADD COLUMN roleActive BOOL""".trimMargin())

                database.execSQL("""ALTER TABLE StatementEntity 
                        RENAME COLUMN personuid to statementPersonUid, 
                        RENAME COLUMN verbUid to statementVerbUid, 
                        ADD COLUMN extensionProgress INTEGER, 
                        ADD COLUMN statementContentEntryUid BIGINT
                        """.trimMargin())

                database.execSQL("ALTER TABLE EntityRole ADD COLUMN erActive BOOL")


                database.execSQL("DROP TABLE IF EXISTS DiscussionMessage")
                database.execSQL("DROP TABLE IF EXISTS FeedEntry")
                database.execSQL("DROP TABLE IF EXISTS Location")
                database.execSQL("DROP TABLE IF EXISTS LocationAncestorJoin")
                database.execSQL("DROP TABLE IF EXISTS PersonLocationJoin")

            }
        }


        private fun addMigrations(builder: DatabaseBuilder<UmAppDatabase>): DatabaseBuilder<UmAppDatabase> {


            builder.addMigrations(object : DoorMigration(26, 27) {
                override fun migrate(database: DoorSqlDatabase) {

                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN status, ADD COLUMN contentFlags INTEGER NOT NULL DEFAULT 0, ADD COLUMN ceInactive BOOL")
                }

            })


            builder.addMigrations(object : DoorMigration(25, 26) {
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN imported, ADD COLUMN status INTEGER NOT NULL DEFAULT 0")
                }

            })

            builder.addMigrations(object : DoorMigration(24, 25) {
                override fun migrate(database: DoorSqlDatabase) {
                    try {
                        database.execSQL("ALTER TABLE Container RENAME COLUMN lastModified TO cntLastModified")
                    } catch (e: Exception) {
                        print(e.message)
                    }
                }

            })

            builder.addMigrations(object : DoorMigration(20, 24) {
                override fun migrate(database: DoorSqlDatabase) {

                    // SyncNode
                    database.execSQL("CREATE TABLE IF NOT EXISTS SyncNode (  nodeClientId  INTEGER  PRIMARY KEY  NOT NULL , master  BOOL )")
                    database.execSQL("INSERT INTO SyncNode(nodeClientId,master) SELECT devicebits, master FROM syncdevicebits")
                    database.execSQL("DROP TABLE syncdevicebits")


                    //ContentEntryStatus
                    database.execSQL("ALTER TABLE ContentEntryStatus ADD COLUMN locallyAvailable BOOL, ADD COLUMN downloadSpeed INTEGER")

                    // NetworkNode
                    database.execSQL("ALTER TABLE NetworkNode ADD COLUMN endpointUrl TEXT, ADD COLUMN dlastUpdateTimeStamp  BIGINT, ADD COLUMN numFailureCount  INTEGER, ADD COLUMN groupSsid  TEXT, ALTER COLUMN nodeId SET DATA TYPE BIGINT")

                    // EntryStatusResponse
                    database.execSQL("ALTER TABLE EntryStatusResponse RENAME COLUMN respondernodeid TO erNodeId")
                    database.execSQL("ALTER TABLE EntryStatusResponse RENAME COLUMN id TO erId")
                    database.execSQL("ALTER TABLE EntryStatusResponse DROP COLUMN entryid, ADD COLUMN erContainerUid BIGINT")


                    // Content Entry
                    database.execSQL("ALTER TABLE ContentEntry ADD COLUMN imported BOOL")
                    database.execSQL("ALTER TABLE ContentEntry RENAME to ContentEntry_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntry (  title  TEXT , description  TEXT , entryId  TEXT , author  TEXT , publisher  TEXT , licenseType  INTEGER , licenseName  TEXT , licenseUrl  TEXT , sourceUrl  TEXT , thumbnailUrl  TEXT , cntLastModified  BIGINT , primaryLanguageUid  BIGINT , languageVariantUid  BIGINT , leaf  BOOL , imported  BOOL , publik  BOOL , contentTypeFlag  INTEGER , contentEntryLocalChangeSeqNum  BIGINT , contentEntryMasterChangeSeqNum  BIGINT , contentEntryLastChangedBy  INTEGER , contentEntryUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentEntry (contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, cntLastModified, primaryLanguageUid, languageVariantUid, leaf, imported, publik, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy) SELECT contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, cntLastModified, primaryLanguageUid, languageVariantUid, leaf, imported, publik, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy FROM ContentEntry_OLD")
                    database.execSQL("DROP TABLE ContentEntry_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_42_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_42")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntry_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntry_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_42_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentEntry SET contentEntryLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentEntry_lcsn_seq') END),
                    | contentEntryMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentEntry_mcsn_seq') 
                    | ELSE NEW.contentEntryMasterChangeSeqNum END)
                    | WHERE contentEntryUid = NEW.contentEntryUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_42_trig 
                    |AFTER UPDATE OR INSERT ON ContentEntry 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_42_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntry_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentEntry_trk_clientId_epk_rx_csn 
                    |ON ContentEntry_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // Agent Entity
                    database.execSQL("CREATE TABLE IF NOT EXISTS AgentEntity (  agentMbox  TEXT , agentMbox_sha1sum  TEXT , agentOpenid  TEXT , agentAccountName  TEXT , agentHomePage  TEXT , agentPersonUid  BIGINT , statementMasterChangeSeqNum  BIGINT , statementLocalChangeSeqNum  BIGINT , statementLastChangedBy  INTEGER , agentUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS AgentEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS AgentEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_68_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE AgentEntity SET statementLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
                    | ELSE NEXTVAL('AgentEntity_lcsn_seq') END),
                    | statementMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('AgentEntity_mcsn_seq') 
                    | ELSE NEW.statementMasterChangeSeqNum END)
                    | WHERE agentUid = NEW.agentUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_68_trig 
                    |AFTER UPDATE OR INSERT ON AgentEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_68_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS AgentEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_AgentEntity_trk_clientId_epk_rx_csn 
                    |ON AgentEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // Clazz
                    database.execSQL("ALTER TABLE Clazz ADD COLUMN clazzLocationUid  BIGINT")
                    database.execSQL("ALTER TABLE Clazz RENAME to Clazz_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Clazz (  clazzName  TEXT , attendanceAverage  FLOAT , clazzMasterChangeSeqNum  BIGINT , clazzLocalChangeSeqNum  BIGINT , clazzLastChangedBy  INTEGER , clazzLocationUid  BIGINT , clazzUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Clazz (clazzUid, clazzName, attendanceAverage, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLocationUid) SELECT clazzUid, clazzName, attendanceAverage, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLocationUid FROM Clazz_OLD")
                    database.execSQL("DROP TABLE Clazz_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_6_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_6")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Clazz_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Clazz_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_6_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Clazz SET clazzLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLocalChangeSeqNum 
                    | ELSE NEXTVAL('Clazz_lcsn_seq') END),
                    | clazzMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Clazz_mcsn_seq') 
                    | ELSE NEW.clazzMasterChangeSeqNum END)
                    | WHERE clazzUid = NEW.clazzUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_6_trig 
                    |AFTER UPDATE OR INSERT ON Clazz 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_6_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Clazz_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Clazz_trk_clientId_epk_rx_csn 
                    |ON Clazz_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // StateContentEntity
                    database.execSQL("CREATE TABLE IF NOT EXISTS StateContentEntity (  stateContentStateUid  BIGINT , stateContentKey  TEXT , stateContentValue  TEXT , isIsactive  BOOL , stateContentMasterChangeSeqNum  BIGINT , stateContentLocalChangeSeqNum  BIGINT , stateContentLastChangedBy  INTEGER , stateContentUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StateContentEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StateContentEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_72_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE StateContentEntity SET stateContentLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateContentLocalChangeSeqNum 
                    | ELSE NEXTVAL('StateContentEntity_lcsn_seq') END),
                    | stateContentMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('StateContentEntity_mcsn_seq') 
                    | ELSE NEW.stateContentMasterChangeSeqNum END)
                    | WHERE stateContentUid = NEW.stateContentUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_72_trig 
                    |AFTER UPDATE OR INSERT ON StateContentEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_72_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS StateContentEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_StateContentEntity_trk_clientId_epk_rx_csn 
                    |ON StateContentEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // State Entity
                    database.execSQL("CREATE TABLE IF NOT EXISTS StateEntity (  stateId  TEXT , agentUid  BIGINT , activityId  TEXT , registration  TEXT , isIsactive  BOOL , timestamp  BIGINT , stateMasterChangeSeqNum  BIGINT , stateLocalChangeSeqNum  BIGINT , stateLastChangedBy  INTEGER , stateUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StateEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StateEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_70_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE StateEntity SET stateLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateLocalChangeSeqNum 
                    | ELSE NEXTVAL('StateEntity_lcsn_seq') END),
                    | stateMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('StateEntity_mcsn_seq') 
                    | ELSE NEW.stateMasterChangeSeqNum END)
                    | WHERE stateUid = NEW.stateUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_70_trig 
                    |AFTER UPDATE OR INSERT ON StateEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_70_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS StateEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_StateEntity_trk_clientId_epk_rx_csn 
                    |ON StateEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // Statement Entity
                    database.execSQL("CREATE TABLE IF NOT EXISTS StatementEntity (  statementId  TEXT , personUid  BIGINT , verbUid  BIGINT , xObjectUid  BIGINT , subStatementActorUid  BIGINT , substatementVerbUid  BIGINT , subStatementObjectUid  BIGINT , agentUid  BIGINT , instructorUid  BIGINT , authorityUid  BIGINT , teamUid  BIGINT , resultCompletion  BOOL , resultSuccess  SMALLINT , resultScoreScaled  BIGINT , resultScoreRaw  BIGINT , resultScoreMin  BIGINT , resultScoreMax  BIGINT , resultDuration  BIGINT , resultResponse  TEXT , timestamp  BIGINT , stored  BIGINT , contextRegistration  TEXT , contextPlatform  TEXT , contextStatementId  TEXT , fullStatement  TEXT , statementMasterChangeSeqNum  BIGINT , statementLocalChangeSeqNum  BIGINT , statementLastChangedBy  INTEGER , statementUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StatementEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS StatementEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_60_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE StatementEntity SET statementLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
                    | ELSE NEXTVAL('StatementEntity_lcsn_seq') END),
                    | statementMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('StatementEntity_mcsn_seq') 
                    | ELSE NEW.statementMasterChangeSeqNum END)
                    | WHERE statementUid = NEW.statementUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_60_trig 
                    |AFTER UPDATE OR INSERT ON StatementEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_60_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS StatementEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_StatementEntity_trk_clientId_epk_rx_csn 
                    |ON StatementEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // Verb Entity
                    database.execSQL("CREATE TABLE IF NOT EXISTS VerbEntity (  urlId  TEXT , verbMasterChangeSeqNum  BIGINT , verbLocalChangeSeqNum  BIGINT , verbLastChangedBy  INTEGER , verbUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS VerbEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS VerbEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_62_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE VerbEntity SET verbLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
                    | ELSE NEXTVAL('VerbEntity_lcsn_seq') END),
                    | verbMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('VerbEntity_mcsn_seq') 
                    | ELSE NEW.verbMasterChangeSeqNum END)
                    | WHERE verbUid = NEW.verbUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_62_trig 
                    |AFTER UPDATE OR INSERT ON VerbEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_62_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS VerbEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_VerbEntity_trk_clientId_epk_rx_csn 
                    |ON VerbEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // XLangMapEntry
                    database.execSQL("CREATE TABLE IF NOT EXISTS XLangMapEntry (  verbLangMapUid  BIGINT , objectLangMapUid  BIGINT , languageLangMapUid  BIGINT , languageVariantLangMapUid  BIGINT , valueLangMap  TEXT , statementLangMapMasterCsn  INTEGER , statementLangMapLocalCsn  INTEGER , statementLangMapLcb  INTEGER , statementLangMapUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS XLangMapEntry_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS XLangMapEntry_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_74_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE XLangMapEntry SET statementLangMapLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLangMapLocalCsn 
                    | ELSE NEXTVAL('XLangMapEntry_lcsn_seq') END),
                    | statementLangMapMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('XLangMapEntry_mcsn_seq') 
                    | ELSE NEW.statementLangMapMasterCsn END)
                    | WHERE statementLangMapUid = NEW.statementLangMapUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_74_trig 
                    |AFTER UPDATE OR INSERT ON XLangMapEntry 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_74_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS XLangMapEntry_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_XLangMapEntry_trk_clientId_epk_rx_csn 
                    |ON XLangMapEntry_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // XObjectEntity
                    database.execSQL("CREATE TABLE IF NOT EXISTS XObjectEntity (  objectType  TEXT , objectId  TEXT , definitionType  TEXT , interactionType  TEXT , correctResponsePattern  TEXT , objectContentEntryUid  BIGINT , xObjectMasterChangeSeqNum  BIGINT , xObjectocalChangeSeqNum  BIGINT , xObjectLastChangedBy  INTEGER , xObjectUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS XObjectEntity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS XObjectEntity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_64_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE XObjectEntity SET xObjectocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.xObjectocalChangeSeqNum 
                    | ELSE NEXTVAL('XObjectEntity_lcsn_seq') END),
                    | xObjectMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('XObjectEntity_mcsn_seq') 
                    | ELSE NEW.xObjectMasterChangeSeqNum END)
                    | WHERE xObjectUid = NEW.xObjectUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_64_trig 
                    |AFTER UPDATE OR INSERT ON XObjectEntity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_64_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS XObjectEntity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_XObjectEntity_trk_clientId_epk_rx_csn 
                    |ON XObjectEntity_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // DownloadJob
                    database.execSQL("ALTER TABLE DownloadJob ADD COLUMN totalBytesToDownload BIGINT, ADD COLUMN bytesDownloadedSoFar BIGINT, ADD COLUMN djRootContentEntryUid  BIGINT, ADD COLUMN meteredNetworkAllowed  BOOL,ADD COLUMN djDestinationDir TEXT")

                    // DownloadJobItem
                    database.execSQL("ALTER TABLE DownloadJobItem ADD COLUMN djiContainerUid  BIGINT , ADD COLUMN djiContentEntryUid  BIGINT, DROP COLUMN djicontententryfileuid")

                    // DownloadJobItemParentChildJoin
                    database.execSQL("CREATE TABLE IF NOT EXISTS DownloadJobItemParentChildJoin (  djiParentDjiUid  INTEGER , djiChildDjiUid  INTEGER , djiCepcjUid  BIGINT , djiPcjUid  SERIAL  PRIMARY KEY  NOT NULL )")

                    // DownloadSet
                    database.execSQL("DROP TABLE DownloadSet")
                    database.execSQL("DROP TABLE DownloadSetItem")
                    database.execSQL("DROP SEQUENCE IF EXISTS downloadset_dsuid_seq")
                    database.execSQL("DROP SEQUENCE IF EXISTS downloadsetitem_dsiuid_seq")

                    // PersonAuth
                    database.execSQL("DROP TABLE PersonAuth")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_30")
                    database.execSQL("DROP TRIGGER IF EXISTS inc_csn_30_trig ON PersonAuth")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_30_fn")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth (  personAuthUid  BIGINT  PRIMARY KEY  NOT NULL , passwordHash  TEXT )")

                    //Clazz Member
                    database.execSQL("ALTER TABLE ClazzMember RENAME to ClazzMember_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzMember (  clazzMemberPersonUid  BIGINT , clazzMemberClazzUid  BIGINT , dateJoined  BIGINT , dateLeft  BIGINT , role  INTEGER , clazzMemberLocalChangeSeqNum  BIGINT , clazzMemberMasterChangeSeqNum  BIGINT , clazzMemberLastChangedBy  INTEGER , clazzMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ClazzMember (clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, dateJoined, dateLeft, role, clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy) SELECT clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, dateJoined, dateLeft, role, clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy FROM ClazzMember_OLD")
                    database.execSQL("DROP TABLE ClazzMember_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_11_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_11")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzMember_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzMember_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_11_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzMemberLocalChangeSeqNum 
                    | ELSE NEXTVAL('ClazzMember_lcsn_seq') END),
                    | clazzMemberMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ClazzMember_mcsn_seq') 
                    | ELSE NEW.clazzMemberMasterChangeSeqNum END)
                    | WHERE clazzMemberUid = NEW.clazzMemberUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_11_trig 
                    |AFTER UPDATE OR INSERT ON ClazzMember 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_11_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzMember_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzMember_trk_clientId_epk_rx_csn 
                    |ON ClazzMember_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // Container
                    database.execSQL("ALTER TABLE Container RENAME to Container_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Container (  cntLocalCsn  BIGINT , cntMasterCsn  BIGINT , cntLastModBy  INTEGER , fileSize  BIGINT , containerContentEntryUid  BIGINT , cntLastModified  BIGINT , mimeType  TEXT , remarks  TEXT , mobileOptimized  BOOL , cntNumEntries  INTEGER , containerUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Container (containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries) SELECT containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries FROM Container_OLD")
                    database.execSQL("DROP TABLE Container_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_51_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_51")
                    database.execSQL("""
                    |CREATE 
                    | INDEX cnt_uid_to_most_recent 
                    |ON Container (containerContentEntryUid, cntLastModified)
                    """.trimMargin())
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Container_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Container_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_51_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Container SET cntLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cntLocalCsn 
                    | ELSE NEXTVAL('Container_lcsn_seq') END),
                    | cntMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Container_mcsn_seq') 
                    | ELSE NEW.cntMasterCsn END)
                    | WHERE containerUid = NEW.containerUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_51_trig 
                    |AFTER UPDATE OR INSERT ON Container 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_51_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Container_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Container_trk_clientId_epk_rx_csn 
                    |ON Container_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // ContentCategory
                    database.execSQL("ALTER TABLE ContentCategory RENAME to ContentCategory_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentCategory (  ctnCatContentCategorySchemaUid  BIGINT , name  TEXT , contentCategoryLocalChangeSeqNum  BIGINT , contentCategoryMasterChangeSeqNum  BIGINT , contentCategoryLastChangedBy  INTEGER , contentCategoryUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentCategory (contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy) SELECT contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy FROM ContentCategory_OLD")
                    database.execSQL("DROP TABLE ContentCategory_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_1_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_1")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentCategory_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentCategory_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_1_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategoryLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentCategory_lcsn_seq') END),
                    | contentCategoryMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentCategory_mcsn_seq') 
                    | ELSE NEW.contentCategoryMasterChangeSeqNum END)
                    | WHERE contentCategoryUid = NEW.contentCategoryUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_1_trig 
                    |AFTER UPDATE OR INSERT ON ContentCategory 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_1_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentCategory_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentCategory_trk_clientId_epk_rx_csn 
                    |ON ContentCategory_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // ContentCategorySchema
                    database.execSQL("ALTER TABLE ContentCategorySchema RENAME to ContentCategorySchema_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentCategorySchema (  schemaName  TEXT , schemaUrl  TEXT , contentCategorySchemaLocalChangeSeqNum  BIGINT , contentCategorySchemaMasterChangeSeqNum  BIGINT , contentCategorySchemaLastChangedBy  INTEGER , contentCategorySchemaUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentCategorySchema (contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy) SELECT contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy FROM ContentCategorySchema_OLD")
                    database.execSQL("DROP TABLE ContentCategorySchema_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_2_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_2")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentCategorySchema_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentCategorySchema_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_2_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategorySchemaLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentCategorySchema_lcsn_seq') END),
                    | contentCategorySchemaMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentCategorySchema_mcsn_seq') 
                    | ELSE NEW.contentCategorySchemaMasterChangeSeqNum END)
                    | WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_2_trig 
                    |AFTER UPDATE OR INSERT ON ContentCategorySchema 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_2_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentCategorySchema_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentCategorySchema_trk_clientId_epk_rx_csn 
                    |ON ContentCategorySchema_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // ContentEntryContentCategoryJoin
                    database.execSQL("ALTER TABLE ContentEntryContentCategoryJoin RENAME to ContentEntryContentCategoryJoin_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoin (  ceccjContentEntryUid  BIGINT , ceccjContentCategoryUid  BIGINT , ceccjLocalChangeSeqNum  BIGINT , ceccjMasterChangeSeqNum  BIGINT , ceccjLastChangedBy  INTEGER , ceccjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentEntryContentCategoryJoin (ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy) SELECT ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy FROM ContentEntryContentCategoryJoin_OLD")
                    database.execSQL("DROP TABLE ContentEntryContentCategoryJoin_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_3_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_3")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryContentCategoryJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryContentCategoryJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_3_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.ceccjLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentEntryContentCategoryJoin_lcsn_seq') END),
                    | ceccjMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentEntryContentCategoryJoin_mcsn_seq') 
                    | ELSE NEW.ceccjMasterChangeSeqNum END)
                    | WHERE ceccjUid = NEW.ceccjUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_3_trig 
                    |AFTER UPDATE OR INSERT ON ContentEntryContentCategoryJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_3_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentEntryContentCategoryJoin_trk_clientId_epk_rx_csn 
                    |ON ContentEntryContentCategoryJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    //ContentEntryParentChildJoin
                    database.execSQL("ALTER TABLE ContentEntryParentChildJoin RENAME to ContentEntryParentChildJoin_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoin (  cepcjChildContentEntryUid  BIGINT , cepcjParentContentEntryUid  BIGINT , childIndex  INTEGER , cepcjLocalChangeSeqNum  BIGINT , cepcjMasterChangeSeqNum  BIGINT , cepcjLastChangedBy  INTEGER , cepcjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentEntryParentChildJoin (cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy) SELECT cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy FROM ContentEntryParentChildJoin_OLD")
                    database.execSQL("DROP TABLE ContentEntryParentChildJoin_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_7_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_7")
                    database.execSQL("""
                    |CREATE 
                    | INDEX parent_child 
                    |ON ContentEntryParentChildJoin (cepcjChildContentEntryUid, cepcjParentContentEntryUid)
                    """.trimMargin())
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryParentChildJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryParentChildJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_7_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cepcjLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentEntryParentChildJoin_lcsn_seq') END),
                    | cepcjMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentEntryParentChildJoin_mcsn_seq') 
                    | ELSE NEW.cepcjMasterChangeSeqNum END)
                    | WHERE cepcjUid = NEW.cepcjUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_7_trig 
                    |AFTER UPDATE OR INSERT ON ContentEntryParentChildJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_7_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentEntryParentChildJoin_trk_clientId_epk_rx_csn 
                    |ON ContentEntryParentChildJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // ContentEntryRelatedEntryJoin
                    database.execSQL("ALTER TABLE ContentEntryRelatedEntryJoin RENAME to ContentEntryRelatedEntryJoin_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoin (  cerejContentEntryUid  BIGINT , cerejRelatedEntryUid  BIGINT , cerejLastChangedBy  INTEGER , relType  INTEGER , comment  TEXT , cerejRelLanguageUid  BIGINT , cerejLocalChangeSeqNum  BIGINT , cerejMasterChangeSeqNum  BIGINT , cerejUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO ContentEntryRelatedEntryJoin (cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum) SELECT cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum FROM ContentEntryRelatedEntryJoin_OLD")
                    database.execSQL("DROP TABLE ContentEntryRelatedEntryJoin_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_8_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_8")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryRelatedEntryJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ContentEntryRelatedEntryJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_8_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cerejLocalChangeSeqNum 
                    | ELSE NEXTVAL('ContentEntryRelatedEntryJoin_lcsn_seq') END),
                    | cerejMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ContentEntryRelatedEntryJoin_mcsn_seq') 
                    | ELSE NEW.cerejMasterChangeSeqNum END)
                    | WHERE cerejUid = NEW.cerejUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_8_trig 
                    |AFTER UPDATE OR INSERT ON ContentEntryRelatedEntryJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_8_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ContentEntryRelatedEntryJoin_trk_clientId_epk_rx_csn 
                    |ON ContentEntryRelatedEntryJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // EntityRole
                    database.execSQL("ALTER TABLE EntityRole RENAME to EntityRole_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS EntityRole (  erMasterCsn  BIGINT , erLocalCsn  BIGINT , erLastChangedBy  INTEGER , erTableId  INTEGER , erEntityUid  BIGINT , erGroupUid  BIGINT , erRoleUid  BIGINT , erUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO EntityRole (erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, erEntityUid, erGroupUid, erRoleUid) SELECT erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, erEntityUid, erGroupUid, erRoleUid FROM EntityRole_OLD")
                    database.execSQL("DROP TABLE EntityRole_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_47_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_47")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS EntityRole_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS EntityRole_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_47_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE EntityRole SET erLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.erLocalCsn 
                    | ELSE NEXTVAL('EntityRole_lcsn_seq') END),
                    | erMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('EntityRole_mcsn_seq') 
                    | ELSE NEW.erMasterCsn END)
                    | WHERE erUid = NEW.erUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_47_trig 
                    |AFTER UPDATE OR INSERT ON EntityRole 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_47_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS EntityRole_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_EntityRole_trk_clientId_epk_rx_csn 
                    |ON EntityRole_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    database.execSQL("ALTER TABLE Language RENAME to Language_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Language (  langUid  BIGINT  PRIMARY KEY  NOT NULL , name  TEXT , iso_639_1_standard  TEXT , iso_639_2_standard  TEXT , iso_639_3_standard  TEXT , langLocalChangeSeqNum  BIGINT , langMasterChangeSeqNum  BIGINT , langLastChangedBy  INTEGER )")
                    database.execSQL("INSERT INTO Language (langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy) SELECT langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy FROM Language_OLD")
                    database.execSQL("DROP TABLE Language_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_13_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_13")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Language_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Language_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_13_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Language SET langLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langLocalChangeSeqNum 
                    | ELSE NEXTVAL('Language_lcsn_seq') END),
                    | langMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Language_mcsn_seq') 
                    | ELSE NEW.langMasterChangeSeqNum END)
                    | WHERE langUid = NEW.langUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_13_trig 
                    |AFTER UPDATE OR INSERT ON Language 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_13_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Language_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Language_trk_clientId_epk_rx_csn 
                    |ON Language_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // Lanaguage Variant
                    database.execSQL("ALTER TABLE LanguageVariant RENAME to LanguageVariant_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS LanguageVariant (  langUid  BIGINT , countryCode  TEXT , name  TEXT , langVariantLocalChangeSeqNum  BIGINT , langVariantMasterChangeSeqNum  BIGINT , langVariantLastChangedBy  INTEGER , langVariantUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO LanguageVariant (langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy) SELECT langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy FROM LanguageVariant_OLD")
                    database.execSQL("DROP TABLE LanguageVariant_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_10_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_10")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS LanguageVariant_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS LanguageVariant_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_10_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE LanguageVariant SET langVariantLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langVariantLocalChangeSeqNum 
                    | ELSE NEXTVAL('LanguageVariant_lcsn_seq') END),
                    | langVariantMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('LanguageVariant_mcsn_seq') 
                    | ELSE NEW.langVariantMasterChangeSeqNum END)
                    | WHERE langVariantUid = NEW.langVariantUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_10_trig 
                    |AFTER UPDATE OR INSERT ON LanguageVariant 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_10_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS LanguageVariant_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_LanguageVariant_trk_clientId_epk_rx_csn 
                    |ON LanguageVariant_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // Location
                    database.execSQL("ALTER TABLE Location ADD COLUMN locationActive BOOL")
                    database.execSQL("ALTER TABLE Location RENAME to Location_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Location (  title  TEXT , description  TEXT , lng  TEXT , lat  TEXT , parentLocationUid  BIGINT , locationActive  BOOL , locationLocalChangeSeqNum  BIGINT , locationMasterChangeSeqNum  BIGINT , locationLastChangedBy  INTEGER , locationUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Location (locationUid, title, description, lng, lat, parentLocationUid, locationActive, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy) SELECT locationUid, title, description, lng, lat, parentLocationUid, locationActive, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy FROM Location_OLD")
                    database.execSQL("DROP TABLE Location_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_29_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_29")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Location_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Location_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_29_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Location SET locationLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.locationLocalChangeSeqNum 
                    | ELSE NEXTVAL('Location_lcsn_seq') END),
                    | locationMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Location_mcsn_seq') 
                    | ELSE NEW.locationMasterChangeSeqNum END)
                    | WHERE locationUid = NEW.locationUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_29_trig 
                    |AFTER UPDATE OR INSERT ON Location 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_29_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Location_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Location_trk_clientId_epk_rx_csn 
                    |ON Location_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // Person
                    database.execSQL("ALTER TABLE Person RENAME to Person_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Person (  username  TEXT , firstNames  TEXT , lastName  TEXT , emailAddr  TEXT , phoneNum  TEXT , gender  INTEGER , active  BOOL , admin  BOOL , personMasterChangeSeqNum  BIGINT , personLocalChangeSeqNum  BIGINT , personLastChangedBy  INTEGER , personUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Person (personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy) SELECT personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy FROM Person_OLD")
                    database.execSQL("DROP TABLE Person_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_9_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_9")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Person_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Person_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_9_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Person SET personLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personLocalChangeSeqNum 
                    | ELSE NEXTVAL('Person_lcsn_seq') END),
                    | personMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Person_mcsn_seq') 
                    | ELSE NEW.personMasterChangeSeqNum END)
                    | WHERE personUid = NEW.personUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_9_trig 
                    |AFTER UPDATE OR INSERT ON Person 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_9_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Person_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Person_trk_clientId_epk_rx_csn 
                    |ON Person_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // PersonGroup
                    database.execSQL("ALTER TABLE PersonGroup RENAME to PersonGroup_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroup (  groupMasterCsn  BIGINT , groupLocalCsn  BIGINT , groupLastChangedBy  INTEGER , groupName  TEXT , groupPersonUid  BIGINT , groupUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonGroup (groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, groupPersonUid) SELECT groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, groupPersonUid FROM PersonGroup_OLD")
                    database.execSQL("DROP TABLE PersonGroup_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_43_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_43")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonGroup_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonGroup_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_43_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonGroup SET groupLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupLocalCsn 
                    | ELSE NEXTVAL('PersonGroup_lcsn_seq') END),
                    | groupMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonGroup_mcsn_seq') 
                    | ELSE NEW.groupMasterCsn END)
                    | WHERE groupUid = NEW.groupUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_43_trig 
                    |AFTER UPDATE OR INSERT ON PersonGroup 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_43_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroup_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroup_trk_clientId_epk_rx_csn 
                    |ON PersonGroup_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // personLocationJoin
                    database.execSQL("ALTER TABLE PersonLocationJoin RENAME to PersonLocationJoin_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonLocationJoin (  personLocationPersonUid  BIGINT , personLocationLocationUid  BIGINT , plMasterCsn  BIGINT , plLocalCsn  BIGINT , plLastChangedBy  INTEGER , personLocationUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonLocationJoin (personLocationUid, personLocationPersonUid, personLocationLocationUid, plMasterCsn, plLocalCsn, plLastChangedBy) SELECT personLocationUid, personLocationPersonUid, personLocationLocationUid, plMasterCsn, plLocalCsn, plLastChangedBy FROM PersonLocationJoin_OLD")
                    database.execSQL("DROP TABLE PersonLocationJoin_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_48_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_48")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonLocationJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonLocationJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_48_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonLocationJoin SET plLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.plLocalCsn 
                    | ELSE NEXTVAL('PersonLocationJoin_lcsn_seq') END),
                    | plMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonLocationJoin_mcsn_seq') 
                    | ELSE NEW.plMasterCsn END)
                    | WHERE personLocationUid = NEW.personLocationUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_48_trig 
                    |AFTER UPDATE OR INSERT ON PersonLocationJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_48_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonLocationJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonLocationJoin_trk_clientId_epk_rx_csn 
                    |ON PersonLocationJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    // PersonPicture
                    database.execSQL("ALTER TABLE PersonPicture RENAME to PersonPicture_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonPicture (  personPicturePersonUid  BIGINT , personPictureMasterCsn  BIGINT , personPictureLocalCsn  BIGINT , personPictureLastChangedBy  INTEGER , fileSize  INTEGER , picTimestamp  INTEGER , mimeType  TEXT , personPictureUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonPicture (personPictureUid, personPicturePersonUid, personPictureMasterCsn, personPictureLocalCsn, personPictureLastChangedBy, fileSize, picTimestamp, mimeType) SELECT personPictureUid, personPicturePersonUid, personPictureMasterCsn, personPictureLocalCsn, personPictureLastChangedBy, fileSize, picTimestamp, mimeType FROM PersonPicture_OLD")
                    database.execSQL("DROP TABLE PersonPicture_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_50_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_50")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonPicture_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonPicture_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_50_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonPicture SET personPictureLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personPictureLocalCsn 
                    | ELSE NEXTVAL('PersonPicture_lcsn_seq') END),
                    | personPictureMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonPicture_mcsn_seq') 
                    | ELSE NEW.personPictureMasterCsn END)
                    | WHERE personPictureUid = NEW.personPictureUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_50_trig 
                    |AFTER UPDATE OR INSERT ON PersonPicture 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_50_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonPicture_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonPicture_trk_clientId_epk_rx_csn 
                    |ON PersonPicture_trk (clientId, epk, rx, csn)
                    """.trimMargin())


                    // Role
                    database.execSQL("ALTER TABLE Role RENAME to Role_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Role (  roleName  TEXT , roleMasterCsn  BIGINT , roleLocalCsn  BIGINT , roleLastChangedBy  INTEGER , rolePermissions  BIGINT , roleUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Role (roleUid, roleName, roleMasterCsn, roleLocalCsn, roleLastChangedBy, rolePermissions) SELECT roleUid, roleName, roleMasterCsn, roleLocalCsn, roleLastChangedBy, rolePermissions FROM Role_OLD")
                    database.execSQL("DROP TABLE Role_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_45_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_45")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Role_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Role_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_45_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Role SET roleLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.roleLocalCsn 
                    | ELSE NEXTVAL('Role_lcsn_seq') END),
                    | roleMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Role_mcsn_seq') 
                    | ELSE NEW.roleMasterCsn END)
                    | WHERE roleUid = NEW.roleUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_45_trig 
                    |AFTER UPDATE OR INSERT ON Role 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_45_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS Role_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Role_trk_clientId_epk_rx_csn 
                    |ON Role_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    //PersonGroupMember
                    database.execSQL("ALTER TABLE PersonGroupMember RENAME to PersonGroupMember_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroupMember (  groupMemberPersonUid  BIGINT , groupMemberGroupUid  BIGINT , groupMemberMasterCsn  BIGINT , groupMemberLocalCsn  BIGINT , groupMemberLastChangedBy  INTEGER , groupMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonGroupMember (groupMemberUid, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy) SELECT groupMemberUid, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy FROM PersonGroupMember_OLD")
                    database.execSQL("DROP TABLE PersonGroupMember_OLD")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_44_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_44")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonGroupMember_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonGroupMember_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_44_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonGroupMember SET groupMemberLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupMemberLocalCsn 
                    | ELSE NEXTVAL('PersonGroupMember_lcsn_seq') END),
                    | groupMemberMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonGroupMember_mcsn_seq') 
                    | ELSE NEW.groupMemberMasterCsn END)
                    | WHERE groupMemberUid = NEW.groupMemberUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_44_trig 
                    |AFTER UPDATE OR INSERT ON PersonGroupMember 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_44_fn()
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroupMember_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroupMember_trk_clientId_epk_rx_csn 
                    |ON PersonGroupMember_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_4")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_5")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_4_fn")
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_5_fn")

                }

            })



            builder.addMigrations(MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28,
                    MIGRATION_29_30_TRIGGERS, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32,
                    MIGRATION_36_37)

            return builder
        }
    }


}
