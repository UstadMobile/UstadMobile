package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.MinReplicationVersion
import com.ustadmobile.door.entities.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigration
import com.ustadmobile.door.migration.DoorMigrationStatementList
import com.ustadmobile.door.migration.DoorMigrationSync
import com.ustadmobile.door.util.DoorSqlGenerator
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_NO_DELETE
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_STUDENT_GROUP
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.FLAG_TEACHER_GROUP
import com.ustadmobile.lib.util.ext.fixTincan
import com.ustadmobile.lib.util.randomString
import kotlin.js.JsName
import kotlin.jvm.JvmField

@Database(entities = [NetworkNode::class,
    ClazzLog::class, ClazzLogAttendanceRecord::class,
    Schedule::class, DateRange::class, HolidayCalendar::class, Holiday::class,
    ScheduledCheck::class,
    AuditLog::class, CustomField::class, CustomFieldValue::class, CustomFieldValueOption::class,
    Person::class,
    Clazz::class, ClazzEnrolment::class, LeavingReason::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    EntityRole::class, PersonGroup::class, PersonGroupMember::class,
    PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, XObjectEntity::class, StatementEntity::class,
    ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class, XLangMapEntry::class,
    SyncNode::class, LocallyAvailableContainer::class, ContainerETag::class,
    School::class,
    SchoolMember::class, Comments::class,
    Report::class,
    Site::class, ContainerImportJob::class,
    LearnerGroup::class, LearnerGroupMember::class,
    GroupLearningSession::class,
    SiteTerms::class, ClazzContentJoin::class,
    PersonParentJoin::class,
    ScopedGrant::class,
    ErrorReport::class,
    ClazzAssignment::class, ClazzAssignmentContentJoin::class,
    ClazzAssignmentRollUp::class,
    PersonAuth2::class,
    UserSession::class,
    ContentJob::class, ContentJobItem::class, CourseBlock::class,

    //Door Helper entities
    SqliteChangeSeqNums::class,
    UpdateNotification::class,
    ChangeLog::class,
    ZombieAttachmentData::class,
    DoorNode::class,
    ReplicationStatus::class,

    ClazzLogReplicate::class,
    ClazzLogAttendanceRecordReplicate::class,
    ScheduleReplicate::class,
    HolidayCalendarReplicate::class,
    HolidayReplicate::class,
    PersonReplicate::class,
    ClazzReplicate::class,
    ClazzEnrolmentReplicate::class,
    LeavingReasonReplicate::class,
    ContentEntryReplicate::class,
    ContentEntryContentCategoryJoinReplicate::class,
    ContentEntryParentChildJoinReplicate::class,
    ContentEntryRelatedEntryJoinReplicate::class,
    ContentCategorySchemaReplicate::class,
    ContentCategoryReplicate::class,
    LanguageReplicate::class,
    LanguageVariantReplicate::class,
    PersonGroupReplicate::class,
    PersonGroupMemberReplicate::class,
    PersonPictureReplicate::class,
    ContainerReplicate::class,
    VerbEntityReplicate::class,
    XObjectEntityReplicate::class,
    StatementEntityReplicate::class,
    ContextXObjectStatementJoinReplicate::class,
    AgentEntityReplicate::class,
    StateEntityReplicate::class,
    StateContentEntityReplicate::class,
    XLangMapEntryReplicate::class,
    SchoolReplicate::class,
    SchoolMemberReplicate::class,
    CommentsReplicate::class,
    ReportReplicate::class,
    SiteReplicate::class,
    LearnerGroupReplicate::class,
    LearnerGroupMemberReplicate::class,
    GroupLearningSessionReplicate::class,
    SiteTermsReplicate::class,
    ClazzContentJoinReplicate::class,
    PersonParentJoinReplicate::class,
    ScopedGrantReplicate::class,
    ErrorReportReplicate::class,
    ClazzAssignmentReplicate::class,
    ClazzAssignmentContentJoinReplicate::class,
    PersonAuth2Replicate::class,
    UserSessionReplicate::class
    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_TRACKER_ENTITIES

], version = 100)
@MinReplicationVersion(60)
abstract class UmAppDatabase : DoorDatabase() {

    /*
        Changes from 38-39:
        1. Added personGroupUid to Person
        2. Added personGroupFlag to PersonGroup
        3. Removed groupPersonUid from PersonGroup

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


    /**
     * Preload a few entities where we have fixed UIDs for fixed items (e.g. Xapi Verbs)
     */
    fun preload() {
        verbDao.initPreloadedVerbs()
        reportDao.initPreloadedTemplates()
        leavingReasonDao.initPreloadedLeavingReasons()
        languageDao.initPreloadedLanguages()
    }

    @JsName("networkNodeDao")
    abstract val networkNodeDao: NetworkNodeDao

    @JsName("personDao")
    abstract val personDao: PersonDao

    @JsName("clazzDao")
    abstract val clazzDao: ClazzDao

    @JsName("courseBlockDao")
    abstract val courseBlockDao: CourseBlockDao

    @JsName("clazzEnrolmentDao")
    abstract val clazzEnrolmentDao: ClazzEnrolmentDao

    @JsName("leavingReasonDao")
    abstract val leavingReasonDao: LeavingReasonDao

    @JsName("contentEntryDao")
    abstract val contentEntryDao: ContentEntryDao

    @JsName("contentEntryContentCategoryJoinDao")
    abstract val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao

    @JsName("contentEntryParentChildJoinDao")
    abstract val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao

    @JsName("contentEntryRelatedEntryJoinDao")
    abstract val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao


    @JsName("clazzContentJoinDao")
    abstract val clazzContentJoinDao: ClazzContentJoinDao

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

    @JsName("connectivityStatusDao")
    abstract val connectivityStatusDao: ConnectivityStatusDao

    @JsName("containerDao")
    abstract val containerDao: ContainerDao

    @JsName("containerEntryDao")
    abstract val containerEntryDao: ContainerEntryDao

    @JsName("containerEntryFileDao")
    abstract val containerEntryFileDao: ContainerEntryFileDao

    @JsName("containerETagDao")
    abstract val containerETagDao: ContainerETagDao

    @JsName("verbDao")
    abstract val verbDao: VerbDao

    @JsName("xObjectDao")
    abstract val xObjectDao: XObjectDao

    @JsName("reportDao")
    abstract val reportDao: ReportDao

    @JsName("containerImportJobDao")
    abstract val containerImportJobDao: ContainerImportJobDao

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

    @JsName("learnerGroupDao")
    abstract val learnerGroupDao: LearnerGroupDao

    @JsName("learnerGroupMemberDao")
    abstract val learnerGroupMemberDao: LearnerGroupMemberDao

    @JsName("groupLearningSessionDao")
    abstract val groupLearningSessionDao: GroupLearningSessionDao

    abstract val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
    abstract val clazzLogDao: ClazzLogDao
    abstract val customFieldDao: CustomFieldDao
    abstract val customFieldValueDao: CustomFieldValueDao
    abstract val customFieldValueOptionDao: CustomFieldValueOptionDao

    abstract val scheduleDao: ScheduleDao

    abstract val scheduledCheckDao: ScheduledCheckDao

    abstract val holidayCalendarDao: HolidayCalendarDao
    abstract val holidayDao: HolidayDao
    abstract val schoolDao: SchoolDao

    @JsName("xLangMapEntryDao")
    abstract val xLangMapEntryDao: XLangMapEntryDao

    abstract val locallyAvailableContainerDao: LocallyAvailableContainerDao

    @JsName("schoolMemberDao")
    abstract val schoolMemberDao: SchoolMemberDao

    @JsName("clazzAssignmentDao")
    abstract val clazzAssignmentDao: ClazzAssignmentDao

    @JsName("clazzAssignmentContentJoinDao")
    abstract val clazzAssignmentContentJoinDao: ClazzAssignmentContentJoinDao

    @JsName("cacheClazzAssignmentDao")
    abstract val clazzAssignmentRollUpDao: ClazzAssignmentRollUpDao

    @JsName("commentsDao")
    abstract val commentsDao: CommentsDao

    @JsName("syncNodeDao")
    abstract val syncNodeDao: SyncNodeDao

    abstract val siteDao: SiteDao

    abstract val siteTermsDao: SiteTermsDao

    abstract val personParentJoinDao: PersonParentJoinDao

    abstract val scopedGrantDao: ScopedGrantDao

    abstract val errorReportDao: ErrorReportDao

    abstract val personAuth2Dao: PersonAuth2Dao

    abstract val userSessionDao: UserSessionDao

    abstract val contentJobItemDao: ContentJobItemDao

    abstract val contentJobDao: ContentJobDao

    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_SYNCDAO


    companion object {

        const val TAG_DB = DoorTag.TAG_DB

        const val TAG_REPO = DoorTag.TAG_REPO

        val MIGRATION_44_45 = DoorMigrationSync(44, 45) { database ->
            database.execSQL("DROP TABLE ContainerUploadJob")

            if (database.dbType() == DoorDbType.SQLITE) {
                database.execSQL("CREATE TABLE IF NOT EXISTS ContainerImportJob (`cijUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cijContainerUid` INTEGER NOT NULL, `cijFilePath` TEXT, `cijContainerBaseDir` TEXT, `cijContentEntryUid` INTEGER NOT NULL, `cijMimeType` TEXT, `cijSessionId` TEXT, `cijJobStatus` INTEGER NOT NULL, `cijBytesSoFar` INTEGER NOT NULL, `cijImportCompleted` INTEGER NOT NULL, `cijContentLength` INTEGER NOT NULL, `cijContainerEntryFileUids` TEXT, `cijConversionParams` TEXT)")
            } else if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("CREATE TABLE IF NOT EXISTS ContainerImportJob (  cijContainerUid  BIGINT , cijFilePath  TEXT , cijContainerBaseDir  TEXT , cijContentEntryUid  BIGINT , cijMimeType  TEXT , cijSessionId  TEXT , cijJobStatus  INTEGER , cijBytesSoFar  BIGINT , cijImportCompleted  BOOL , cijContentLength  BIGINT , cijContainerEntryFileUids  TEXT , cijConversionParams  TEXT , cijUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
            }
        }

        val MIGRATION_45_46 = DoorMigrationSync(45, 46) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("""
                    Update ClazzWorkQuestionResponse
                    SET clazzWorkQuestionResponseLCB = (SELECT nodeClientId from SyncNode)
                    WHERE
                    clazzWorkQuestionResponseLCB = 0
                """.trimIndent())

            }
        }

        /**
         * Add indexes to improve performance of queries that check permissions
         */
        val MIGRATION_46_47 = DoorMigrationSync(46, 47) { database ->
            database.execSQL("CREATE INDEX index_ClazzMember_clazzMemberPersonUid_clazzMemberClazzUid ON ClazzMember (clazzMemberPersonUid, clazzMemberClazzUid)")
            database.execSQL("CREATE INDEX index_ClazzMember_clazzMemberClazzUid_clazzMemberPersonUid ON ClazzMember (clazzMemberClazzUid, clazzMemberPersonUid)")
            database.execSQL("CREATE INDEX index_EntityRole_erGroupUid_erRoleUid_erTableId ON EntityRole (erGroupUid, erRoleUid, erTableId)")
            database.execSQL("CREATE INDEX index_Role_rolePermissions ON Role(rolePermissions)")

            //Add a PersonGroup for Admin
            if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("""
                    INSERT INTO PersonGroup(groupName, groupActive, personGroupFlag, groupMasterCsn, groupLocalCsn, groupLastChangedBy) 
                    SELECT 'PGA' || person.personUid AS groupName, 
                    true as groupActive,
                    1 as personGroupFlag,
                    0 as groupMasterCsn,
                    0 as groupLocalCsn,
                    0 as groupLastChangedBy
                    FROM person
                    where admin = true
                    AND personGroupUid = 0""")
                database.execSQL("""
                    UPDATE Person SET
                    personGroupUid = (SELECT groupUid FROM PersonGroup WHERE groupName = ('PGA' || Person.personUid) LIMIT 1)
                    WHERE
                    admin = true AND personGroupUid = 0
                """)
                database.execSQL("""
                    INSERT INTO PersonGroupMember(groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy)
                    SELECT Person.personUid AS groupMemberPersonUid,
                    Person.personGroupUid AS groupMemberGroupUid,
                    0 AS groupMemberMasterCsn,
                    0 AS groupMemberLocalCsn,
                    0 AS groupMemberLastChangedBy
                    FROM Person
                    WHERE admin = true
                    AND (SELECT COUNT(*) FROM PersonGroupMember WHERE PersonGroupmember.groupMemberGroupUid = Person.personGroupUid) = 0
                """)
            }

        }

        val MIGRATION_47_48 = DoorMigrationSync(47, 48) { database ->
            database.execSQL("CREATE INDEX " +
                    "index_ClazzMember_clazzMemberClazzUid_clazzMemberRole " +
                    "ON ClazzMember (clazzMemberClazzUid, clazzMemberRole)")
            database.execSQL("CREATE INDEX " +
                    "index_SchoolMember_schoolMemberSchoolUid_schoolMemberActive_schoolMemberRole " +
                    "ON SchoolMember (schoolMemberSchoolUid, schoolMemberActive, schoolMemberRole)")
        }

        val MIGRATION_48_49 = DoorMigrationSync(48, 49) { database ->
            database.execSQL("""ALTER TABLE ScrapeRun ADD COLUMN conversionParams TEXT""".trimMargin())

            database.execSQL("""
              |CREATE 
              | INDEX index_ScrapeQueueItem_status_itemType 
              |ON ScrapeQueueItem (status, itemType)
              """.trimMargin())

            if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("ALTER TABLE ScrapeRun RENAME to ScrapeRun_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS ScrapeRun (  scrapeType  TEXT , scrapeRunStatus  INTEGER  NOT NULL , conversionParams  TEXT , scrapeRunUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("INSERT INTO ScrapeRun (scrapeRunUid, scrapeType, scrapeRunStatus, conversionParams) SELECT scrapeRunUid, scrapeType, status, conversionParams FROM ScrapeRun_OLD")
                database.execSQL("DROP TABLE ScrapeRun_OLD")

            } else if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("""ALTER TABLE ScrapeRun RENAME COLUMN status to scrapeRunStatus
                    """.trimMargin())
            }


        }

        val MIGRATION_49_50 = DoorMigrationSync(49, 50) { database ->
            database.execSQL("DROP TABLE TimeZoneEntity")
        }

        val MIGRATION_50_51 = DoorMigrationSync(50, 51) { database ->
            database.execSQL("DROP TABLE IF EXISTS SqliteSyncablePk")
        }

        //One off server only change to update clazz end time default to Long.MAX_VALUE
        val MIGRATION_51_52 = DoorMigrationSync(51, 52) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("UPDATE Clazz SET clazzEndTime = ${systemTimeInMillis()}," +
                        "clazzLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) " +
                        "WHERE clazzEndTime = 0")
            }
        }

        //Add the WorkspaceTerms syncable entity
        val MIGRATION_52_53 = DoorMigrationSync(52, 53) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("CREATE TABLE IF NOT EXISTS WorkspaceTerms (  termsHtml  TEXT , wtLang  TEXT , wtLastChangedBy  INTEGER  NOT NULL , wtPrimaryCsn  BIGINT  NOT NULL , wtLocalCsn  BIGINT  NOT NULL , wtUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS WorkspaceTerms_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS WorkspaceTerms_lcsn_seq")
                database.execSQL("""
                  |CREATE OR REPLACE FUNCTION 
                  | inccsn_272_fn() RETURNS trigger AS ${'$'}${'$'}
                  | BEGIN  
                  | UPDATE WorkspaceTerms SET wtLocalCsn =
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.wtLocalCsn 
                  | ELSE NEXTVAL('WorkspaceTerms_lcsn_seq') END),
                  | wtPrimaryCsn = 
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                  | THEN NEXTVAL('WorkspaceTerms_mcsn_seq') 
                  | ELSE NEW.wtPrimaryCsn END)
                  | WHERE wtUid = NEW.wtUid;
                  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  | SELECT 272, NEW.wtUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
                  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
                  | RETURN null;
                  | END ${'$'}${'$'}
                  | LANGUAGE plpgsql
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER inccsn_272_trig 
                  |AFTER UPDATE OR INSERT ON WorkspaceTerms 
                  |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                  |EXECUTE PROCEDURE inccsn_272_fn()
                  """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS WorkspaceTerms_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_WorkspaceTerms_trk_clientId_epk_csn 
                  |ON WorkspaceTerms_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_WorkspaceTerms_trk_epk_clientId 
                  |ON WorkspaceTerms_trk (epk, clientId)
                  """.trimMargin())
            } else {
                database.execSQL("CREATE TABLE IF NOT EXISTS WorkspaceTerms (  termsHtml  TEXT , wtLang  TEXT , wtLastChangedBy  INTEGER  NOT NULL , wtPrimaryCsn  INTEGER  NOT NULL , wtLocalCsn  INTEGER  NOT NULL , wtUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                  |CREATE TRIGGER INS_LOC_272
                  |AFTER INSERT ON WorkspaceTerms
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                  |    NEW.wtLocalCsn = 0)
                  |BEGIN
                  |    UPDATE WorkspaceTerms
                  |    SET wtPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |    WHERE wtUid = NEW.wtUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums
                  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |    WHERE sCsnTableId = 272;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER INS_PRI_272
                  |            AFTER INSERT ON WorkspaceTerms
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                  |                NEW.wtPrimaryCsn = 0)
                  |            BEGIN
                  |                UPDATE WorkspaceTerms
                  |                SET wtPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |                WHERE wtUid = NEW.wtUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 272;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 272, NEW.wtUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER UPD_LOC_272
                  |AFTER UPDATE ON WorkspaceTerms
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                  |    AND (NEW.wtLocalCsn == OLD.wtLocalCsn OR
                  |        NEW.wtLocalCsn == 0))
                  |BEGIN
                  |    UPDATE WorkspaceTerms
                  |    SET wtLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 272) 
                  |    WHERE wtUid = NEW.wtUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums 
                  |    SET sCsnNextLocal = sCsnNextLocal + 1
                  |    WHERE sCsnTableId = 272;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER UPD_PRI_272
                  |            AFTER UPDATE ON WorkspaceTerms
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                  |                AND (NEW.wtPrimaryCsn == OLD.wtPrimaryCsn OR
                  |                    NEW.wtPrimaryCsn == 0))
                  |            BEGIN
                  |                UPDATE WorkspaceTerms
                  |                SET wtPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |                WHERE wtUid = NEW.wtUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 272;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 272, NEW.wtUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())
                database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(272, 1, 1)")
                database.execSQL("CREATE TABLE IF NOT EXISTS WorkspaceTerms_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL, pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_WorkspaceTerms_trk_clientId_epk_csn 
                  |ON WorkspaceTerms_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_WorkspaceTerms_trk_epk_clientId 
                  |ON WorkspaceTerms_trk (epk, clientId)
                  """.trimMargin())
            }

        }

        val MIGRATION_53_54 = DoorMigrationSync(53, 54) { database ->
            database.execSQL("ALTER TABLE Language ADD COLUMN Language_Type TEXT")

            //Change WorkSpace into a SyncableEntity
            if (database.dbType() == DoorDbType.POSTGRES) {
                //TODO: sync annotation add columns to table
                database.execSQL("CREATE TABLE IF NOT EXISTS Site_trk (  epk  BIGINT NOT NULL, clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  BOOL NOT NULL, reqId  INTEGER NOT NULL, ts  BIGINT NOT NULL, pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_Site_trk_clientId_epk_csn 
                  |ON Site_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_Site_trk_epk_clientId 
                  |ON Site_trk (epk, clientId)
                  """.trimMargin())

                database.execSQL("ALTER TABLE WorkSpace RENAME TO Site")
                database.execSQL("ALTER SEQUENCE workspace_uid_seq RENAME TO site_siteuid_seq")
                database.execSQL("ALTER TABLE Site RENAME COLUMN uid TO siteUid")
                database.execSQL("ALTER TABLE Site ADD COLUMN sitePcsn BIGINT DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE Site ADD COLUMN siteLcsn BIGINT DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE Site ADD COLUMN siteLcb INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE Site RENAME COLUMN name to siteName")

                database.execSQL("CREATE SEQUENCE IF NOT EXISTS Site_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS Site_lcsn_seq")

                database.execSQL("""
                  |CREATE OR REPLACE FUNCTION 
                  | inccsn_189_fn() RETURNS trigger AS ${'$'}${'$'}
                  | BEGIN  
                  | UPDATE Site SET siteLcsn =
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.siteLcsn 
                  | ELSE NEXTVAL('Site_lcsn_seq') END),
                  | sitePcsn = 
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                  | THEN NEXTVAL('Site_mcsn_seq') 
                  | ELSE NEW.sitePcsn END)
                  | WHERE siteUid = NEW.siteUid;
                  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  | SELECT 189, NEW.siteUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
                  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
                  | RETURN null;
                  | END ${'$'}${'$'}
                  | LANGUAGE plpgsql
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER inccsn_189_trig 
                  |AFTER UPDATE OR INSERT ON Site 
                  |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                  |EXECUTE PROCEDURE inccsn_189_fn()
                  """.trimMargin())


                database.execSQL("DROP TABLE WorkspaceTerms")


                database.execSQL("CREATE TABLE IF NOT EXISTS SiteTerms (  termsHtml  TEXT , sTermsLang  TEXT , sTermsLangUid  BIGINT  NOT NULL , sTermsActive  BOOL  NOT NULL , sTermsLastChangedBy  INTEGER  NOT NULL , sTermsPrimaryCsn  BIGINT  NOT NULL , sTermsLocalCsn  BIGINT  NOT NULL , sTermsUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS SiteTerms_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS SiteTerms_lcsn_seq")
                database.execSQL("""
                  |CREATE OR REPLACE FUNCTION 
                  | inccsn_272_fn() RETURNS trigger AS ${'$'}${'$'}
                  | BEGIN  
                  | UPDATE SiteTerms SET sTermsLocalCsn =
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.sTermsLocalCsn 
                  | ELSE NEXTVAL('SiteTerms_lcsn_seq') END),
                  | sTermsPrimaryCsn = 
                  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                  | THEN NEXTVAL('SiteTerms_mcsn_seq') 
                  | ELSE NEW.sTermsPrimaryCsn END)
                  | WHERE sTermsUid = NEW.sTermsUid;
                  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  | SELECT 272, NEW.sTermsUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
                  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
                  | RETURN null;
                  | END ${'$'}${'$'}
                  | LANGUAGE plpgsql
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER inccsn_272_trig 
                  |AFTER UPDATE OR INSERT ON SiteTerms 
                  |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                  |EXECUTE PROCEDURE inccsn_272_fn()
                  """.trimMargin())

                database.execSQL("CREATE TABLE IF NOT EXISTS SiteTerms_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_SiteTerms_trk_clientId_epk_csn 
                  |ON SiteTerms_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_SiteTerms_trk_epk_clientId 
                  |ON SiteTerms_trk (epk, clientId)
                  """.trimMargin())

            } else {
                //Create site table as a syncable entity
                database.execSQL("CREATE TABLE IF NOT EXISTS Site (  sitePcsn  INTEGER  NOT NULL , siteLcsn  INTEGER  NOT NULL , siteLcb  INTEGER  NOT NULL , siteName  TEXT , guestLogin  INTEGER  NOT NULL , registrationAllowed  INTEGER  NOT NULL , siteUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                    INSERT INTO Site (siteUid, sitePcsn, siteLcsn, siteLcb, siteName, guestLogin, registrationAllowed) 
                    SELECT uid AS siteUid, 0 AS sitePcsn, 0 AS siteLcsn, 0 AS siteLcb, name AS siteName, guestLogin, registrationAllowed 
                    FROM WorkSpace""")
                database.execSQL("DROP TABLE WorkSpace")

                database.execSQL("CREATE TABLE IF NOT EXISTS Site_trk (  epk  INTEGER NOT NULL, clientId  INTEGER  NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL, reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL, pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_Site_trk_clientId_epk_csn 
                  |ON Site_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_Site_trk_epk_clientId 
                  |ON Site_trk (epk, clientId)
                  """.trimMargin())


                database.execSQL("""
                  |CREATE TRIGGER INS_LOC_189
                  |AFTER INSERT ON Site
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                  |    NEW.siteLcsn = 0)
                  |BEGIN
                  |    UPDATE Site
                  |    SET sitePcsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 189)
                  |    WHERE siteUid = NEW.siteUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums
                  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |    WHERE sCsnTableId = 189;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER INS_PRI_189
                  |            AFTER INSERT ON Site
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                  |                NEW.sitePcsn = 0)
                  |            BEGIN
                  |                UPDATE Site
                  |                SET sitePcsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 189)
                  |                WHERE siteUid = NEW.siteUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 189;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 189, NEW.siteUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER UPD_LOC_189
                  |AFTER UPDATE ON Site
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                  |    AND (NEW.siteLcsn == OLD.siteLcsn OR
                  |        NEW.siteLcsn == 0))
                  |BEGIN
                  |    UPDATE Site
                  |    SET siteLcsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 189) 
                  |    WHERE siteUid = NEW.siteUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums 
                  |    SET sCsnNextLocal = sCsnNextLocal + 1
                  |    WHERE sCsnTableId = 189;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER UPD_PRI_189
                  |            AFTER UPDATE ON Site
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                  |                AND (NEW.sitePcsn == OLD.sitePcsn OR
                  |                    NEW.sitePcsn == 0))
                  |            BEGIN
                  |                UPDATE Site
                  |                SET sitePcsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 189)
                  |                WHERE siteUid = NEW.siteUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 189;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 189, NEW.siteUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())

                //Create SiteTerms as a syncable entity that replaces WorkspaceTerms
                database.execSQL("DROP TABLE WorkspaceTerms")
                database.execSQL("DROP TABLE WorkspaceTerms_trk")
                database.execSQL("CREATE TABLE IF NOT EXISTS SiteTerms (  termsHtml  TEXT , sTermsLang  TEXT , sTermsLangUid  INTEGER  NOT NULL , sTermsActive  INTEGER  NOT NULL , sTermsLastChangedBy  INTEGER  NOT NULL , sTermsPrimaryCsn  INTEGER  NOT NULL , sTermsLocalCsn  INTEGER  NOT NULL , sTermsUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")

                database.execSQL("""
                  |CREATE TRIGGER INS_LOC_272
                  |AFTER INSERT ON SiteTerms
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                  |    NEW.sTermsLocalCsn = 0)
                  |BEGIN
                  |    UPDATE SiteTerms
                  |    SET sTermsPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |    WHERE sTermsUid = NEW.sTermsUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums
                  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |    WHERE sCsnTableId = 272;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER INS_PRI_272
                  |            AFTER INSERT ON SiteTerms
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                  |                NEW.sTermsPrimaryCsn = 0)
                  |            BEGIN
                  |                UPDATE SiteTerms
                  |                SET sTermsPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |                WHERE sTermsUid = NEW.sTermsUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 272;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 272, NEW.sTermsUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER UPD_LOC_272
                  |AFTER UPDATE ON SiteTerms
                  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                  |    AND (NEW.sTermsLocalCsn == OLD.sTermsLocalCsn OR
                  |        NEW.sTermsLocalCsn == 0))
                  |BEGIN
                  |    UPDATE SiteTerms
                  |    SET sTermsLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 272) 
                  |    WHERE sTermsUid = NEW.sTermsUid;
                  |    
                  |    UPDATE SqliteChangeSeqNums 
                  |    SET sCsnNextLocal = sCsnNextLocal + 1
                  |    WHERE sCsnTableId = 272;
                  |END
                  """.trimMargin())
                database.execSQL("""
                  |            CREATE TRIGGER UPD_PRI_272
                  |            AFTER UPDATE ON SiteTerms
                  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                  |                AND (NEW.sTermsPrimaryCsn == OLD.sTermsPrimaryCsn OR
                  |                    NEW.sTermsPrimaryCsn == 0))
                  |            BEGIN
                  |                UPDATE SiteTerms
                  |                SET sTermsPrimaryCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 272)
                  |                WHERE sTermsUid = NEW.sTermsUid;
                  |                
                  |                UPDATE SqliteChangeSeqNums
                  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                  |                WHERE sCsnTableId = 272;
                  |                
                  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                  |SELECT 272, NEW.sTermsUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                  |            END
                  """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS SiteTerms_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL, reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL, pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                  |CREATE 
                  | INDEX index_SiteTerms_trk_clientId_epk_csn 
                  |ON SiteTerms_trk (clientId, epk, csn)
                  """.trimMargin())
                database.execSQL("""
                  |CREATE 
                  |UNIQUE INDEX index_SiteTerms_trk_epk_clientId 
                  |ON SiteTerms_trk (epk, clientId)
                  """.trimMargin())

            }

        }

        val MIGRATION_54_55 = DoorMigrationSync(54, 55) { database ->
            database.execSQL("ALTER TABLE PersonPicture ADD COLUMN personPictureUri TEXT")
            database.execSQL("ALTER TABLE PersonPicture ADD COLUMN personPictureMd5 TEXT")
        }

        //Add triggers that check for Zombie attachments
        val MIGRATION_55_56 = DoorMigrationSync(55, 56) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {
                database.execSQL("CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableName  TEXT , zaPrimaryKey  INTEGER  NOT NULL , zaUri  TEXT , zaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("""
                    CREATE TRIGGER ATTUPD_PersonPicture
                    AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
                    OLD.personPictureMd5 IS NOT NULL AND (SELECT COUNT(*) FROM PersonPicture WHERE personPictureMd5 = OLD.personPictureMd5) = 0
                    BEGIN
                    INSERT INTO ZombieAttachmentData(zaTableName, zaPrimaryKey, zaUri) VALUES('PersonPicture', OLD.personPictureUid, OLD.personPictureUri);
                    END""")
            } else {
                database.execSQL("CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableName  TEXT , zaPrimaryKey  BIGINT  NOT NULL , zaUri  TEXT , zaUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                  |CREATE OR REPLACE FUNCTION attach_PersonPicture_fn() RETURNS trigger AS ${'$'}${'$'}
                  |BEGIN
                  |INSERT INTO ZombieAttachmentData(zaTableName, zaPrimaryKey, zaUri) 
                  |SELECT 'PersonPicture' AS zaTableName, OLD.personPictureUid AS zaPrimaryKey, OLD.personPictureUri AS zaUri
                  |WHERE (SELECT COUNT(*) FROM PersonPicture WHERE personPictureMd5 = OLD.personPictureMd5) = 0;
                  |RETURN null;
                  |END ${'$'}${'$'}
                  |LANGUAGE plpgsql
                  """.trimMargin())
                database.execSQL("""
                  |CREATE TRIGGER attach_PersonPicture_trig
                  |AFTER UPDATE ON PersonPicture
                  |FOR EACH ROW WHEN (OLD.personPictureUri IS NOT NULL)
                  |EXECUTE PROCEDURE attach_PersonPicture_fn();
                  """.trimMargin())
            }
        }

        val MIGRATION_56_57 = DoorMigrationSync(56, 57) { database ->
            database.execSQL("""
                UPDATE ContainerEntryFile SET 
                cefPath = REPLACE(cefPath, '/build/storage/singleton/container/', '/data/singleton/container/')
                WHERE cefPath LIKE '%/build/storage/singleton/container/%'
            """.trimIndent())

        }


        val MIGRATION_57_58 = DoorMigrationSync(57, 58) { database ->
            database.execSQL("DROP TABLE IF EXISTS ReportFilter")
            database.execSQL("DROP TABLE IF EXISTS ReportFilter_trk")

            // update statementVerb
            database.execSQL("""UPDATE StatementEntity SET statementVerbUid = 
                ${VerbEntity.VERB_PASSED_UID} WHERE statementVerbUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_PASSED_URL}')""".trimMargin())
            database.execSQL("""UPDATE StatementEntity SET statementVerbUid = 
                ${VerbEntity.VERB_FAILED_UID} WHERE statementVerbUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_FAILED_URL}')""".trimMargin())

            // update subStatementVerb
            database.execSQL("""UPDATE StatementEntity SET substatementVerbUid = 
                ${VerbEntity.VERB_PASSED_UID} WHERE substatementVerbUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_PASSED_URL}')""".trimMargin())
            database.execSQL("""UPDATE StatementEntity SET substatementVerbUid = 
                ${VerbEntity.VERB_FAILED_UID} WHERE substatementVerbUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_FAILED_URL}')""".trimMargin())

            // update langmap
            database.execSQL("""UPDATE XLangMapEntry SET verbLangMapUid = 
                ${VerbEntity.VERB_PASSED_UID} WHERE verbLangMapUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_PASSED_URL}')""".trimMargin())
            database.execSQL("""UPDATE XLangMapEntry SET verbLangMapUid = 
                ${VerbEntity.VERB_FAILED_UID} WHERE verbLangMapUid IN (SELECT verbUid 
                FROM VerbEntity WHERE urlId = '${VerbEntity.VERB_FAILED_URL}')""".trimMargin())


            if (database.dbType() == DoorDbType.POSTGRES) {

                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS reportSeries TEXT""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS reportDescription TEXT""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS fromRelTo INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS fromRelOffSet INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS fromRelUnit INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS toRelTo INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS toRelOffSet INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS toRelUnit INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS priority INTEGER""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS reportDateRangeSelection INTEGER""")

                database.execSQL("""ALTER TABLE Report ADD COLUMN IF NOT EXISTS isTemplate BOOL DEFAULT FALSE""".trimMargin())
                database.execSQL("""ALTER TABLE Report 
                    DROP COLUMN IF EXISTS chartType""".trimMargin())
                database.execSQL("""ALTER TABLE Report 
                    DROP COLUMN IF EXISTS yAxis""".trimMargin())
                database.execSQL("""ALTER TABLE Report 
                    DROP COLUMN IF EXISTS subGroup""".trimMargin())

                database.execSQL("ALTER TABLE StatementEntity ADD COLUMN IF NOT EXISTS contentEntryRoot BOOL DEFAULT FALSE")
                database.execSQL("""UPDATE StatementEntity SET contentEntryRoot = true 
                    WHERE statementUid IN (select statementUid from StatementEntity 
                    LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = StatementEntity.statementContentEntryUid 
                    LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid 
                    WHERE XObjectEntity.objectId = ContentEntry.entryId)""".trimMargin())

                database.execSQL("""ALTER TABLE VerbEntity ADD COLUMN IF NOT EXISTS verbInActive BOOL DEFAULT FALSE""")
                database.execSQL("""UPDATE VerbEntity SET verbInActive = TRUE WHERE 
                    urlId = '${VerbEntity.VERB_PASSED_URL}' AND verbUid != ${VerbEntity.VERB_PASSED_UID}""".trimMargin())
                database.execSQL("""UPDATE VerbEntity SET verbInActive = TRUE WHERE 
                    urlId = '${VerbEntity.VERB_FAILED_URL}' AND verbUid != ${VerbEntity.VERB_FAILED_UID}""".trimMargin())

            } else if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("""ALTER TABLE Report ADD COLUMN reportSeries TEXT""".trimMargin())
                database.execSQL("""ALTER TABLE Report ADD COLUMN reportDescription TEXT""".trimMargin())
                database.execSQL("""ALTER TABLE Report 
                    ADD COLUMN isTemplate INTEGER""".trimMargin())

                database.execSQL("ALTER TABLE Report RENAME to Report_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS Report (`reportUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `reportOwnerUid` INTEGER NOT NULL, `xAxis` INTEGER NOT NULL, `reportDateRangeSelection` INTEGER NOT NULL, `fromDate` INTEGER NOT NULL, `fromRelTo` INTEGER NOT NULL, `fromRelOffSet` INTEGER NOT NULL, `fromRelUnit` INTEGER NOT NULL, `toDate` INTEGER NOT NULL, `toRelTo` INTEGER NOT NULL, `toRelOffSet` INTEGER NOT NULL, `toRelUnit` INTEGER NOT NULL, `reportTitle` TEXT, `reportDescription` TEXT, `reportSeries` TEXT, `reportInactive` INTEGER NOT NULL, `isTemplate` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `reportMasterChangeSeqNum` INTEGER NOT NULL, `reportLocalChangeSeqNum` INTEGER NOT NULL, `reportLastChangedBy` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO Report (reportUid, reportOwnerUid, xAxis, reportDateRangeSelection, fromDate, fromRelTo, fromRelOffSet, fromRelUnit, toDate, toRelTo, toRelOffSet, toRelUnit, reportTitle, reportDescription, reportSeries, reportInactive, isTemplate, priority, reportMasterChangeSeqNum, reportLocalChangeSeqNum, reportLastChangedBy) SELECT reportUid, reportOwnerUid, xAxis,0, fromDate, 0, 0, 0, 0, 0, 0, 0, reportTitle, reportDescription, reportSeries, reportInactive, isTemplate, 1, reportMasterChangeSeqNum, reportLocalChangeSeqNum, reportLastChangedBy FROM Report_OLD")
                database.execSQL("DROP TABLE Report_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS Report_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_Report_trk_clientId_epk_csn` ON Report_trk (`clientId`, `epk`, `csn`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_XLangMapEntry_verbLangMapUid` ON XLangMapEntry (`verbLangMapUid`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_StatementEntity_statementPersonUid` ON StatementEntity (`statementPersonUid`)")

                database.execSQL("ALTER TABLE StatementEntity ADD COLUMN contentEntryRoot INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("""UPDATE StatementEntity SET contentEntryRoot = 1 WHERE 
                    statementUid IN (select statementUid from StatementEntity LEFT JOIN 
                    ContentEntry ON ContentEntry.contentEntryUid = StatementEntity.statementContentEntryUid 
                    LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid 
                    WHERE XObjectEntity.objectId = ContentEntry.entryId)""".trimMargin())

                database.execSQL("""ALTER TABLE VerbEntity ADD COLUMN verbInActive INTEGER DEFAULT 0 NOT NULL""")
                database.execSQL("""UPDATE VerbEntity SET verbInActive = 1 WHERE 
                    urlId = '${VerbEntity.VERB_PASSED_URL}' AND verbUid != ${VerbEntity.VERB_PASSED_UID}""".trimMargin())
                database.execSQL("""UPDATE VerbEntity SET verbInActive = 1 WHERE 
                    urlId = '${VerbEntity.VERB_FAILED_URL}' AND verbUid != ${VerbEntity.VERB_FAILED_UID}""".trimMargin())

            }
        }


        val MIGRATION_58_59 = DoorMigrationSync(58, 59) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("""ALTER TABLE ClazzLogAttendanceRecord 
                    ADD COLUMN clazzLogAttendanceRecordPersonUid INTEGER DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzLogAttendanceRecord 
                    RENAME to ClazzLogAttendanceRecord_OLD""".trimMargin())
                database.execSQL("""CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord 
                    (  clazzLogAttendanceRecordClazzLogUid  INTEGER  NOT NULL , 
                    clazzLogAttendanceRecordPersonUid  INTEGER  NOT NULL , 
                    attendanceStatus  INTEGER  NOT NULL , 
                    clazzLogAttendanceRecordMasterChangeSeqNum  INTEGER  NOT NULL , 
                    clazzLogAttendanceRecordLocalChangeSeqNum  INTEGER  NOT NULL , 
                    clazzLogAttendanceRecordLastChangedBy  INTEGER  NOT NULL , 
                    clazzLogAttendanceRecordUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""".trimMargin())
                database.execSQL("""INSERT INTO ClazzLogAttendanceRecord 
                    (clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, 
                    clazzLogAttendanceRecordPersonUid, attendanceStatus, 
                    clazzLogAttendanceRecordMasterChangeSeqNum, 
                    clazzLogAttendanceRecordLocalChangeSeqNum, 
                    clazzLogAttendanceRecordLastChangedBy) SELECT 
                    clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, 
                    clazzLogAttendanceRecordPersonUid, attendanceStatus, 
                    clazzLogAttendanceRecordMasterChangeSeqNum, 
                    clazzLogAttendanceRecordLocalChangeSeqNum, 
                    clazzLogAttendanceRecordLastChangedBy FROM ClazzLogAttendanceRecord_OLD""".trimMargin())
                database.execSQL("DROP TABLE ClazzLogAttendanceRecord_OLD")
                database.execSQL("""
                      |CREATE TRIGGER INS_LOC_15
                      |AFTER INSERT ON ClazzLogAttendanceRecord
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                      |    NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0)
                      |BEGIN
                      |    UPDATE ClazzLogAttendanceRecord
                      |    SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
                      |    WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums
                      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |    WHERE sCsnTableId = 15;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER INS_PRI_15
                      |            AFTER INSERT ON ClazzLogAttendanceRecord
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                      |                NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0)
                      |            BEGIN
                      |                UPDATE ClazzLogAttendanceRecord
                      |                SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
                      |                WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 15;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 15, NEW.clazzLogAttendanceRecordUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER UPD_LOC_15
                      |AFTER UPDATE ON ClazzLogAttendanceRecord
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                      |    AND (NEW.clazzLogAttendanceRecordLocalChangeSeqNum == OLD.clazzLogAttendanceRecordLocalChangeSeqNum OR
                      |        NEW.clazzLogAttendanceRecordLocalChangeSeqNum == 0))
                      |BEGIN
                      |    UPDATE ClazzLogAttendanceRecord
                      |    SET clazzLogAttendanceRecordLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 15) 
                      |    WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums 
                      |    SET sCsnNextLocal = sCsnNextLocal + 1
                      |    WHERE sCsnTableId = 15;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER UPD_PRI_15
                      |            AFTER UPDATE ON ClazzLogAttendanceRecord
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                      |                AND (NEW.clazzLogAttendanceRecordMasterChangeSeqNum == OLD.clazzLogAttendanceRecordMasterChangeSeqNum OR
                      |                    NEW.clazzLogAttendanceRecordMasterChangeSeqNum == 0))
                      |            BEGIN
                      |                UPDATE ClazzLogAttendanceRecord
                      |                SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
                      |                WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 15;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 15, NEW.clazzLogAttendanceRecordUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord_trk (  epk  INTEGER , clientId  INTEGER , csn  INTEGER , rx  INTEGER , reqId  INTEGER , ts  INTEGER , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")


                database.execSQL("ALTER TABLE ClazzWorkSubmission RENAME to ClazzWorkSubmission_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission (  clazzWorkSubmissionClazzWorkUid  INTEGER  NOT NULL , clazzWorkSubmissionMarkerPersonUid  INTEGER  NOT NULL , clazzWorkSubmissionPersonUid  INTEGER  NOT NULL , clazzWorkSubmissionInactive  INTEGER  NOT NULL , clazzWorkSubmissionDateTimeStarted  INTEGER  NOT NULL , clazzWorkSubmissionDateTimeUpdated  INTEGER  NOT NULL , clazzWorkSubmissionDateTimeFinished  INTEGER  NOT NULL , clazzWorkSubmissionDateTimeMarked  INTEGER  NOT NULL , clazzWorkSubmissionText  TEXT , clazzWorkSubmissionScore  INTEGER  NOT NULL , clazzWorkSubmissionMCSN  INTEGER  NOT NULL , clazzWorkSubmissionLCSN  INTEGER  NOT NULL , clazzWorkSubmissionLCB  INTEGER  NOT NULL , clazzWorkSubmissionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("INSERT INTO ClazzWorkSubmission (clazzWorkSubmissionUid, clazzWorkSubmissionClazzWorkUid, clazzWorkSubmissionMarkerPersonUid, clazzWorkSubmissionPersonUid, clazzWorkSubmissionInactive, clazzWorkSubmissionDateTimeStarted, clazzWorkSubmissionDateTimeUpdated, clazzWorkSubmissionDateTimeFinished, clazzWorkSubmissionDateTimeMarked, clazzWorkSubmissionText, clazzWorkSubmissionScore, clazzWorkSubmissionMCSN, clazzWorkSubmissionLCSN, clazzWorkSubmissionLCB) SELECT clazzWorkSubmissionUid, clazzWorkSubmissionClazzWorkUid, clazzWorkSubmissionMarkerPersonUid, clazzWorkSubmissionPersonUid, clazzWorkSubmissionInactive, clazzWorkSubmissionDateTimeStarted, clazzWorkSubmissionDateTimeUpdated, clazzWorkSubmissionDateTimeFinished, clazzWorkSubmissionDateTimeMarked, clazzWorkSubmissionText, clazzWorkSubmissionScore, clazzWorkSubmissionMCSN, clazzWorkSubmissionLCSN, clazzWorkSubmissionLCB FROM ClazzWorkSubmission_OLD")
                database.execSQL("DROP TABLE ClazzWorkSubmission_OLD")
                database.execSQL("""
                      |CREATE TRIGGER INS_LOC_206
                      |AFTER INSERT ON ClazzWorkSubmission
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                      |    NEW.clazzWorkSubmissionLCSN = 0)
                      |BEGIN
                      |    UPDATE ClazzWorkSubmission
                      |    SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
                      |    WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums
                      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |    WHERE sCsnTableId = 206;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER INS_PRI_206
                      |            AFTER INSERT ON ClazzWorkSubmission
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                      |                NEW.clazzWorkSubmissionMCSN = 0)
                      |            BEGIN
                      |                UPDATE ClazzWorkSubmission
                      |                SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
                      |                WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 206;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 206, NEW.clazzWorkSubmissionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER UPD_LOC_206
                      |AFTER UPDATE ON ClazzWorkSubmission
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                      |    AND (NEW.clazzWorkSubmissionLCSN == OLD.clazzWorkSubmissionLCSN OR
                      |        NEW.clazzWorkSubmissionLCSN == 0))
                      |BEGIN
                      |    UPDATE ClazzWorkSubmission
                      |    SET clazzWorkSubmissionLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 206) 
                      |    WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums 
                      |    SET sCsnNextLocal = sCsnNextLocal + 1
                      |    WHERE sCsnTableId = 206;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER UPD_PRI_206
                      |            AFTER UPDATE ON ClazzWorkSubmission
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                      |                AND (NEW.clazzWorkSubmissionMCSN == OLD.clazzWorkSubmissionMCSN OR
                      |                    NEW.clazzWorkSubmissionMCSN == 0))
                      |            BEGIN
                      |                UPDATE ClazzWorkSubmission
                      |                SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
                      |                WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 206;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 206, NEW.clazzWorkSubmissionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkSubmission_trk (  epk  INTEGER , clientId  INTEGER , csn  INTEGER , rx  INTEGER , reqId  INTEGER , ts  INTEGER , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")


                database.execSQL("ALTER TABLE ClazzWorkQuestionResponse RENAME to ClazzWorkQuestionResponse_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse (  clazzWorkQuestionResponseClazzWorkUid  INTEGER  NOT NULL , clazzWorkQuestionResponseQuestionUid  INTEGER  NOT NULL , clazzWorkQuestionResponseText  TEXT , clazzWorkQuestionResponseOptionSelected  INTEGER  NOT NULL , clazzWorkQuestionResponsePersonUid  INTEGER  NOT NULL , clazzWorkQuestionResponseInactive  INTEGER  NOT NULL , clazzWorkQuestionResponseDateResponded  INTEGER  NOT NULL , clazzWorkQuestionResponseMCSN  INTEGER  NOT NULL , clazzWorkQuestionResponseLCSN  INTEGER  NOT NULL , clazzWorkQuestionResponseLCB  INTEGER  NOT NULL , clazzWorkQuestionResponseUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("INSERT INTO ClazzWorkQuestionResponse (clazzWorkQuestionResponseUid, clazzWorkQuestionResponseClazzWorkUid, clazzWorkQuestionResponseQuestionUid, clazzWorkQuestionResponseText, clazzWorkQuestionResponseOptionSelected, clazzWorkQuestionResponsePersonUid, clazzWorkQuestionResponseInactive, clazzWorkQuestionResponseDateResponded, clazzWorkQuestionResponseMCSN, clazzWorkQuestionResponseLCSN, clazzWorkQuestionResponseLCB) SELECT clazzWorkQuestionResponseUid, clazzWorkQuestionResponseClazzWorkUid, clazzWorkQuestionResponseQuestionUid, clazzWorkQuestionResponseText, clazzWorkQuestionResponseOptionSelected, clazzWorkQuestionResponsePersonUid, clazzWorkQuestionResponseInactive, clazzWorkQuestionResponseDateResponded, clazzWorkQuestionResponseMCSN, clazzWorkQuestionResponseLCSN, clazzWorkQuestionResponseLCB FROM ClazzWorkQuestionResponse_OLD")
                database.execSQL("DROP TABLE ClazzWorkQuestionResponse_OLD")
                database.execSQL("""
                      |CREATE TRIGGER INS_LOC_209
                      |AFTER INSERT ON ClazzWorkQuestionResponse
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                      |    NEW.clazzWorkQuestionResponseLCSN = 0)
                      |BEGIN
                      |    UPDATE ClazzWorkQuestionResponse
                      |    SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
                      |    WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums
                      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |    WHERE sCsnTableId = 209;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER INS_PRI_209
                      |            AFTER INSERT ON ClazzWorkQuestionResponse
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                      |                NEW.clazzWorkQuestionResponseMCSN = 0)
                      |            BEGIN
                      |                UPDATE ClazzWorkQuestionResponse
                      |                SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
                      |                WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 209;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 209, NEW.clazzWorkQuestionResponseUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER UPD_LOC_209
                      |AFTER UPDATE ON ClazzWorkQuestionResponse
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                      |    AND (NEW.clazzWorkQuestionResponseLCSN == OLD.clazzWorkQuestionResponseLCSN OR
                      |        NEW.clazzWorkQuestionResponseLCSN == 0))
                      |BEGIN
                      |    UPDATE ClazzWorkQuestionResponse
                      |    SET clazzWorkQuestionResponseLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 209) 
                      |    WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums 
                      |    SET sCsnNextLocal = sCsnNextLocal + 1
                      |    WHERE sCsnTableId = 209;
                      |END
                  """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER UPD_PRI_209
                      |            AFTER UPDATE ON ClazzWorkQuestionResponse
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                      |                AND (NEW.clazzWorkQuestionResponseMCSN == OLD.clazzWorkQuestionResponseMCSN OR
                      |                    NEW.clazzWorkQuestionResponseMCSN == 0))
                      |            BEGIN
                      |                UPDATE ClazzWorkQuestionResponse
                      |                SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
                      |                WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 209;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 209, NEW.clazzWorkQuestionResponseUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzWorkQuestionResponse_trk (  epk  INTEGER , clientId  INTEGER , csn  INTEGER , rx  INTEGER , reqId  INTEGER , ts  INTEGER , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")


                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzEnrolment (`clazzEnrolmentUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clazzEnrolmentPersonUid` INTEGER NOT NULL, `clazzEnrolmentClazzUid` INTEGER NOT NULL, `clazzEnrolmentDateJoined` INTEGER NOT NULL, `clazzEnrolmentDateLeft` INTEGER NOT NULL, `clazzEnrolmentRole` INTEGER NOT NULL, `clazzEnrolmentAttendancePercentage` REAL NOT NULL, `clazzEnrolmentActive` INTEGER NOT NULL, `clazzEnrolmentLocalChangeSeqNum` INTEGER NOT NULL, `clazzEnrolmentMasterChangeSeqNum` INTEGER NOT NULL, `clazzEnrolmentLastChangedBy` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentPersonUid_clazzEnrolmentClazzUid ON ClazzEnrolment (clazzEnrolmentPersonUid, clazzEnrolmentClazzUid)")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentClazzUid_clazzEnrolmentPersonUid ON ClazzEnrolment (clazzEnrolmentClazzUid, clazzEnrolmentPersonUid)")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentClazzUid_clazzEnrolmentRole ON ClazzEnrolment (clazzEnrolmentClazzUid, clazzEnrolmentRole)")
                database.execSQL("INSERT INTO ClazzEnrolment (clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy) SELECT clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, clazzMemberDateJoined, clazzMemberDateLeft, clazzMemberRole, clazzMemberAttendancePercentage, clazzMemberActive,clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy FROM ClazzMember")
                database.execSQL("DROP TABLE ClazzMember")
                database.execSQL("DROP TABLE IF EXISTS ClazzMember_trk")
                database.execSQL("""
                      |CREATE TRIGGER INS_LOC_65
                      |AFTER INSERT ON ClazzEnrolment
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                      |    NEW.clazzEnrolmentLocalChangeSeqNum = 0)
                      |BEGIN
                      |    UPDATE ClazzEnrolment
                      |    SET clazzEnrolmentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
                      |    WHERE clazzEnrolmentUid = NEW.clazzEnrolmentUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums
                      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |    WHERE sCsnTableId = 65;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER INS_PRI_65
                      |            AFTER INSERT ON ClazzEnrolment
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                      |                NEW.clazzEnrolmentMasterChangeSeqNum = 0)
                      |            BEGIN
                      |                UPDATE ClazzEnrolment
                      |                SET clazzEnrolmentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
                      |                WHERE clazzEnrolmentUid = NEW.clazzEnrolmentUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 65;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 65, NEW.clazzEnrolmentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER UPD_LOC_65
                      |AFTER UPDATE ON ClazzEnrolment
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                      |    AND (NEW.clazzEnrolmentLocalChangeSeqNum == OLD.clazzEnrolmentLocalChangeSeqNum OR
                      |        NEW.clazzEnrolmentLocalChangeSeqNum == 0))
                      |BEGIN
                      |    UPDATE ClazzEnrolment
                      |    SET clazzEnrolmentLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 65) 
                      |    WHERE clazzEnrolmentUid = NEW.clazzEnrolmentUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums 
                      |    SET sCsnNextLocal = sCsnNextLocal + 1
                      |    WHERE sCsnTableId = 65;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER UPD_PRI_65
                      |            AFTER UPDATE ON ClazzEnrolment
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                      |                AND (NEW.clazzEnrolmentMasterChangeSeqNum == OLD.clazzEnrolmentMasterChangeSeqNum OR
                      |                    NEW.clazzEnrolmentMasterChangeSeqNum == 0))
                      |            BEGIN
                      |                UPDATE ClazzEnrolment
                      |                SET clazzEnrolmentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
                      |                WHERE clazzEnrolmentUid = NEW.clazzEnrolmentUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 65;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 65, NEW.clazzEnrolmentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzEnrolment_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                database.execSQL("""
                      |CREATE 
                      | INDEX index_ClazzEnrolment_trk_clientId_epk_csn 
                      |ON ClazzEnrolment_trk (clientId, epk, csn)
                      """.trimMargin())
                database.execSQL("""
                      |CREATE 
                      |UNIQUE INDEX index_ClazzEnrolment_trk_epk_clientId 
                      |ON ClazzEnrolment_trk (epk, clientId)
                      """.trimMargin())
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_ClazzEnrolment_clazzEnrolmentPersonUid` ON ClazzEnrolment (`clazzEnrolmentPersonUid`)""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_ClazzEnrolment_clazzEnrolmentClazzUid` ON ClazzEnrolment (`clazzEnrolmentClazzUid`)""")


            } else if (database.dbType() == DoorDbType.POSTGRES) {

                database.execSQL("""ALTER TABLE ClazzLogAttendanceRecord 
                    ADD COLUMN clazzLogAttendanceRecordPersonUid BIGINT DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("""UPDATE ClazzLogAttendanceRecord SET 
                    clazzLogAttendanceRecordPersonUid = (SELECT clazzMemberPersonUid 
                    FROM ClazzMember LEFT JOIN ClazzLogAttendanceRecord ON 
                    ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = 
                    ClazzMember.clazzMemberUid WHERE ClazzMember.clazzMemberUid = 
                    ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid LIMIT 1)""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzLogAttendanceRecord 
                    DROP COLUMN IF EXISTS clazzLogAttendanceRecordClazzMemberUid""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzWorkSubmission 
                    DROP COLUMN IF EXISTS clazzWorkSubmissionClazzMemberUid""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzWorkSubmission 
                    DROP COLUMN IF EXISTS clazzWorkSubmissionMarkerClazzMemberUid""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzWorkQuestionResponse 
                    DROP COLUMN IF EXISTS clazzWorkQuestionResponseClazzMemberUid""".trimMargin())


                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzEnrolment (  clazzEnrolmentPersonUid  BIGINT  NOT NULL , clazzEnrolmentClazzUid  BIGINT  NOT NULL , clazzEnrolmentDateJoined  BIGINT  NOT NULL , clazzEnrolmentDateLeft  BIGINT  NOT NULL , clazzEnrolmentRole  INTEGER  NOT NULL , clazzEnrolmentAttendancePercentage  FLOAT  NOT NULL , clazzEnrolmentActive  BOOL  NOT NULL , clazzEnrolmentLocalChangeSeqNum  BIGINT  NOT NULL , clazzEnrolmentMasterChangeSeqNum  BIGINT  NOT NULL , clazzEnrolmentLastChangedBy  INTEGER  NOT NULL , clazzEnrolmentUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentPersonUid_clazzEnrolmentClazzUid ON ClazzEnrolment (clazzEnrolmentPersonUid, clazzEnrolmentClazzUid)")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentClazzUid_clazzEnrolmentPersonUid ON ClazzEnrolment (clazzEnrolmentClazzUid, clazzEnrolmentPersonUid)")
                database.execSQL("CREATE INDEX index_ClazzEnrolment_clazzEnrolmentClazzUid_clazzEnrolmentRole ON ClazzEnrolment (clazzEnrolmentClazzUid, clazzEnrolmentRole)")
                database.execSQL("INSERT INTO ClazzEnrolment (clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy) SELECT clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, clazzMemberDateJoined, clazzMemberDateLeft, clazzMemberRole, clazzMemberAttendancePercentage, clazzMemberActive, clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy FROM ClazzMember")
                database.execSQL("DROP TABLE ClazzMember")
                database.execSQL("DROP TABLE IF EXISTS ClazzMember_trk")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzEnrolment_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzEnrolment_lcsn_seq")
                database.execSQL("""
                      |CREATE OR REPLACE FUNCTION 
                      | inccsn_65_fn() RETURNS trigger AS ${'$'}${'$'}
                      | BEGIN  
                      | UPDATE ClazzEnrolment SET clazzEnrolmentLocalChangeSeqNum =
                      | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzEnrolmentLocalChangeSeqNum 
                      | ELSE NEXTVAL('ClazzEnrolment_lcsn_seq') END),
                      | clazzEnrolmentMasterChangeSeqNum = 
                      | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                      | THEN NEXTVAL('ClazzEnrolment_mcsn_seq') 
                      | ELSE NEW.clazzEnrolmentMasterChangeSeqNum END)
                      | WHERE clazzEnrolmentUid = NEW.clazzEnrolmentUid;
                      | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      | SELECT 65, NEW.clazzEnrolmentUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
                      | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
                      | RETURN null;
                      | END ${'$'}${'$'}
                      | LANGUAGE plpgsql
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER inccsn_65_trig 
                      |AFTER UPDATE OR INSERT ON ClazzEnrolment 
                      |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                      |EXECUTE PROCEDURE inccsn_65_fn()
                      """.trimMargin())
                database.execSQL("DROP FUNCTION IF EXISTS inc_csn_65_fn")
                database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_65")
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzEnrolment_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                      |CREATE 
                      | INDEX index_ClazzEnrolment_trk_clientId_epk_csn 
                      |ON ClazzEnrolment_trk (clientId, epk, csn)
                      """.trimMargin())
                database.execSQL("""
                      |CREATE 
                      |UNIQUE INDEX index_ClazzEnrolment_trk_epk_clientId 
                      |ON ClazzEnrolment_trk (epk, clientId)
                      """.trimMargin())


            }


        }

        val MIGRATION_59_60 = DoorMigrationSync(59, 60) { database ->
            database.execSQL("""ALTER TABLE ClazzEnrolment 
                    ADD COLUMN clazzEnrolmentOutcome INTEGER DEFAULT 0 NOT NULL""".trimMargin())

            if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("""ALTER TABLE ClazzEnrolment 
                    ADD COLUMN clazzEnrolmentLeavingReasonUid INTEGER DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("""CREATE TABLE IF NOT EXISTS LeavingReason (`leavingReasonUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `leavingReasonTitle` TEXT, `leavingReasonMCSN` INTEGER NOT NULL, `leavingReasonCSN` INTEGER NOT NULL, `leavingReasonLCB` INTEGER NOT NULL)""")
                database.execSQL("""
                      |CREATE TRIGGER INS_LOC_410
                      |AFTER INSERT ON LeavingReason
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
                      |    NEW.leavingReasonCSN = 0)
                      |BEGIN
                      |    UPDATE LeavingReason
                      |    SET leavingReasonMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 410)
                      |    WHERE leavingReasonUid = NEW.leavingReasonUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums
                      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |    WHERE sCsnTableId = 410;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER INS_PRI_410
                      |            AFTER INSERT ON LeavingReason
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
                      |                NEW.leavingReasonMCSN = 0)
                      |            BEGIN
                      |                UPDATE LeavingReason
                      |                SET leavingReasonMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 410)
                      |                WHERE leavingReasonUid = NEW.leavingReasonUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 410;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 410, NEW.leavingReasonUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER UPD_LOC_410
                      |AFTER UPDATE ON LeavingReason
                      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
                      |    AND (NEW.leavingReasonCSN == OLD.leavingReasonCSN OR
                      |        NEW.leavingReasonCSN == 0))
                      |BEGIN
                      |    UPDATE LeavingReason
                      |    SET leavingReasonCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 410) 
                      |    WHERE leavingReasonUid = NEW.leavingReasonUid;
                      |    
                      |    UPDATE SqliteChangeSeqNums 
                      |    SET sCsnNextLocal = sCsnNextLocal + 1
                      |    WHERE sCsnTableId = 410;
                      |END
                      """.trimMargin())
                database.execSQL("""
                      |            CREATE TRIGGER UPD_PRI_410
                      |            AFTER UPDATE ON LeavingReason
                      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
                      |                AND (NEW.leavingReasonMCSN == OLD.leavingReasonMCSN OR
                      |                    NEW.leavingReasonMCSN == 0))
                      |            BEGIN
                      |                UPDATE LeavingReason
                      |                SET leavingReasonMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 410)
                      |                WHERE leavingReasonUid = NEW.leavingReasonUid;
                      |                
                      |                UPDATE SqliteChangeSeqNums
                      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
                      |                WHERE sCsnTableId = 410;
                      |                
                      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      |SELECT 410, NEW.leavingReasonUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
                      |            END
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS LeavingReason_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_LeavingReason_trk_clientId_epk_csn` ON LeavingReason_trk (`clientId`, `epk`, `csn`)")
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS 
                   `index_LeavingReason_trk_epk_clientId` ON 
                   LeavingReason_trk (`epk`, `clientId`)
                   """)


            } else if (database.dbType() == DoorDbType.POSTGRES) {

                database.execSQL("""UPDATE ClazzEnrolment SET 
                clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}""".trimMargin())

                database.execSQL("""ALTER TABLE ClazzEnrolment 
                    ADD COLUMN clazzEnrolmentLeavingReasonUid BIGINT DEFAULT 0 NOT NULL""".trimMargin())

                database.execSQL("CREATE TABLE IF NOT EXISTS LeavingReason (  leavingReasonTitle  TEXT , leavingReasonMCSN  BIGINT  NOT NULL , leavingReasonCSN  BIGINT  NOT NULL , leavingReasonLCB  INTEGER  NOT NULL , leavingReasonUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS LeavingReason_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS LeavingReason_lcsn_seq")
                database.execSQL("""
                      |CREATE OR REPLACE FUNCTION 
                      | inccsn_410_fn() RETURNS trigger AS ${'$'}${'$'}
                      | BEGIN  
                      | UPDATE LeavingReason SET leavingReasonCSN =
                      | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.leavingReasonCSN 
                      | ELSE NEXTVAL('LeavingReason_lcsn_seq') END),
                      | leavingReasonMCSN = 
                      | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                      | THEN NEXTVAL('LeavingReason_mcsn_seq') 
                      | ELSE NEW.leavingReasonMCSN END)
                      | WHERE leavingReasonUid = NEW.leavingReasonUid;
                      | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
                      | SELECT 410, NEW.leavingReasonUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
                      | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
                      | RETURN null;
                      | END ${'$'}${'$'}
                      | LANGUAGE plpgsql
                      """.trimMargin())
                database.execSQL("""
                      |CREATE TRIGGER inccsn_410_trig 
                      |AFTER UPDATE OR INSERT ON LeavingReason 
                      |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                      |EXECUTE PROCEDURE inccsn_410_fn()
                      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS LeavingReason_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
                      |CREATE 
                      | INDEX index_LeavingReason_trk_clientId_epk_csn 
                      |ON LeavingReason_trk (clientId, epk, csn)
                      """.trimMargin())
                database.execSQL("""
                      |CREATE 
                      |UNIQUE INDEX index_LeavingReason_trk_epk_clientId 
                      |ON LeavingReason_trk (epk, clientId)
                      """.trimMargin())

            }


        }

        val MIGRATION_60_61 = DoorMigrationSync(60, 61) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {

                database.execSQL("""UPDATE Role SET 
                rolePermissions = ${Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT} 
                WHERE roleUid = ${Role.ROLE_CLAZZ_TEACHER_UID} """.trimMargin())

                database.execSQL("""UPDATE persongroupmember SET groupmemberactive = true 
                    WHERE groupmemberactive is NULL""".trimMargin())

                database.execSQL("""INSERT INTO PersonGroup
                    (groupName, groupActive, personGroupFlag) SELECT
                    'Admin Group', true, ${PersonGroup.PERSONGROUP_FLAG_PERSONGROUP} 
                    WHERE EXISTS (SELECT * FROM Person WHERE firstNames = 'Admin' 
                    AND lastName = 'User' AND personGroupUid = 0)""".trimMargin())
                database.execSQL("""UPDATE PERSON SET personGroupUid = (SELECT groupUid
                    FROM PersonGroup WHERE groupName = 'Admin Group' LIMIT 1) WHERE
                    firstNames = 'Admin' AND lastName = 'User' AND personGroupUid = 0""".trimMargin())
                database.execSQL("""INSERT INTO PersonGroupMember 
                    (groupMemberActive, groupMemberPersonUid, groupMemberGroupUid) 
                     SELECT true,(SELECT Person.personUid FROM PERSON WHERE username = 'admin') 
                    , (SELECT groupUid FROM PersonGroup WHERE groupName = 'Admin Group' LIMIT 1) 
                    WHERE EXISTS (SELECT * FROM PersonGroup WHERE groupName = 'Admin Group'); """.trimMargin())

            }
        }

        val MIGRATION_61_62 = DoorMigrationSync(61, 62) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("""ALTER TABLE Language 
                    ADD COLUMN languageActive BOOL DEFAULT FALSE NOT NULL""")
                database.execSQL("""UPDATE Language SET languageActive = true""")
            }else {
                database.execSQL("""ALTER TABLE Language 
                    ADD COLUMN languageActive INTEGER DEFAULT 0 NOT NULL""")
            }

        }

        val MIGRATION_62_63 = DoorMigrationSync(62, 63) { database ->
            //Adds LastChangedTime field to all syncable entities so the field will be ready to use
            //for the new p2p enabled sync systme
            val fieldType = if(database.dbType() == DoorDbType.SQLITE) {
                "INTEGER"
            }else {
                "BIGINT"
            }

            val lastModTimeFields = listOf("ClazzLog" to "clazzLogLastChangedTime",
                "ClazzLogAttendanceRecord" to "clazzLogAttendanceRecordLastChangedTime",
                "Schedule" to "scheduleLastChangedTime",
                "DateRange" to "dateRangeLct",
                "HolidayCalendar" to "umCalendarLct",
                "Holiday" to "holLct",
                "CustomField" to "customFieldLct",
                "CustomFieldValue" to "customFieldLct",
                "Person" to "personLct",
                "Clazz" to "clazzLct",
                "ClazzEnrolment" to "clazzEnrolmentLct",
                "LeavingReason" to "leavingReasonLct",
                "PersonCustomFieldValue" to "personCustomFieldValueLct",
                "ContentEntry" to "contentEntryLct",
                "ContentEntryContentCategoryJoin" to "ceccjLct",
                "ContentCategorySchema" to "contentCategorySchemaLct",
                "ContentEntryParentChildJoin" to "cepcjLct",
                "ContentEntryRelatedEntryJoin" to "cerejLct",
                "ContentCategory" to "contentCategoryLct",
                "Language" to "langLct",
                "LanguageVariant" to "langVariantLct",
                "Role" to "roleLct",
                "EntityRole" to "erLct",
                "PersonGroup" to "groupLct",
                "PersonGroupMember" to "groupMemberLct",
                "PersonPicture" to "personPictureLct",
                "Container" to "cntLct",
                "VerbEntity" to "verbLct",
                "XObjectEntity" to "xObjectLct",
                "StatementEntity" to "statementLct",
                "ContextXObjectStatementJoin" to "contextXObjectLct",
                "AgentEntity" to "agentLct",
                "StateEntity" to "stateLct",
                "StateContentEntity" to "stateContentLct",
                "XLangMapEntry" to "statementLangMapLct",
                "School" to "schoolLct",
                "SchoolMember" to "schoolMemberLct",
                "ClazzWork" to "clazzWorkLct",
                "ClazzWorkContentJoin" to "clazzWorkContentJoinLct",
                "Comments" to "commentsLct",
                "ClazzWorkQuestion" to "clazzWorkQuestionLct",
                "ClazzWorkQuestionOption" to "clazzWorkQuestionOptionLct",
                "ClazzWorkSubmission" to "clazzWorkSubmissionLct",
                "ClazzWorkQuestionResponse" to "clazzWorkQuestionResponseLct",
                "ContentEntryProgress" to "contentEntryProgressLct",
                "Report" to "reportLct",
                "Site" to "siteLct",
                "LearnerGroup" to "learnerGroupLct",
                "LearnerGroupMember" to "learnerGroupMemberLct",
                "GroupLearningSession" to "groupLearningSessionLct",
                "SiteTerms" to "sTermsLct",
                "ScheduledCheck" to "scheduledCheckLct",
                "CustomFieldValueOption" to "customFieldValueLct",
                "AuditLog" to "auditLogLct")

            lastModTimeFields.forEach {
                database.execSQL("ALTER TABLE ${it.first} ADD COLUMN ${it.second} $fieldType NOT NULL DEFAULT 0")
            }

        }

        val MIGRATION_63_64 = DoorMigrationSync(63, 64) { database ->
            database.execSQL("ALTER TABLE Person ADD COLUMN personCountry TEXT")
        }

        val MIGRATION_78_79 = DoorMigrationSync(78, 79) { database ->
            database.execSQL("ALTER TABLE Report ADD COLUMN reportTitleId INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE Report ADD COLUMN reportDescId INTEGER NOT NULL DEFAULT 0")

            if(database.dbType() == DoorDbType.POSTGRES){
                //Report Data migration

                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.BLANK_REPORT} , reportDescId = ${Report.BLANK_REPORT_DESC} WHERE 
                        reportUid = ${Report.TEMPLATE_BLANK_REPORT_UID}
                """.trimIndent())
                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.CONTENT_USAGE_OVER_TIME} , reportDescId = ${Report.CONTENT_USAGE_OVER_TIME_DESC}  WHERE 
                        reportUid = ${Report.TEMPLATE_CONTENT_USAGE_OVER_TIME_UID}
                """.trimIndent())
                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.UNIQUE_CONTENT_USERS_OVER_TIME} , reportDescId = ${Report.UNIQUE_CONTENT_USERS_OVER_TIME_DESC} WHERE 
                        reportUid = ${Report.TEMPLATE_UNIQUE_CONTENT_USERS_UID}
                """.trimIndent())
                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.ATTENDANCE_OVER_TIME_BY_CLASS} , reportDescId = ${Report.ATTENDANCE_OVER_TIME_BY_CLASS_DESC} WHERE 
                        reportUid = ${Report.TEMPLATE_ATTENDANCE_OVER_TIME_BY_CLASS_UID}
                """.trimIndent())
                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.CONTENT_USAGE_BY_CLASS} , reportDescId = ${Report.CONTENT_USAGE_BY_CLASS_DESC} WHERE 
                        reportUid = ${Report.TEMPLATE_CONTENT_USAGE_BY_CLASS_UID}
                """.trimIndent())
                database.execSQL("""
                    UPDATE Report SET reportTitleId = ${Report.CONTENT_COMPLETION} , reportDescId = ${Report.CONTENT_COMPLETION_DESC} WHERE 
                        reportUid = ${Report.TEMPLATE_CONTENT_COMPLETION_UID}
                """.trimIndent())
            }

        }

        val MIGRATION_64_65 = DoorMigrationSync(64, 65) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {

                database.execSQL("ALTER TABLE StatementEntity ADD COLUMN statementClazzUid BIGINT DEFAULT 0 NOT NULL")


                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzContentJoin (  ccjContentEntryUid  BIGINT  NOT NULL , ccjClazzUid  BIGINT  NOT NULL , ccjActive  BOOL  NOT NULL , ccjLocalChangeSeqNum  BIGINT  NOT NULL , ccjMasterChangeSeqNum  BIGINT  NOT NULL , ccjLastChangedBy  INTEGER  NOT NULL , ccjLct  BIGINT  NOT NULL , ccjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE INDEX index_ClazzContentJoin_ccjContentEntryUid ON ClazzContentJoin (ccjContentEntryUid)")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzContentJoin_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzContentJoin_lcsn_seq")
                database.execSQL("""
      |CREATE OR REPLACE FUNCTION 
      | inccsn_134_fn() RETURNS trigger AS ${'$'}${'$'}
      | BEGIN  
      | UPDATE ClazzContentJoin SET ccjLocalChangeSeqNum =
      | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.ccjLocalChangeSeqNum 
      | ELSE NEXTVAL('ClazzContentJoin_lcsn_seq') END),
      | ccjMasterChangeSeqNum = 
      | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
      | THEN NEXTVAL('ClazzContentJoin_mcsn_seq') 
      | ELSE NEW.ccjMasterChangeSeqNum END)
      | WHERE ccjUid = NEW.ccjUid;
      | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
      | SELECT 134, NEW.ccjUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
      | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
      | RETURN null;
      | END ${'$'}${'$'}
      | LANGUAGE plpgsql
      """.trimMargin())
                database.execSQL("""
      |CREATE TRIGGER inccsn_134_trig 
      |AFTER UPDATE OR INSERT ON ClazzContentJoin 
      |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
      |EXECUTE PROCEDURE inccsn_134_fn()
      """.trimMargin())
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzContentJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("""
      |CREATE 
      | INDEX index_ClazzContentJoin_trk_clientId_epk_csn 
      |ON ClazzContentJoin_trk (clientId, epk, csn)
      """.trimMargin())
                database.execSQL("""
      |CREATE 
      |UNIQUE INDEX index_ClazzContentJoin_trk_epk_clientId 
      |ON ClazzContentJoin_trk (epk, clientId)
      """.trimMargin())


            }else{

                database.execSQL("ALTER TABLE StatementEntity ADD COLUMN statementClazzUid INTEGER DEFAULT 0 NOT NULL")


                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzContentJoin (`ccjUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ccjContentEntryUid` INTEGER NOT NULL, `ccjClazzUid` INTEGER NOT NULL, `ccjActive` INTEGER NOT NULL, `ccjLocalChangeSeqNum` INTEGER NOT NULL, `ccjMasterChangeSeqNum` INTEGER NOT NULL, `ccjLastChangedBy` INTEGER NOT NULL, `ccjLct` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ClazzContentJoin_ccjContentEntryUid` ON ClazzContentJoin (`ccjContentEntryUid`)")

                database.execSQL("""
      |CREATE TRIGGER INS_LOC_134
      |AFTER INSERT ON ClazzContentJoin
      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
      |    NEW.ccjLocalChangeSeqNum = 0)
      |BEGIN
      |    UPDATE ClazzContentJoin
      |    SET ccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 134)
      |    WHERE ccjUid = NEW.ccjUid;
      |    
      |    UPDATE SqliteChangeSeqNums
      |    SET sCsnNextPrimary = sCsnNextPrimary + 1
      |    WHERE sCsnTableId = 134;
      |END
      """.trimMargin())
                database.execSQL("""
      |            CREATE TRIGGER INS_PRI_134
      |            AFTER INSERT ON ClazzContentJoin
      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
      |                NEW.ccjMasterChangeSeqNum = 0)
      |            BEGIN
      |                UPDATE ClazzContentJoin
      |                SET ccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 134)
      |                WHERE ccjUid = NEW.ccjUid;
      |                
      |                UPDATE SqliteChangeSeqNums
      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
      |                WHERE sCsnTableId = 134;
      |                
      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
      |SELECT 134, NEW.ccjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
      |            END
      """.trimMargin())
                database.execSQL("""
      |CREATE TRIGGER UPD_LOC_134
      |AFTER UPDATE ON ClazzContentJoin
      |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
      |    AND (NEW.ccjLocalChangeSeqNum == OLD.ccjLocalChangeSeqNum OR
      |        NEW.ccjLocalChangeSeqNum == 0))
      |BEGIN
      |    UPDATE ClazzContentJoin
      |    SET ccjLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 134) 
      |    WHERE ccjUid = NEW.ccjUid;
      |    
      |    UPDATE SqliteChangeSeqNums 
      |    SET sCsnNextLocal = sCsnNextLocal + 1
      |    WHERE sCsnTableId = 134;
      |END
      """.trimMargin())
                database.execSQL("""
      |            CREATE TRIGGER UPD_PRI_134
      |            AFTER UPDATE ON ClazzContentJoin
      |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
      |                AND (NEW.ccjMasterChangeSeqNum == OLD.ccjMasterChangeSeqNum OR
      |                    NEW.ccjMasterChangeSeqNum == 0))
      |            BEGIN
      |                UPDATE ClazzContentJoin
      |                SET ccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 134)
      |                WHERE ccjUid = NEW.ccjUid;
      |                
      |                UPDATE SqliteChangeSeqNums
      |                SET sCsnNextPrimary = sCsnNextPrimary + 1
      |                WHERE sCsnTableId = 134;
      |                
      |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
      |SELECT 134, NEW.ccjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
      |            END
      """.trimMargin())


                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzContentJoin_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ClazzContentJoin_trk_clientId_epk_csn` ON ClazzContentJoin_trk (`clientId`, `epk`, `csn`)")

                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ClazzContentJoin_trk_epk_clientId` ON ClazzContentJoin_trk (`epk`, `clientId`)")


            }


        }

        val MIGRATION_65_66 = DoorMigrationSync(65, 66) { database ->
             if(database.dbType() == DoorDbType.SQLITE){
                    //Add table ScopedGrant
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScopedGrant (  sgPcsn  INTEGER  NOT NULL , sgLcsn  INTEGER  NOT NULL , sgLcb  INTEGER  NOT NULL , sgLct  INTEGER  NOT NULL , sgTableId  INTEGER  NOT NULL , sgEntityUid  INTEGER  NOT NULL , sgPermissions  INTEGER  NOT NULL , sgGroupUid  INTEGER  NOT NULL , sgIndex  INTEGER  NOT NULL , sgFlags  INTEGER  NOT NULL , sgUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("ScopedGrant", 48, "sgUid",
                        "sgLcsn", "sgPcsn").forEach {
                        database.execSQL(it)
                    }
                    DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("ScopedGrant", 48, "sgUid",
                        "sgLcsn", "sgPcsn").forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScopedGrant_trk (  epk  INTEGER  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  INTEGER  NOT NULL DEFAULT 0 , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  INTEGER  NOT NULL DEFAULT 0 , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_ScopedGrant_trk_clientId_epk_csn ON ScopedGrant_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_ScopedGrant_trk_epk_clientId ON ScopedGrant_trk (epk, clientId)")

                    //PersonParentJoin
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonParentJoin (  ppjPcsn  INTEGER  NOT NULL , ppjLcsn  INTEGER  NOT NULL , ppjLcb  INTEGER  NOT NULL , ppjLct  INTEGER  NOT NULL , ppjParentPersonUid  INTEGER  NOT NULL , ppjMinorPersonUid  INTEGER  NOT NULL , ppjRelationship  INTEGER  NOT NULL , ppjEmail  TEXT , ppjPhone  TEXT , ppjInactive  INTEGER  NOT NULL , ppjStatus  INTEGER  NOT NULL , ppjApprovalTiemstamp  INTEGER  NOT NULL , ppjApprovalIpAddr  TEXT , ppjUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("PersonParentJoin", 512,
                        "ppjUid", "ppjLcsn", "ppjPcsn").forEach {
                        database.execSQL(it)
                    }
                    DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("PersonParentJoin", 512,
                        "ppjUid", "ppjLcsn", "ppjPcsn").forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonParentJoin_trk (  epk  INTEGER  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  INTEGER  NOT NULL DEFAULT 0 , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  INTEGER  NOT NULL DEFAULT 0 , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_PersonParentJoin_trk_clientId_epk_csn ON PersonParentJoin_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_PersonParentJoin_trk_epk_clientId ON PersonParentJoin_trk (epk, clientId)")


                    //Begin: Create table ErrorReport for SQLite
                    database.execSQL("CREATE TABLE IF NOT EXISTS ErrorReport (  errPcsn  INTEGER  NOT NULL , errLcsn  INTEGER  NOT NULL , errLcb  INTEGER  NOT NULL , errLct  INTEGER  NOT NULL , severity  INTEGER  NOT NULL , timestamp  INTEGER  NOT NULL , presenterUri  TEXT , appVersion  TEXT , versionCode  INTEGER  NOT NULL , errorCode  INTEGER  NOT NULL , operatingSys  TEXT , osVersion  TEXT , stackTrace  TEXT , message  TEXT , errUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("ErrorReport", 419, "errUid",
                        "errLcsn", "errPcsn").forEach {
                        database.execSQL(it)
                    }
                    DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("ErrorReport", 419, "errUid",
                        "errLcsn", "errPcsn").forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS ErrorReport_trk (  epk  INTEGER  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  INTEGER  NOT NULL DEFAULT 0 , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  INTEGER  NOT NULL DEFAULT 0 , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_ErrorReport_trk_clientId_epk_csn ON ErrorReport_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_ErrorReport_trk_epk_clientId ON ErrorReport_trk (epk, clientId)")
                }else {
                    //ScopedGrant
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScopedGrant (  sgPcsn  BIGINT  NOT NULL , sgLcsn  BIGINT  NOT NULL , sgLcb  INTEGER  NOT NULL , sgLct  BIGINT  NOT NULL , sgTableId  INTEGER  NOT NULL , sgEntityUid  BIGINT  NOT NULL , sgPermissions  BIGINT  NOT NULL , sgGroupUid  BIGINT  NOT NULL , sgIndex  INTEGER  NOT NULL , sgFlags  INTEGER  NOT NULL , sgUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ScopedGrant_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ScopedGrant_lcsn_seq")
                    DoorSqlGenerator.generateSyncableEntityFunctionAndTriggerPostgres(entityName =
                        "ScopedGrant", tableId = 48, pkFieldName = "sgUid", localCsnFieldName = "sgLcsn",
                        primaryCsnFieldName = "sgPcsn").forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScopedGrant_trk (  epk  BIGINT  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  BOOL  NOT NULL DEFAULT false , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  BIGINT  NOT NULL DEFAULT 0 , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_ScopedGrant_trk_clientId_epk_csn ON ScopedGrant_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_ScopedGrant_trk_epk_clientId ON ScopedGrant_trk (epk, clientId)")

                    //PersonParentJoin
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonParentJoin (  ppjPcsn  BIGINT  NOT NULL , ppjLcsn  BIGINT  NOT NULL , ppjLcb  INTEGER  NOT NULL , ppjLct  BIGINT  NOT NULL , ppjParentPersonUid  BIGINT  NOT NULL , ppjMinorPersonUid  BIGINT  NOT NULL , ppjRelationship  INTEGER  NOT NULL , ppjEmail  TEXT , ppjPhone  TEXT , ppjInactive  BOOL  NOT NULL , ppjStatus  INTEGER  NOT NULL , ppjApprovalTiemstamp  BIGINT  NOT NULL , ppjApprovalIpAddr  TEXT , ppjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonParentJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonParentJoin_lcsn_seq")
                    DoorSqlGenerator.generateSyncableEntityFunctionAndTriggerPostgres(entityName =
                        "PersonParentJoin", tableId = 512, pkFieldName = "ppjUid", localCsnFieldName =
                        "ppjLcsn", primaryCsnFieldName = "ppjPcsn"
                    ).forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonParentJoin_trk (  epk  BIGINT  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  BOOL  NOT NULL DEFAULT false , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  BIGINT  NOT NULL DEFAULT 0 , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_PersonParentJoin_trk_clientId_epk_csn ON PersonParentJoin_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_PersonParentJoin_trk_epk_clientId ON PersonParentJoin_trk (epk, clientId)")

                    //ErrorReport
                    database.execSQL("CREATE TABLE IF NOT EXISTS ErrorReport (  errPcsn  BIGINT  NOT NULL , errLcsn  BIGINT  NOT NULL , errLcb  INTEGER  NOT NULL , errLct  BIGINT  NOT NULL , severity  INTEGER  NOT NULL , timestamp  BIGINT  NOT NULL , presenterUri  TEXT , appVersion  TEXT , versionCode  INTEGER  NOT NULL , errorCode  INTEGER  NOT NULL , operatingSys  TEXT , osVersion  TEXT , stackTrace  TEXT , message  TEXT , errUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ErrorReport_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ErrorReport_lcsn_seq")
                    DoorSqlGenerator.generateSyncableEntityFunctionAndTriggerPostgres(entityName =
                        "ErrorReport", tableId = 419, pkFieldName = "errUid", localCsnFieldName = "errLcsn",
                        primaryCsnFieldName = "errPcsn"
                    ).forEach {
                        database.execSQL(it)
                    }
                    database.execSQL("CREATE TABLE IF NOT EXISTS ErrorReport_trk (  epk  BIGINT  NOT NULL DEFAULT 0 , clientId  INTEGER  NOT NULL DEFAULT 0 , csn  INTEGER  NOT NULL DEFAULT 0 , rx  BOOL  NOT NULL DEFAULT false , reqId  INTEGER  NOT NULL DEFAULT 0 , ts  BIGINT  NOT NULL DEFAULT 0 , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE  INDEX index_ErrorReport_trk_clientId_epk_csn ON ErrorReport_trk (clientId, epk, csn)")
                    database.execSQL("CREATE UNIQUE INDEX index_ErrorReport_trk_epk_clientId ON ErrorReport_trk (epk, clientId)")

                    database.execSQL("""
                        UPDATE Role
                           SET rolePermissions = (rolePermissions | ${Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT})
                         WHERE roleUid = ${Role.ROLE_CLAZZ_TEACHER_UID}   
                    """.trimIndent())

                    database.execSQL("""
                        UPDATE Role
                           SET rolePermissions = (rolePermissions | ${Role.ROLE_SCHOOL_STAFF_PERMISSIONS_DEFAULT})
                         WHERE roleUid = ${Role.ROLE_SCHOOL_STAFF_UID}  
                    """.trimIndent())

                    //For each preexisting role-entityrole assignment, make a ScopedGrant to give
                    // the same permissions
                    val updateTime = systemTimeInMillis()
                    database.execSQL("""
                        INSERT INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, 
                                    sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags)
                             SELECT EntityRole.erUid AS sgUid, 0 AS sgPcsn, 0 AS sgLcsn, 0 AS sgLcb, 
                                    $updateTime AS sgLct, EntityRole.erTableId AS sgTableId, 
                                    EntityRole.erEntityUid AS sgEntityUid,
                                    Role.rolePermissions AS sgPermissions, 
                                    EntityRole.erGroupUid AS sgGroupUid, 0 AS sgIndex, 
                                    CASE 
                                         WHEN Role.roleUid = ${Role.ROLE_CLAZZ_TEACHER_UID} 
                                              THEN ${FLAG_TEACHER_GROUP.or(FLAG_NO_DELETE)}
                                         WHEN Role.roleUid = ${Role.ROLE_SCHOOL_STAFF_UID} 
                                              THEN ${FLAG_TEACHER_GROUP.or(FLAG_NO_DELETE)}
                                         WHEN Role.roleUid = ${Role.ROLE_CLAZZ_STUDENT_UID} 
                                              THEN ${FLAG_STUDENT_GROUP.or(FLAG_NO_DELETE)}
                                         WHEN Role.roleUid = ${Role.ROLE_SCHOOL_STUDENT_UID} 
                                              THEN ${FLAG_STUDENT_GROUP.or(FLAG_NO_DELETE)}
                                         ELSE 0
                                    END AS sgFlags
                               FROM EntityRole
                                    JOIN Role ON EntityRole.erRoleUid = Role.roleUid     
                    """.trimIndent())

                    database.execSQL("""
                        INSERT INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, 
                                    sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags)
                             SELECT Person.personUid AS sgUid, 0 AS sgPcsn, 0 AS sgLcsn, 0 AS sgLcb, 
                                    $updateTime AS sgLct, 
                                    ${ScopedGrant.ALL_TABLES} as sgTableId,
                                    ${ScopedGrant.ALL_ENTITIES} AS sgEntityUid,
                                    ${Role.ALL_PERMISSIONS} AS sgPermissions,
                                    Person.personGroupUid AS sgGroupUid,
                                    0 AS sgFlags,
                                    0 AS sgIndex
                               FROM Person
                              WHERE CAST(Person.admin AS INTEGER) = 1      
                                          
                    """.trimIndent())
                }

        }

        val MIGRATION_66_67 =  DoorMigrationSync(66, 67) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("ALTER TABLE ContainerImportJob RENAME to ContainerImportJob_OLD")
                database.execSQL("CREATE TABLE IF NOT EXISTS ContainerImportJob (  cijContainerUid  INTEGER  NOT NULL , cijUri  TEXT , cijImportMode  INTEGER  NOT NULL , cijContainerBaseDir  TEXT , cijContentEntryUid  INTEGER  NOT NULL , cijMimeType  TEXT , cijSessionId  TEXT , cijJobStatus  INTEGER  NOT NULL , cijBytesSoFar  INTEGER  NOT NULL , cijImportCompleted  INTEGER  NOT NULL , cijContentLength  INTEGER  NOT NULL , cijContainerEntryFileUids  TEXT , cijConversionParams  TEXT , cijUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("INSERT INTO ContainerImportJob (cijUid, cijContainerUid, cijUri, cijImportMode, cijContainerBaseDir, cijContentEntryUid, cijMimeType, cijSessionId, cijJobStatus, cijBytesSoFar, cijImportCompleted, cijContentLength, cijContainerEntryFileUids, cijConversionParams) SELECT cijUid, cijContainerUid, cijFilePath, 0, cijContainerBaseDir, cijContentEntryUid, cijMimeType, cijSessionId, cijJobStatus, cijBytesSoFar, cijImportCompleted, cijContentLength, cijContainerEntryFileUids, cijConversionParams FROM ContainerImportJob_OLD")
                database.execSQL("DROP TABLE ContainerImportJob_OLD")


            } else {

                database.execSQL("""ALTER TABLE ContainerImportJob RENAME COLUMN cijFilePath to cijUri""".trimMargin())
                database.execSQL("""ALTER TABLE ContainerImportJob ADD COLUMN cijImportMode INTEGER DEFAULT 0 NOT NULL""")

            }
        }

        //Note 67-68 requires the predetermined nodeId, so it is not here as a constant.



        val MIGRATION_68_69 = DoorMigrationSync(68, 69) { database ->
            if (database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("""ALTER TABLE ContentEntry ADD COLUMN contentOwner BIGINT DEFAULT 0 NOT NULL""")
                database.execSQL("""UPDATE ContentEntry 
                                       SET contentOwner = (SELECT personUid 
                                                             FROM Person 
                                                            WHERE admin LIMIT 1)""")
            }else{
                database.execSQL("""ALTER TABLE ContentEntry ADD COLUMN contentOwner INTEGER DEFAULT 0 NOT NULL""")
            }
        }


        @Suppress("MemberVisibilityCanBePrivate")
        internal val MIGRATION_69_70 =  DoorMigrationSync(69, 70) { database ->
            database.execSQL("ALTER TABLE Site ADD COLUMN authSalt TEXT")

            if(database.dbType() == DoorDbType.SQLITE) {
                database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth2 (  pauthUid  INTEGER  PRIMARY KEY  NOT NULL , pauthMechanism  TEXT , pauthAuth  TEXT , pauthLcsn  INTEGER  NOT NULL , pauthPcsn  INTEGER  NOT NULL , pauthLcb  INTEGER  NOT NULL , pauthLct  INTEGER  NOT NULL )")
                DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("PersonAuth2", 678,
                    "pauthUid", "pauthLcsn", "pauthPcsn").forEach {
                    database.execSQL(it)
                }
                DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("PersonAuth2", 678,
                    "pauthUid", "pauthLcsn", "pauthPcsn").forEach {
                    database.execSQL(it)
                }
                database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth2_trk (  epk  INTEGER  NOT NULL DEFAULT  0 , clientId  INTEGER  NOT NULL DEFAULT  0 , csn  INTEGER  NOT NULL DEFAULT  0 , rx  INTEGER  NOT NULL DEFAULT  0 , reqId  INTEGER  NOT NULL DEFAULT  0 , ts  INTEGER  NOT NULL DEFAULT  0 , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("CREATE  INDEX index_PersonAuth2_trk_clientId_epk_csn ON PersonAuth2_trk (clientId, epk, csn)")
                database.execSQL("CREATE UNIQUE INDEX index_PersonAuth2_trk_epk_clientId ON PersonAuth2_trk (epk, clientId)")

                database.execSQL("CREATE TABLE IF NOT EXISTS UserSession (  usPcsn  INTEGER  NOT NULL , usLcsn  INTEGER  NOT NULL , usLcb  INTEGER  NOT NULL , usLct  INTEGER  NOT NULL , usPersonUid  INTEGER  NOT NULL , usClientNodeId  INTEGER  NOT NULL , usStartTime  INTEGER  NOT NULL , usEndTime  INTEGER  NOT NULL , usStatus  INTEGER  NOT NULL , usReason  INTEGER  NOT NULL , usAuth  TEXT , usSessionType  INTEGER  NOT NULL , usUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("UserSession", 679, "usUid",
                    "usLcsn", "usPcsn").forEach {
                    database.execSQL(it)
                }
                DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("UserSession", 679, "usUid",
                    "usLcsn", "usPcsn").forEach {
                    database.execSQL(it)
                }
                database.execSQL("CREATE TABLE IF NOT EXISTS UserSession_trk (  epk  INTEGER  NOT NULL DEFAULT  0 , clientId  INTEGER  NOT NULL DEFAULT  0 , csn  INTEGER  NOT NULL DEFAULT  0 , rx  INTEGER  NOT NULL DEFAULT  0 , reqId  INTEGER  NOT NULL DEFAULT  0 , ts  INTEGER  NOT NULL DEFAULT  0 , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                database.execSQL("CREATE  INDEX index_UserSession_trk_clientId_epk_csn ON UserSession_trk (clientId, epk, csn)")
                database.execSQL("CREATE UNIQUE INDEX index_UserSession_trk_epk_clientId ON UserSession_trk (epk, clientId)")
            }else {
                database.execSQL("""
                    UPDATE Site
                       SET authSalt = '${randomString(20)}'
                """)
                database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth2 (  pauthUid  BIGINT  PRIMARY KEY  NOT NULL , pauthMechanism  TEXT , pauthAuth  TEXT , pauthLcsn  BIGINT  NOT NULL , pauthPcsn  BIGINT  NOT NULL , pauthLcb  INTEGER  NOT NULL , pauthLct  BIGINT  NOT NULL )")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonAuth2_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonAuth2_lcsn_seq")
                DoorSqlGenerator.generateSyncableEntityFunctionAndTriggerPostgres(entityName =
                "PersonAuth2", tableId = 678, pkFieldName = "pauthUid", localCsnFieldName =
                "pauthLcsn", primaryCsnFieldName = "pauthPcsn").forEach {
                    database.execSQL(it)
                }
                database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth2_trk (  epk  BIGINT  NOT NULL DEFAULT  0 , clientId  INTEGER  NOT NULL DEFAULT  0 , csn  INTEGER  NOT NULL DEFAULT  0 , rx  BOOL  NOT NULL DEFAULT  false , reqId  INTEGER  NOT NULL DEFAULT  0 , ts  BIGINT  NOT NULL DEFAULT  0 , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE  INDEX index_PersonAuth2_trk_clientId_epk_csn ON PersonAuth2_trk (clientId, epk, csn)")
                database.execSQL("CREATE UNIQUE INDEX index_PersonAuth2_trk_epk_clientId ON PersonAuth2_trk (epk, clientId)")

                database.execSQL("CREATE TABLE IF NOT EXISTS UserSession (  usPcsn  BIGINT  NOT NULL , usLcsn  BIGINT  NOT NULL , usLcb  INTEGER  NOT NULL , usLct  BIGINT  NOT NULL , usPersonUid  BIGINT  NOT NULL , usClientNodeId  INTEGER  NOT NULL , usStartTime  BIGINT  NOT NULL , usEndTime  BIGINT  NOT NULL , usStatus  INTEGER  NOT NULL , usReason  INTEGER  NOT NULL , usAuth  TEXT , usSessionType  INTEGER  NOT NULL , usUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS UserSession_mcsn_seq")
                database.execSQL("CREATE SEQUENCE IF NOT EXISTS UserSession_lcsn_seq")
                DoorSqlGenerator.generateSyncableEntityFunctionAndTriggerPostgres(entityName =
                "UserSession", tableId = 679, pkFieldName = "usUid", localCsnFieldName = "usLcsn",
                    primaryCsnFieldName = "usPcsn").forEach {
                    database.execSQL(it)
                }
                database.execSQL("CREATE TABLE IF NOT EXISTS UserSession_trk (  epk  BIGINT  NOT NULL DEFAULT  0 , clientId  INTEGER  NOT NULL DEFAULT  0 , csn  INTEGER  NOT NULL DEFAULT  0 , rx  BOOL  NOT NULL DEFAULT  false , reqId  INTEGER  NOT NULL DEFAULT  0 , ts  BIGINT  NOT NULL DEFAULT  0 , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE  INDEX index_UserSession_trk_clientId_epk_csn ON UserSession_trk (clientId, epk, csn)")
                database.execSQL("CREATE UNIQUE INDEX index_UserSession_trk_epk_clientId ON UserSession_trk (epk, clientId)")
            }

            database.execSQL("CREATE INDEX person_status_node_idx ON UserSession (usPersonUid, usStatus, usClientNodeId)")
            database.execSQL("CREATE INDEX node_status_person_idx ON UserSession (usClientNodeId, usStatus, usPersonUid)")


        }

        internal val MIGRATION_70_71 = DoorMigrationSync(70, 71) { database ->
            database.execSQL("CREATE INDEX idx_group_to_entity ON ScopedGrant (sgGroupUid, sgPermissions, sgTableId, sgEntityUid)")
            database.execSQL("CREATE INDEX idx_entity_to_group ON ScopedGrant (sgTableId, sgEntityUid, sgPermissions, sgGroupUid)")
            database.execSQL("DROP TABLE DeviceSession")
        }

        val MIGRATION_71_72 = DoorMigrationSync(71, 72) { database ->
                if(database.dbType() == DoorDbType.SQLITE) {

                    database.execSQL( "CREATE TABLE IF NOT EXISTS ClazzAssignment (`caUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `caTitle` TEXT, `caDescription` TEXT, `caDeadlineDate` INTEGER NOT NULL, `caStartDate` INTEGER NOT NULL, `caLateSubmissionType` INTEGER NOT NULL, `caLateSubmissionPenalty` INTEGER NOT NULL, `caGracePeriodDate` INTEGER NOT NULL, `caActive` INTEGER NOT NULL, `caClassCommentEnabled` INTEGER NOT NULL, `caPrivateCommentsEnabled` INTEGER NOT NULL, `caClazzUid` INTEGER NOT NULL, `caLocalChangeSeqNum` INTEGER NOT NULL, `caMasterChangeSeqNum` INTEGER NOT NULL, `caLastChangedBy` INTEGER NOT NULL, `caLct` INTEGER NOT NULL)")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (`cacjUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cacjContentUid` INTEGER NOT NULL, `cacjAssignmentUid` INTEGER NOT NULL, `cacjActive` INTEGER NOT NULL, `cacjMCSN` INTEGER NOT NULL, `cacjLCSN` INTEGER NOT NULL, `cacjLCB` INTEGER NOT NULL, `cacjLct` INTEGER NOT NULL)")

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_ClazzAssignment_trk_clientId_epk_csn` ON ClazzAssignment_trk (`clientId`, `epk`, `csn`)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ClazzAssignment_trk_epk_clientId` ON ClazzAssignment_trk (`epk`, `clientId`)")

                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin_trk (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epk` INTEGER NOT NULL, `clientId` INTEGER NOT NULL, `csn` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `reqId` INTEGER NOT NULL, `ts` INTEGER NOT NULL)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_ClazzAssignmentContentJoin_trk_clientId_epk_csn` ON ClazzAssignmentContentJoin_trk (`clientId`, `epk`, `csn`)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ClazzAssignmentContentJoin_trk_epk_clientId` ON ClazzAssignmentContentJoin_trk (`epk`, `clientId`)")

                    database.execSQL("""
          |CREATE TRIGGER INS_LOC_520
          |AFTER INSERT ON ClazzAssignment
          |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
          |    NEW.caLocalChangeSeqNum = 0)
          |BEGIN
          |    UPDATE ClazzAssignment
          |    SET caMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 520)
          |    WHERE caUid = NEW.caUid;
          |    
          |    UPDATE SqliteChangeSeqNums
          |    SET sCsnNextPrimary = sCsnNextPrimary + 1
          |    WHERE sCsnTableId = 520;
          |END
          """.trimMargin())

                    database.execSQL("""
          |            CREATE TRIGGER INS_PRI_520
          |            AFTER INSERT ON ClazzAssignment
          |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
          |                NEW.caMasterChangeSeqNum = 0)
          |            BEGIN
          |                UPDATE ClazzAssignment
          |                SET caMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 520)
          |                WHERE caUid = NEW.caUid;
          |                
          |                UPDATE SqliteChangeSeqNums
          |                SET sCsnNextPrimary = sCsnNextPrimary + 1
          |                WHERE sCsnTableId = 520;
          |                
          |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          |SELECT 520, NEW.caUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
          |            END
          """.trimMargin())

                    database.execSQL("""
          |CREATE TRIGGER UPD_LOC_520
          |AFTER UPDATE ON ClazzAssignment
          |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
          |    AND (NEW.caLocalChangeSeqNum == OLD.caLocalChangeSeqNum OR
          |        NEW.caLocalChangeSeqNum == 0))
          |BEGIN
          |    UPDATE ClazzAssignment
          |    SET caLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 520) 
          |    WHERE caUid = NEW.caUid;
          |    
          |    UPDATE SqliteChangeSeqNums 
          |    SET sCsnNextLocal = sCsnNextLocal + 1
          |    WHERE sCsnTableId = 520;
          |END
          """.trimMargin())
                    database.execSQL("""
          |            CREATE TRIGGER UPD_PRI_520
          |            AFTER UPDATE ON ClazzAssignment
          |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
          |                AND (NEW.caMasterChangeSeqNum == OLD.caMasterChangeSeqNum OR
          |                    NEW.caMasterChangeSeqNum == 0))
          |            BEGIN
          |                UPDATE ClazzAssignment
          |                SET caMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 520)
          |                WHERE caUid = NEW.caUid;
          |                
          |                UPDATE SqliteChangeSeqNums
          |                SET sCsnNextPrimary = sCsnNextPrimary + 1
          |                WHERE sCsnTableId = 520;
          |                
          |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          |SELECT 520, NEW.caUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
          |            END
          """.trimMargin())

                    database.execSQL("""
          |CREATE TRIGGER INS_LOC_521
          |AFTER INSERT ON ClazzAssignmentContentJoin
          |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
          |    NEW.cacjLCSN = 0)
          |BEGIN
          |    UPDATE ClazzAssignmentContentJoin
          |    SET cacjMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 521)
          |    WHERE cacjUid = NEW.cacjUid;
          |    
          |    UPDATE SqliteChangeSeqNums
          |    SET sCsnNextPrimary = sCsnNextPrimary + 1
          |    WHERE sCsnTableId = 521;
          |END
          """.trimMargin())

                    database.execSQL("""
          |            CREATE TRIGGER INS_PRI_521
          |            AFTER INSERT ON ClazzAssignmentContentJoin
          |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
          |                NEW.cacjMCSN = 0)
          |            BEGIN
          |                UPDATE ClazzAssignmentContentJoin
          |                SET cacjMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 521)
          |                WHERE cacjUid = NEW.cacjUid;
          |                
          |                UPDATE SqliteChangeSeqNums
          |                SET sCsnNextPrimary = sCsnNextPrimary + 1
          |                WHERE sCsnTableId = 521;
          |                
          |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          |SELECT 521, NEW.cacjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
          |            END
          """.trimMargin())
                    database.execSQL("""
          |CREATE TRIGGER UPD_LOC_521
          |AFTER UPDATE ON ClazzAssignmentContentJoin
          |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
          |    AND (NEW.cacjLCSN == OLD.cacjLCSN OR
          |        NEW.cacjLCSN == 0))
          |BEGIN
          |    UPDATE ClazzAssignmentContentJoin
          |    SET cacjLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 521) 
          |    WHERE cacjUid = NEW.cacjUid;
          |    
          |    UPDATE SqliteChangeSeqNums 
          |    SET sCsnNextLocal = sCsnNextLocal + 1
          |    WHERE sCsnTableId = 521;
          |END
          """.trimMargin())
                    database.execSQL("""
          |            CREATE TRIGGER UPD_PRI_521
          |            AFTER UPDATE ON ClazzAssignmentContentJoin
          |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
          |                AND (NEW.cacjMCSN == OLD.cacjMCSN OR
          |                    NEW.cacjMCSN == 0))
          |            BEGIN
          |                UPDATE ClazzAssignmentContentJoin
          |                SET cacjMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 521)
          |                WHERE cacjUid = NEW.cacjUid;
          |                
          |                UPDATE SqliteChangeSeqNums
          |                SET sCsnNextPrimary = sCsnNextPrimary + 1
          |                WHERE sCsnTableId = 521;
          |                
          |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          |SELECT 521, NEW.cacjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
          |            END
          """.trimMargin())

                }else{


                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment (  caTitle  TEXT , caDescription  TEXT , caDeadlineDate  BIGINT  NOT NULL ,  caStartDate  BIGINT  NOT NULL , caLateSubmissionType  INTEGER  NOT NULL , caLateSubmissionPenalty  INTEGER  NOT NULL , caGracePeriodDate  BIGINT  NOT NULL , caActive  BOOL  NOT NULL , caClassCommentEnabled  BOOL  NOT NULL , caPrivateCommentsEnabled  BOOL  NOT NULL , caClazzUid  BIGINT  NOT NULL , caLocalChangeSeqNum  BIGINT  NOT NULL , caMasterChangeSeqNum  BIGINT  NOT NULL , caLastChangedBy  INTEGER  NOT NULL , caLct  BIGINT  NOT NULL , caUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignment_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignment_lcsn_seq")
                    database.execSQL("""
          |CREATE OR REPLACE FUNCTION 
          | inccsn_520_fn() RETURNS trigger AS ${'$'}${'$'}
          | BEGIN  
          | UPDATE ClazzAssignment SET caLocalChangeSeqNum =
          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.caLocalChangeSeqNum 
          | ELSE NEXTVAL('ClazzAssignment_lcsn_seq') END),
          | caMasterChangeSeqNum = 
          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
          | THEN NEXTVAL('ClazzAssignment_mcsn_seq') 
          | ELSE NEW.caMasterChangeSeqNum END)
          | WHERE caUid = NEW.caUid;
          | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          | SELECT 520, NEW.caUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
          | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
          | RETURN null;
          | END ${'$'}${'$'}
          | LANGUAGE plpgsql
          """.trimMargin())
                    database.execSQL("""
          |CREATE TRIGGER inccsn_520_trig 
          |AFTER UPDATE OR INSERT ON ClazzAssignment 
          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
          |EXECUTE PROCEDURE inccsn_520_fn()
          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
          |CREATE 
          | INDEX index_ClazzAssignment_trk_clientId_epk_csn 
          |ON ClazzAssignment_trk (clientId, epk, csn)
          """.trimMargin())
                    database.execSQL("""
          |CREATE 
          |UNIQUE INDEX index_ClazzAssignment_trk_epk_clientId 
          |ON ClazzAssignment_trk (epk, clientId)
          """.trimMargin())
                    //End: Create table ClazzAssignment for PostgreSQL

                    //Begin: Create table ClazzAssignmentContentJoin for PostgreSQL
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (  cacjContentUid  BIGINT  NOT NULL , cacjAssignmentUid  BIGINT  NOT NULL , cacjActive  BOOL  NOT NULL , cacjMCSN  BIGINT  NOT NULL , cacjLCSN  BIGINT  NOT NULL , cacjLCB  INTEGER  NOT NULL , cacjLct  BIGINT  NOT NULL , cacjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignmentContentJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignmentContentJoin_lcsn_seq")
                    database.execSQL("""
          |CREATE OR REPLACE FUNCTION 
          | inccsn_521_fn() RETURNS trigger AS ${'$'}${'$'}
          | BEGIN  
          | UPDATE ClazzAssignmentContentJoin SET cacjLCSN =
          | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cacjLCSN 
          | ELSE NEXTVAL('ClazzAssignmentContentJoin_lcsn_seq') END),
          | cacjMCSN = 
          | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
          | THEN NEXTVAL('ClazzAssignmentContentJoin_mcsn_seq') 
          | ELSE NEW.cacjMCSN END)
          | WHERE cacjUid = NEW.cacjUid;
          | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
          | SELECT 521, NEW.cacjUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
          | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
          | RETURN null;
          | END ${'$'}${'$'}
          | LANGUAGE plpgsql
          """.trimMargin())
                    database.execSQL("""
          |CREATE TRIGGER inccsn_521_trig 
          |AFTER UPDATE OR INSERT ON ClazzAssignmentContentJoin 
          |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
          |EXECUTE PROCEDURE inccsn_521_fn()
          """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
          |CREATE 
          | INDEX index_ClazzAssignmentContentJoin_trk_clientId_epk_csn 
          |ON ClazzAssignmentContentJoin_trk (clientId, epk, csn)
          """.trimMargin())
                    database.execSQL("""
          |CREATE 
          |UNIQUE INDEX index_ClazzAssignmentContentJoin_trk_epk_clientId 
          |ON ClazzAssignmentContentJoin_trk (epk, clientId)
          """.trimMargin())


                }
        }

        val MIGRATION_72_73 = DoorMigrationSync(72, 73) { database ->
            if(database.dbType() == DoorDbType.SQLITE) {
                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentRollUp (`cacheUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cachePersonUid` INTEGER NOT NULL, `cacheContentEntryUid` INTEGER NOT NULL, `cacheClazzAssignmentUid` INTEGER NOT NULL, `cacheStudentScore` INTEGER NOT NULL, `cacheMaxScore` INTEGER NOT NULL, `cacheProgress` INTEGER NOT NULL, `cacheContentComplete` INTEGER NOT NULL, `lastCsnChecked` INTEGER NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ClazzAssignmentRollUp_cachePersonUid_cacheContentEntryUid_cacheClazzAssignmentUid` ON ClazzAssignmentRollUp (`cachePersonUid`, `cacheContentEntryUid`, `cacheClazzAssignmentUid`)")


            }else if(database.dbType() == DoorDbType.POSTGRES){

                database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentRollUp (  cachePersonUid  BIGINT  NOT NULL , cacheContentEntryUid  BIGINT  NOT NULL , cacheClazzAssignmentUid  BIGINT  NOT NULL , cacheStudentScore  INTEGER  NOT NULL , cacheMaxScore  INTEGER  NOT NULL , cacheProgress  INTEGER  NOT NULL , cacheContentComplete  BOOL  NOT NULL , lastCsnChecked  BIGINT  NOT NULL , cacheUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                database.execSQL("CREATE UNIQUE INDEX index_ClazzAssignmentRollUp_cachePersonUid_cacheContentEntryUid_cacheClazzAssignmentUid ON ClazzAssignmentRollUp (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid)")

            }
        }

        val MIGRATION_73_74 = DoorMigrationSync(73, 74) { database ->
            database.execSQL("ALTER TABLE ContentEntry ADD COLUMN completionCriteria INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE ContentEntry ADD COLUMN minScore INTEGER NOT NULL DEFAULT 0")

            database.execSQL("ALTER TABLE ClazzAssignmentRollUp ADD COLUMN cachePenalty INTEGER NOT NULL DEFAULT 0")

            if(database.dbType() == DoorDbType.SQLITE) {

                database.execSQL("ALTER TABLE ClazzAssignmentRollUp ADD COLUMN cacheSuccess INTEGER NOT NULL DEFAULT 0")

            }else if(database.dbType() == DoorDbType.POSTGRES){

                database.execSQL("ALTER TABLE ClazzAssignmentRollUp ADD COLUMN cacheSuccess SMALLINT NOT NULL DEFAULT 0")

            }
        }


        val MIGRATION_74_75 = DoorMigrationSync(74, 75) { database ->
            database.execSQL("DROP TABLE IF EXISTS ClazzWork")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkContentJoin")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestion")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestionOption")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestionResponse")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkSubmission")
            database.execSQL("DROP TABLE IF EXISTS ContentEntryProgress")
            database.execSQL("DROP TABLE IF EXISTS SelQuestionSetResponse")
            database.execSQL("DROP TABLE IF EXISTS ClazzWork_trk")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkContentJoin_trk")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestion_trk")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestionOption_trk")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkQuestionResponse_trk")
            database.execSQL("DROP TABLE IF EXISTS ClazzWorkSubmission_trk")
            database.execSQL("DROP TABLE IF EXISTS ContentEntryProgress_trk")
        }

        val MIGRATION_75_76 = DoorMigrationSync(75, 76) { database ->
            database.execSQL("CREATE INDEX IF NOT EXISTS index_StatementEntity_statementContentEntryUid_statementPersonUid_contentEntryRoot_timestamp_statementLocalChangeSeqNum ON StatementEntity (statementContentEntryUid, statementPersonUid, contentEntryRoot, timestamp, statementLocalChangeSeqNum)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_ClazzAssignment_caClazzUid ON ClazzAssignment (caClazzUid)")
        }

        //Fix adding clazz content permissions for existing teacher and student ScopedGrants.
        val MIGRATION_76_77 = DoorMigrationSync(76, 77) { database ->
            if(database.dbType() == DoorDbType.POSTGRES) {
                database.execSQL("""
                    UPDATE ScopedGrant 
                       SET sgPermissions = (sgPermissions | ${Role.PERMISSION_CLAZZ_CONTENT_SELECT})
                     WHERE (sgFlags & $FLAG_STUDENT_GROUP) = $FLAG_STUDENT_GROUP   
                """)

                val teacherAddPermissions = Role.PERMISSION_CLAZZ_CONTENT_SELECT or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                database.execSQL("""
                    UPDATE ScopedGrant 
                       SET sgPermissions = (sgPermissions | ${teacherAddPermissions})
                     WHERE (sgFlags & $FLAG_TEACHER_GROUP) = $FLAG_TEACHER_GROUP   
                """)

            }
        }


        val MIGRATION_77_78 = DoorMigrationSync(77, 78) { database ->
            database.execSQL("ALTER TABLE Clazz ADD COLUMN clazzParentsPersonGroupUid INTEGER NOT NULL DEFAULT 0")

            if(database.dbType() == DoorDbType.POSTGRES) {
                //Create a new PersonGroup for each class for the parents group
                database.execSQL("""
                    INSERT INTO PersonGroup (groupMasterCsn, groupLocalCsn, 
                                groupLastChangedBy, groupLct, groupName, groupActive, 
                                personGroupFlag)
                         SELECT 0 AS groupMasterCsn, 0 AS groupLocalCsn,
                                0 AS groupLastChangedBy,
                                0 AS groupLct,
                                ('Class-Parents-' || CAST(Clazz.clazzUid AS TEXT)) AS groupName,
                                true AS groupActive,
                                ${PersonGroup.PERSONGROUP_FLAG_PARENT_GROUP} AS personGroupFlag
                           FROM Clazz
                """)

                database.execSQL("""
                    UPDATE Clazz
                       SET clazzParentsPersonGroupUid =
                           (SELECT groupUid 
                              FROM PersonGroup
                             WHERE clazzParentsPersonGroupUid = 0
                               AND groupName = ('Class-Parents-' || CAST(Clazz.clazzUid AS TEXT)))  
                """)

                database.execSQL("""
                    UPDATE PersonGroup
                       SET groupName = 'Parents'
                     WHERE personGroupFlag =  ${PersonGroup.PERSONGROUP_FLAG_PARENT_GROUP}
                       AND groupName LIKE 'Class-Parents%'  
                """)
            }
        }

        val MIGRATION_79_80 = DoorMigrationStatementList(79, 80) { database ->
            if(database.dbType() == DoorDbType.SQLITE) {
                listOf(
                    "ALTER TABLE XLangMapEntry RENAME to XLangMapEntry_OLD",
                    "CREATE TABLE IF NOT EXISTS XLangMapEntry (  verbLangMapUid  INTEGER  NOT NULL , objectLangMapUid  INTEGER  NOT NULL , languageLangMapUid  INTEGER  NOT NULL , languageVariantLangMapUid  INTEGER  NOT NULL , valueLangMap  TEXT , statementLangMapMasterCsn  INTEGER  NOT NULL , statementLangMapLocalCsn  INTEGER  NOT NULL , statementLangMapLcb  INTEGER  NOT NULL , statementLangMapLct  INTEGER  NOT NULL , statementLangMapUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                    "INSERT INTO XLangMapEntry (verbLangMapUid, objectLangMapUid, languageLangMapUid, languageVariantLangMapUid, valueLangMap, statementLangMapMasterCsn, statementLangMapLocalCsn, statementLangMapLcb, statementLangMapLct, statementLangMapUid) SELECT verbLangMapUid, objectLangMapUid, languageLangMapUid, languageVariantLangMapUid, valueLangMap, statementLangMapMasterCsn, statementLangMapLocalCsn, statementLangMapLcb, statementLangMapLct, statementLangMapUid FROM XLangMapEntry_OLD",
                    "DROP TABLE XLangMapEntry_OLD",
                    "CREATE INDEX index_XLangMapEntry_verbLangMapUid ON XLangMapEntry (verbLangMapUid)",
                ) + DoorSqlGenerator.generateSyncableEntityInsertTriggersSqlite("XLangMapEntry",
                    74, "statementLangMapUid", "statementLangMapLocalCsn", "statementLangMapMasterCsn"
                ) + DoorSqlGenerator.generateSyncableEntityUpdateTriggersSqlite("XLangMapEntry",
                    74, "statementLangMapUid", "statementLangMapLocalCsn", "statementLangMapMasterCsn")
            }else {
                listOf()
            }
        }

        @JvmField
        val fooVar = 2

        val MIGRATION_80_81 = DoorMigrationStatementList(80, 81) { database ->
            if(database.dbType() == DoorDbType.SQLITE) {
                listOf(
                    "CREATE TABLE IF NOT EXISTS ContentJob (  toUri  TEXT , cjProgress  INTEGER  NOT NULL , cjTotal  INTEGER  NOT NULL , params  TEXT , cjUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                    "CREATE TABLE IF NOT EXISTS ContentJobItem (  cjiJobUid  INTEGER  NOT NULL , sourceUri  TEXT , cjiIsLeaf  INTEGER  NOT NULL , cjiContentEntryUid  INTEGER  NOT NULL , cjiParentContentEntryUid  INTEGER  NOT NULL , cjiContainerUid  INTEGER  NOT NULL , cjiProgress  INTEGER  NOT NULL , cjiTotal  INTEGER  NOT NULL , cjiStatus  INTEGER  NOT NULL , cjiConnectivityAcceptable  INTEGER  NOT NULL , cjiPluginId  INTEGER  NOT NULL , cjiUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                    "ALTER TABLE Site ADD COLUMN torrentAnnounceUrl TEXT"
                )
            }else {
                listOf(
                    "CREATE TABLE IF NOT EXISTS ContentJob (  toUri  TEXT , cjProgress  BIGINT  NOT NULL , cjTotal  BIGINT  NOT NULL , params  TEXT , cjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )",
                    "CREATE TABLE IF NOT EXISTS ContentJobItem (  cjiJobUid  BIGINT  NOT NULL , sourceUri  TEXT , cjiIsLeaf  BOOL  NOT NULL , cjiContentEntryUid  BIGINT  NOT NULL , cjiParentContentEntryUid  BIGINT  NOT NULL , cjiContainerUid  BIGINT  NOT NULL , cjiProgress  BIGINT  NOT NULL , cjiTotal  BIGINT  NOT NULL , cjiStatus  INTEGER  NOT NULL , cjiConnectivityAcceptable  INTEGER  NOT NULL , cjiPluginId  INTEGER  NOT NULL , cjiUid  BIGSERIAL  PRIMARY KEY  NOT NULL )",
                    "ALTER TABLE Site ADD COLUMN torrentAnnounceUrl TEXT"
                )
            }
        }

        val MIGRATION_81_82 = DoorMigrationStatementList(81, 82) { database ->
            listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiAttemptCount INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_82_83 = DoorMigrationStatementList(82, 83) { database ->
            listOf("DROP TABLE ContentJobItem") + if(database.dbType() == DoorDbType.SQLITE) {
                listOf("CREATE TABLE IF NOT EXISTS ContentJobItem (  cjiJobUid  INTEGER  NOT NULL , sourceUri  TEXT , cjiIsLeaf  INTEGER  NOT NULL , cjiContentEntryUid  INTEGER  NOT NULL , cjiParentContentEntryUid  INTEGER  NOT NULL , cjiContainerUid  INTEGER  NOT NULL , cjiItemProgress  INTEGER  NOT NULL , cjiItemTotal  INTEGER  NOT NULL , cjiRecursiveProgress  INTEGER  NOT NULL , cjiRecursiveTotal  INTEGER  NOT NULL , cjiStatus  INTEGER  NOT NULL , cjiConnectivityAcceptable  INTEGER  NOT NULL , cjiPluginId  INTEGER  NOT NULL , cjiAttemptCount  INTEGER  NOT NULL , cjiParentCjiUid  INTEGER  NOT NULL , cjiUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
            }else {
                listOf("CREATE TABLE IF NOT EXISTS ContentJobItem (  cjiJobUid  BIGINT  NOT NULL , sourceUri  TEXT , cjiIsLeaf  BOOL  NOT NULL , cjiContentEntryUid  BIGINT  NOT NULL , cjiParentContentEntryUid  BIGINT  NOT NULL , cjiContainerUid  BIGINT  NOT NULL , cjiItemProgress  BIGINT  NOT NULL , cjiItemTotal  BIGINT  NOT NULL , cjiRecursiveProgress  BIGINT  NOT NULL , cjiRecursiveTotal  BIGINT  NOT NULL , cjiStatus  INTEGER  NOT NULL , cjiConnectivityAcceptable  INTEGER  NOT NULL , cjiPluginId  INTEGER  NOT NULL , cjiAttemptCount  INTEGER  NOT NULL , cjiParentCjiUid  BIGINT  NOT NULL , cjiUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
            }
        }

        val MIGRATION_83_84 = DoorMigrationStatementList(83, 84) { database ->
            listOf()
        }


        val MIGRATION_84_85 = DoorMigrationStatementList(84, 85){ database ->
            listOf("ALTER TABLE ContentJob ADD COLUMN cjNotificationTitle TEXT",
                    "ALTER TABLE ContentJobItem ADD COLUMN cjiRecursiveStatus INTEGER NOT NULL DEFAULT 0") +
                    if(database.dbType() == DoorDbType.SQLITE){
               listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiServerJobId INTEGER NOT NULL DEFAULT 0")
            }else{
                listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiServerJobId BIGINT NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_85_86 = DoorMigrationStatementList(85, 86){ database ->
                    if(database.dbType() == DoorDbType.SQLITE){
                        listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiStartTime INTEGER NOT NULL DEFAULT 0",
                                "ALTER TABLE ContentJobItem ADD COLUMN cjiFinishTime INTEGER NOT NULL DEFAULT 0",
                                "ALTER TABLE ContentJobItem ADD COLUMN cjiConnectivityNeeded INTEGER NOT NULL DEFAULT 1",
                                "ALTER TABLE ContentJobItem RENAME to ContentJobItem_OLD",
                                "CREATE TABLE IF NOT EXISTS ContentJobItem (`cjiUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cjiJobUid` INTEGER NOT NULL, `sourceUri` TEXT, `cjiIsLeaf` INTEGER NOT NULL, `cjiContentEntryUid` INTEGER NOT NULL, `cjiParentContentEntryUid` INTEGER NOT NULL, `cjiContainerUid` INTEGER NOT NULL, `cjiItemProgress` INTEGER NOT NULL, `cjiItemTotal` INTEGER NOT NULL, `cjiRecursiveProgress` INTEGER NOT NULL, `cjiRecursiveTotal` INTEGER NOT NULL, `cjiStatus` INTEGER NOT NULL, `cjiRecursiveStatus` INTEGER NOT NULL, `cjiConnectivityNeeded` INTEGER NOT NULL, `cjiPluginId` INTEGER NOT NULL, `cjiAttemptCount` INTEGER NOT NULL, `cjiParentCjiUid` INTEGER NOT NULL, `cjiServerJobId` INTEGER NOT NULL, `cjiStartTime` INTEGER NOT NULL, `cjiFinishTime` INTEGER NOT NULL)",
                                "INSERT INTO ContentJobItem (cjiUid, cjiJobUid, sourceUri, cjiIsLeaf, cjiContentEntryUid, cjiParentContentEntryUid, cjiContainerUid, cjiItemProgress, cjiItemTotal, cjiRecursiveProgress, cjiRecursiveTotal, cjiStatus, cjiRecursiveStatus, cjiConnectivityNeeded, cjiPluginId, cjiAttemptCount, cjiParentCjiUid, cjiServerJobId, cjiStartTime, cjiFinishTime) SELECT cjiUid, cjiJobUid, sourceUri, cjiIsLeaf, cjiContentEntryUid, cjiParentContentEntryUid, cjiContainerUid, cjiItemProgress, cjiItemTotal, cjiRecursiveProgress, cjiRecursiveTotal, cjiStatus, cjiRecursiveStatus, cjiConnectivityNeeded, cjiPluginId, cjiAttemptCount, cjiParentCjiUid, cjiServerJobId, cjiStartTime, cjiFinishTime FROM ContentJobItem_OLD",
                                "DROP TABLE ContentJobItem_OLD",
                                "ALTER TABLE ContentJob ADD COLUMN cjIsMeteredAllowed INTEGER NOT NULL DEFAULT 0",
                                "CREATE INDEX IF NOT EXISTS `index_ContentJobItem_cjiContentEntryUid_cjiFinishTime` ON ContentJobItem (`cjiContentEntryUid`, `cjiFinishTime`)"
                        )
                    }else{
                        listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiStartTime BIGINT NOT NULL DEFAULT 0",
                                "ALTER TABLE ContentJobItem ADD COLUMN cjiFinishTime INTEGER NOT NULL DEFAULT 0",
                                "ALTER TABLE ContentJob ADD COLUMN cjIsMeteredAllowed BOOL NOT NULL DEFAULT FALSE",
                                "ALTER TABLE ContentJobItem ADD COLUMN cjiConnectivityNeeded BOOL NOT NULL DEFAULT FALSE",
                                "ALTER TABLE ContentJobItem DROP COLUMN cjiConnectivityAcceptable",
                                "CREATE INDEX index_ContentJobItem_cjiContentEntryUid_cjiFinishTime ON ContentJobItem (cjiContentEntryUid, cjiFinishTime)"
                        )
                    }
        }

        val MIGRATION_86_87 = DoorMigrationStatementList(86, 87) { database ->
            listOf("DROP TABLE IF EXISTS DownloadJob",
                    "DROP TABLE IF EXISTS DownloadJobItem",
                    "DROP TABLE IF EXISTS DownloadJobItemHistory",
                    "DROP TABLE IF EXISTS DownloadJobItemParentChildJoin",
                    "DROP TABLE IF EXISTS ContentEntryStatus")
        }

        val MIGRATION_87_88 = DoorMigrationStatementList(87, 88) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {
                listOf("ALTER TABLE Site RENAME to Site_OLD",
                        "CREATE TABLE IF NOT EXISTS Site (  sitePcsn  INTEGER  NOT NULL , siteLcsn  INTEGER  NOT NULL , siteLcb  INTEGER  NOT NULL , siteLct  INTEGER  NOT NULL , siteName  TEXT , guestLogin  INTEGER  NOT NULL , registrationAllowed  INTEGER  NOT NULL , authSalt  TEXT , siteUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                        "INSERT INTO Site (siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) SELECT siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt FROM Site_OLD",
                        "DROP TABLE Site_OLD"
                )
            } else {
                listOf("ALTER TABLE Site DROP COLUMN IF EXISTS torrentAnnounceUrl")
            }
        }

        val MIGRATION_88_89 = DoorMigrationStatementList(88, 89) { database ->
            listOf("ALTER TABLE ContentJobItem ADD COLUMN cjiUploadSessionUid TEXT")
        }

        val MIGRATION_89_90 = DoorMigrationStatementList(89, 90) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                listOf(
                    "ALTER TABLE ContentJobItem ADD COLUMN cjiContentDeletedOnCancellation INTEGER NOT NULL DEFAULT 0",
                    "ALTER TABLE ContentJobItem ADD COLUMN cjiContainerProcessed INTEGER NOT NULL DEFAULT 0")
            }else {
                listOf(
                    "ALTER TABLE ContentJobItem ADD COLUMN cjiContentDeletedOnCancellation BOOL NOT NULL DEFAULT false",
                    "ALTER TABLE ContentJobItem ADD COLUMN cjiContainerProcessed BOOL NOT NULL DEFAULT false"
                )
            }
        }

        val MIGRATION_90_91 = DoorMigrationStatementList(90, 91) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                DatabaseTriggers.sqliteContentJobItemTriggers.toList()
            }else {
                DatabaseTriggers.postgresContentJobItemTriggers.toList() + listOf(
                    "ALTER TABLE Language ALTER COLUMN languageactive DROP DEFAULT",
                    "ALTER TABLE Language ALTER COLUMN languageActive TYPE BOOL " +
                        "USING CASE WHEN CAST(LanguageActive AS INTEGER) = 0 THEN FALSE ELSE TRUE END"
                )
            }
        }

        val MIGRATION_92_93 = DoorMigrationStatementList(92, 93) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                listOf("ALTER TABLE SyncNode RENAME to SyncNode_OLD",
                    "CREATE TABLE IF NOT EXISTS SyncNode (  nodeClientId  INTEGER  PRIMARY KEY NOT NULL)",
                    "INSERT INTO SyncNode (nodeClientId) SELECT nodeClientId FROM SyncNode_OLD",
                    "DROP TABLE SyncNode_OLD")
            }else {
                listOf("ALTER TABLE SyncNode DROP COLUMN master")
            }
        }

        val MIGRATION_93_94 = DoorMigrationStatementList(93, 94) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                listOf(
                    "CREATE VIEW IF NOT EXISTS ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordReplicate.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordReplicate ON ClazzLogAttendanceRecordReplicate.clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ",
                    "DROP TRIGGER IF EXISTS clazzlogattendancerecord_remote_insert_ins",
                    "CREATE TRIGGER clazzlogattendancerecord_remote_insert_ins INSTEAD OF INSERT ON ClazzLogAttendanceRecord_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime) VALUES (NEW.clazzLogAttendanceRecordUid, NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordPersonUid, NEW.attendanceStatus, NEW.clazzLogAttendanceRecordMasterChangeSeqNum, NEW.clazzLogAttendanceRecordLocalChangeSeqNum, NEW.clazzLogAttendanceRecordLastChangedBy, NEW.clazzLogAttendanceRecordLastChangedTime) /*psql ON CONFLICT (clazzLogAttendanceRecordUid) DO UPDATE SET clazzLogAttendanceRecordClazzLogUid = EXCLUDED.clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid = EXCLUDED.clazzLogAttendanceRecordPersonUid, attendanceStatus = EXCLUDED.attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy = EXCLUDED.clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime = EXCLUDED.clazzLogAttendanceRecordLastChangedTime */; END")
            }else {
                listOf("CREATE OR REPLACE FUNCTION clazzlogattendancerecord_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime) VALUES (NEW.clazzLogAttendanceRecordUid, NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordPersonUid, NEW.attendanceStatus, NEW.clazzLogAttendanceRecordMasterChangeSeqNum, NEW.clazzLogAttendanceRecordLocalChangeSeqNum, NEW.clazzLogAttendanceRecordLastChangedBy, NEW.clazzLogAttendanceRecordLastChangedTime) ON CONFLICT (clazzLogAttendanceRecordUid) DO UPDATE SET clazzLogAttendanceRecordClazzLogUid = EXCLUDED.clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid = EXCLUDED.clazzLogAttendanceRecordPersonUid, attendanceStatus = EXCLUDED.attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy = EXCLUDED.clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime = EXCLUDED.clazzLogAttendanceRecordLastChangedTime ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql",
                    "DROP TRIGGER IF EXISTS clazzlogattendancerecord_remote_insert_trig ON ClazzLogAttendanceRecord_ReceiveView",
                    " CREATE TRIGGER clazzlogattendancerecord_remote_insert_trig INSTEAD OF INSERT ON ClazzLogAttendanceRecord_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazzlogattendancerecord_remote_insert_fn() ")
            }
        }

        val MIGRATION_94_95 = DoorMigrationStatementList(94, 95) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                listOf(
                    "DROP TRIGGER IF EXISTS ATTUPD_PersonPicture",
                    "DROP TABLE IF EXISTS ZombieAttachmentData",
                    "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableId  INTEGER  NOT NULL , zaPrimaryKey  INTEGER  NOT NULL , zaMd5  TEXT , zaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                    """
        |
        |        CREATE TRIGGER ATTUPD_PersonPicture
        |        AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
        |        OLD.personPictureMd5 IS NOT NULL
        |        BEGIN
        |        
        |        INSERT INTO ZombieAttachmentData(zaTableId, zaPrimaryKey, zaMd5) 
        |        SELECT 50 AS zaTableId, OLD.personPictureUid AS zaPrimaryKey, OLD.personPictureMd5 AS zaMd5
        |          FROM PersonPicture   
        |         WHERE PersonPicture.personPictureUid = OLD.personPictureUid
        |           AND (SELECT COUNT(*) 
        |                  FROM PersonPicture
        |                 WHERE personPictureMd5 = OLD.personPictureMd5) = 0
        |    ; 
        |        END
        |    
        """.trimMargin()
                )
            }else {
                listOf(
                    "DROP TRIGGER IF EXISTS attach_PersonPicture_trig ON PersonPicture",
                    "DROP TABLE IF EXISTS ZombieAttachmentData",
                    "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableId  INTEGER  NOT NULL , zaPrimaryKey  BIGINT  NOT NULL , zaMd5  TEXT , zaUid  BIGSERIAL  PRIMARY KEY  NOT NULL )",
                    """
        |    CREATE OR REPLACE FUNCTION attach_PersonPicture_fn() RETURNS trigger AS ${'$'}${'$'}
        |    BEGIN
        |    
        |    INSERT INTO ZombieAttachmentData(zaTableId, zaPrimaryKey, zaMd5) 
        |    SELECT 50 AS zaTableId, OLD.personPictureUid AS zaPrimaryKey, OLD.personPictureMd5 AS zaMd5
        |      FROM PersonPicture   
        |     WHERE PersonPicture.personPictureUid = OLD.personPictureUid
        |       AND (SELECT COUNT(*) 
        |              FROM PersonPicture
        |             WHERE personPictureMd5 = OLD.personPictureMd5) = 0
        |;
        |    RETURN NEW;
        |    END ${'$'}${'$'}
        |    LANGUAGE plpgsql
        """.trimMargin(),

                    """
        |CREATE TRIGGER attach_PersonPicture_trig
        |AFTER UPDATE ON PersonPicture
        |FOR EACH ROW WHEN (OLD.personPictureMd5 IS NOT NULL)
        |EXECUTE PROCEDURE attach_PersonPicture_fn();
        """.trimMargin())
            }
        }

        val MIGRATION_95_96 = DoorMigrationStatementList(95, 96) { db ->
            if(db.dbType() == DoorDbType.POSTGRES) {
                listOf(
                    "DROP VIEW PersonAuth2_receiveview",
                    "ALTER TABLE PersonAuth2 ALTER COLUMN pauthLcb TYPE BIGINT",
                    "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Replicate.* FROM PersonAuth2 LEFT JOIN PersonAuth2Replicate ON PersonAuth2Replicate.paPk = PersonAuth2.pauthUid",
                    "CREATE OR REPLACE FUNCTION personauth2_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) ON CONFLICT (pauthUid) DO UPDATE SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql",                    "CREATE TRIGGER personauth2_remote_insert_trig INSTEAD OF INSERT ON PersonAuth2_ReceiveView FOR EACH ROW EXECUTE PROCEDURE personauth2_remote_insert_fn()")
            }else {
                listOf()
            }
        }

        val MIGRATION_96_97 = DoorMigrationStatementList(96, 97) { db ->
            if(db.dbType() == DoorDbType.SQLITE) {
                listOf("DROP TABLE ZombieAttachmentData",
                    "CREATE TABLE ZombieAttachmentData (  zaUri  TEXT , zaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
                    "DROP TRIGGER IF EXISTS ATTUPD_PersonPicture",
                    """
                    |
                    |        CREATE TRIGGER ATTUPD_PersonPicture
                    |        AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
                    |        OLD.personPictureMd5 IS NOT NULL
                    |        BEGIN
                    |        
                    |        INSERT INTO ZombieAttachmentData(zaUri) 
                    |        SELECT OLD.personPictureUri AS zaUri
                    |          FROM PersonPicture   
                    |         WHERE PersonPicture.personPictureUid = OLD.personPictureUid
                    |           AND (SELECT COUNT(*) 
                    |                  FROM PersonPicture
                    |                 WHERE personPictureMd5 = OLD.personPictureMd5) = 0
                    |    ; 
                    |        END
                    |    
                    """.trimMargin(),
                )
            }else {
                listOf("DROP TABLE IF EXISTS ZombieAttachmentData",
                    "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaUri  TEXT , zaUid  SERIAL  PRIMARY KEY  NOT NULL )",
                    """
                    |    CREATE OR REPLACE FUNCTION attach_PersonPicture_fn() RETURNS trigger AS ${'$'}${'$'}
                    |    BEGIN
                    |    
                    |    INSERT INTO ZombieAttachmentData(zaUri) 
                    |    SELECT OLD.personPictureUri AS zaUri
                    |      FROM PersonPicture   
                    |     WHERE PersonPicture.personPictureUid = OLD.personPictureUid
                    |       AND (SELECT COUNT(*) 
                    |              FROM PersonPicture
                    |             WHERE personPictureMd5 = OLD.personPictureMd5) = 0
                    |;
                    |    RETURN NEW;
                    |    END ${'$'}${'$'}
                    |    LANGUAGE plpgsql
                    """.trimMargin()
                    )
            }
        }


        /***
         *  added 16th Feb to remove special html characters from text - & > <
         */
        val MIGRATION_97_98 = DoorMigrationStatementList(97, 98) { db ->
            if(db.dbType() == DoorDbType.POSTGRES) {
                db.fixTincan()
                listOf()
            }else {
                listOf()
            }
        }

        val MIGRATION_98_99 = DoorMigrationStatementList(98, 99) {db ->
            if(db.dbType() == DoorDbType.POSTGRES) {
                listOf("ALTER TABLE ContentJobItem ALTER COLUMN cjiFinishTime TYPE BIGINT")
            }else {
                listOf()
            }
        }

        /**
         * 27/Feb/2022 - Fixes an issue where there could be multiple ContainerEntryFile entities
         * for the same file (particularly if downloading was done simultaneously). This could lead
         * to problems identifying actual Zombie files, and then deleting real data.
         */
        val MIGRATION_99_100 = DoorMigrationStatementList(99, 100) {db ->
            listOf("""
  UPDATE ContainerEntry
     SET ceCefUid = 
         (SELECT CefOuter.cefUid
            FROM ContainerEntryFile CefOuter
           WHERE CefOuter.cefMd5 = 
		         (SELECT CefInner.cefMd5
				    FROM ContainerEntryFile CefInner
				   WHERE CefInner.cefUid = ContainerEntry.ceCefUid)
		ORDER BY CefOuter.cefUid
           LIMIT 1)
            """,
                """
DELETE FROM ContainerEntryFile 
      WHERE ContainerEntryFile.cefUid != 
            (SELECT CefInner.cefUid 
               FROM ContainerEntryFile CefInner
              WHERE CefInner.cefMd5 = ContainerEntryFile.cefMd5
           ORDER BY CefInner.cefUid
              LIMIT 1)
            """)
        }


        fun migrationList(nodeId: Long) = listOf<DoorMigration>(
            MIGRATION_44_45, MIGRATION_45_46, MIGRATION_46_47,
            MIGRATION_47_48, MIGRATION_48_49, MIGRATION_49_50, MIGRATION_50_51,
            MIGRATION_51_52, MIGRATION_52_53, MIGRATION_53_54, MIGRATION_54_55,
            MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59,
            MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63,
            MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, migrate67to68(nodeId),
            MIGRATION_68_69, MIGRATION_69_70, MIGRATION_70_71, MIGRATION_71_72,
            MIGRATION_72_73, MIGRATION_73_74, MIGRATION_74_75, MIGRATION_75_76,
            MIGRATION_76_77, MIGRATION_77_78, MIGRATION_78_79, MIGRATION_78_79,
            MIGRATION_79_80, MIGRATION_80_81, MIGRATION_81_82, MIGRATION_82_83, MIGRATION_83_84,
            MIGRATION_84_85, MIGRATION_85_86, MIGRATION_86_87, MIGRATION_87_88,
            MIGRATION_88_89, MIGRATION_89_90, MIGRATION_90_91,
            UmAppDatabaseReplicationMigration91_92, MIGRATION_92_93, MIGRATION_93_94, MIGRATION_94_95,
            MIGRATION_95_96, MIGRATION_96_97, MIGRATION_97_98, MIGRATION_98_99,
            MIGRATION_99_100
        )

        internal fun migrate67to68(nodeId: Long)= DoorMigrationSync(67, 68) { database ->
            if (database.dbType() == DoorDbType.SQLITE) {
                database.execSQL("CREATE TABLE IF NOT EXISTS DoorNode (  auth  TEXT , nodeId  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
            } else {
                database.execSQL("ALTER TABLE SyncNode ALTER COLUMN nodeClientId TYPE BIGINT")
                database.execSQL("CREATE TABLE IF NOT EXISTS DoorNode (  auth  TEXT , nodeId  SERIAL  PRIMARY KEY  NOT NULL )")
            }

            database.execSQL(
                """
                UPDATE SyncNode
                   SET nodeClientId = $nodeId
            """.trimIndent()
            )
        }
    }


}
