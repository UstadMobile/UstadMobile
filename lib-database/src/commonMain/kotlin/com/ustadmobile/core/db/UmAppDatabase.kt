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

            builder.addMigrations(MIGRATION_32_33, MIGRATION_33_34, MIGRATION_33_34,
                    MIGRATION_35_36, MIGRATION_36_37)

            return builder
        }
    }


}
