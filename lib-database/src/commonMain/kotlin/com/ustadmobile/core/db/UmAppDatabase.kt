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
    DownloadJob::class, DownloadJobItem::class, DownloadJobItemParentChildJoin::class, Person::class,
    Clazz::class, ClazzMember::class, PersonCustomFieldValue::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    EntityRole::class, PersonGroup::class, PersonGroupMember::class, Location::class,
    LocationAncestorJoin::class, PersonLocationJoin::class, PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ContentEntryStatus::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, XObjectEntity::class, StatementEntity::class,
    ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class, XLangMapEntry::class,
    SyncNode::class, LocallyAvailableContainer::class, ContainerETag::class,
    SyncResult::class,

    ClazzLog::class,ClazzLogAttendanceRecord::class, FeedEntry::class, PersonField::class,
    PersonDetailPresenterField::class,SelQuestion::class,
    SelQuestionResponse::class, SelQuestionResponseNomination::class, SelQuestionSet::class,
    SelQuestionSetRecognition::class, SelQuestionSetResponse::class,
    Schedule::class, DateRange::class, UMCalendar::class,
    ClazzActivity::class, ClazzActivityChange::class,
    SelQuestionOption::class, ScheduledCheck::class,
    AuditLog::class, CustomField::class, CustomFieldValue::class, CustomFieldValueOption::class,
    School::class, ClazzAssignment::class, ClazzAssignmentContentJoin::class

    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_TRACKER_ENTITIES

], version = 36)
@MinSyncVersion(36)
abstract class UmAppDatabase : DoorDatabase(), SyncableDoorDatabase {


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

    @JsName("personCustomFieldDao")
    abstract val personCustomFieldDao: PersonCustomFieldDao

    @JsName("personCustomFieldValueDao")
    abstract val personCustomFieldValueDao: PersonCustomFieldValueDao

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

    @JsName("locationDao")
    abstract val locationDao: LocationDao

    @JsName("locationAncestorJoinDao")
    abstract val locationAncestorJoinDao: LocationAncestorJoinDao

    @JsName("personLocationJoinDao")
    abstract val personLocationJoinDao: PersonLocationJoinDao

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

    @JsName("containerETagDao")
    abstract val containerETagDao: ContainerETagDao

    @JsName("verbDao")
    abstract val verbDao: VerbDao

    @JsName("xObjectDao")
    abstract val xObjectDao: XObjectDao

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

    abstract val syncresultDao : SyncResultDao

    abstract val auditLogDao : AuditLogDao
    abstract val clazzActivityChangeDao : ClazzActivityChangeDao
    abstract val clazzActivityDao : ClazzActivityDao
    abstract val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
    abstract val clazzLogDao : ClazzLogDao
    abstract val customFieldDao: CustomFieldDao
    abstract val customFieldValueDao : CustomFieldValueDao
    abstract val customFieldValueOptionDao : CustomFieldValueOptionDao
    abstract val dateRangeDao : DateRangeDao
    abstract val feedEntryDao : FeedEntryDao
    abstract val personDetailPresenterFieldDao : PersonDetailPresenterFieldDao
    abstract val scheduleDao : ScheduleDao
    abstract val scheduledCheckDao : ScheduledCheckDao
    abstract val selQuestionDao : SelQuestionDao
    abstract val selQuestionOptionDao : SelQuestionOptionDao
    abstract val selQuestionResponseDao : SelQuestionResponseDao
    abstract val selQuestionResponseNominationDao : SelQuestionResponseNominationDao
    abstract val selQuestionSetDao : SelQuestionSetDao
    abstract val selQuestionSetResponseDao : SelQuestionSetResponseDao
    abstract val umCalendarDao : UMCalendarDao
    abstract val schoolDao : SchoolDao
    abstract val clazzAssignmentDao : ClazzAssignmentDao
    abstract val clazzAssignmentContentJoinDao : ClazzAssignmentContentJoinDao

    @JsName("xLangMapEntryDao")
    abstract val xLangMapEntryDao: XLangMapEntryDao

    abstract val locallyAvailableContainerDao: LocallyAvailableContainerDao

    //TODO: DO NOT REMOVE THIS COMMENT!
    //#DOORDB_SYNCDAO


    companion object {

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


        val MIGRATION_27_28 = object : DoorMigration(27, 28) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("DROP TABLE EntryStatusResponse")
            }
        }

        val MIGRATION_30_31 = object : DoorMigration(30, 31) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS ContainerETag (  ceContainerUid  BIGINT  PRIMARY KEY  NOT NULL , cetag  TEXT )")
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

        val MIGRATION_35_36 = object : DoorMigration(35, 36) {
            override fun migrate(database: DoorSqlDatabase) {
                database.execSQL("ALTER TABLE StatementEntity ALTER COLUMN resultScoreScaled TYPE FLOAT")
            }
        }

        val MIGRATION_34_35 = object : DoorMigration(34, 35){
            override fun migrate(database: DoorSqlDatabase) {
                if(database.dbType() == DoorDbType.SQLITE){

                    //Drop PersonCustomField ( cause its not PersonField)
                    database.execSQL("DROP TABLE PersonCustomField")

                    //PersonGroupMember migration:
                    //Begin: Create table PersonGroupMember for SQLite
                    database.execSQL("ALTER TABLE PersonGroupMember RENAME to PersonGroupMember_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS PersonGroupMember (  
                        |groupMemberActive  INTEGER NOT NULL , 
                        |groupMemberPersonUid  INTEGER NOT NULL , 
                        |groupMemberGroupUid  INTEGER NOT NULL , 
                        |groupMemberMasterCsn  INTEGER NOT NULL , 
                        |groupMemberLocalCsn  INTEGER NOT NULL , 
                        |groupMemberLastChangedBy  INTEGER NOT NULL , 
                        |groupMemberUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""".trimMargin())
                    database.execSQL("""INSERT INTO PersonGroupMember (
                        |groupMemberUid, groupMemberActive, groupMemberPersonUid, 
                        |groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, 
                        |groupMemberLastChangedBy) 
                        |SELECT groupMemberUid, 1, 
                        |groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, 
                        |groupMemberLocalCsn, groupMemberLastChangedBy 
                        |FROM PersonGroupMember_OLD""".trimMargin())
                    database.execSQL("DROP TABLE PersonGroupMember_OLD")

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroupMember_groupMemberGroupUid 
                    |ON PersonGroupMember (groupMemberGroupUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroupMember_groupMemberPersonUid 
                    |ON PersonGroupMember (groupMemberPersonUid)
                    """.trimMargin())

                    //Statement migration
                    //Begin: Create table StatementEntity for SQLite
                    database.execSQL("ALTER TABLE StatementEntity RENAME to StatementEntity_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS StatementEntity (  
                        |statementId  TEXT , 
                        |personUid  INTEGER NOT NULL , 
                        |verbUid  INTEGER NOT NULL , 
                        |xObjectUid  INTEGER NOT NULL , 
                        |subStatementActorUid  INTEGER NOT NULL , 
                        |substatementVerbUid  INTEGER NOT NULL , 
                        |subStatementObjectUid  INTEGER NOT NULL , 
                        |agentUid  INTEGER NOT NULL , instructorUid  INTEGER NOT NULL , 
                        |authorityUid  INTEGER NOT NULL , teamUid  INTEGER NOT NULL , 
                        |resultCompletion  INTEGER NOT NULL , resultSuccess  INTEGER NOT NULL , 
                        |resultScoreScaled  INTEGER NOT NULL , resultScoreRaw  INTEGER NOT NULL , 
                        |resultScoreMin  INTEGER NOT NULL , resultScoreMax  INTEGER NOT NULL , 
                        |resultDuration  INTEGER NOT NULL , resultResponse  TEXT , 
                        |timestamp  INTEGER NOT NULL , stored  INTEGER NOT NULL , 
                        |contextRegistration  TEXT , contextPlatform  TEXT , 
                        |contextStatementId  TEXT , fullStatement  TEXT , 
                        |statementMasterChangeSeqNum  INTEGER NOT NULL , 
                        |statementLocalChangeSeqNum  INTEGER NOT NULL , 
                        |statementLastChangedBy  INTEGER NOT NULL , 
                        |extensionProgress  INTEGER NOT NULL , 
                        |statementContentEntryUid  INTEGER NOT NULL , 
                        |statementUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""".trimMargin())
                    database.execSQL("""INSERT INTO StatementEntity (
                        |statementUid, statementId, personUid, verbUid, xObjectUid, 
                        |subStatementActorUid, substatementVerbUid, subStatementObjectUid, 
                        |agentUid, instructorUid, authorityUid, teamUid, resultCompletion, 
                        |resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, 
                        |resultScoreMax, resultDuration, resultResponse, timestamp, stored, 
                        |contextRegistration, contextPlatform, contextStatementId, 
                        |fullStatement, statementMasterChangeSeqNum, 
                        |statementLocalChangeSeqNum, statementLastChangedBy, 
                        |extensionProgress, statementContentEntryUid) 
                        |SELECT statementUid, statementId, personUid, verbUid, xObjectUid, 
                        |subStatementActorUid, substatementVerbUid, subStatementObjectUid, 
                        |agentUid, instructorUid, authorityUid, teamUid, resultCompletion, 
                        |resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, 
                        |resultScoreMax, resultDuration, resultResponse, timestamp, stored, 
                        |contextRegistration, contextPlatform, contextStatementId, 
                        |fullStatement, statementMasterChangeSeqNum, 
                        |statementLocalChangeSeqNum, statementLastChangedBy, 
                        |0, 0 FROM StatementEntity_OLD""".trimMargin())
                    database.execSQL("DROP TABLE StatementEntity_OLD")


                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonCustomFieldValue_trk (" +
                            "  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL , csn  INTEGER NOT NULL , rx  INTEGER NOT NULL ," +
                            " reqId  INTEGER NOT NULL , ts  INTEGER NOT NULL , " +
                            " pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonCustomFieldValue_trk_clientId_epk_rx_csn 
                    |ON PersonCustomFieldValue_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    database.execSQL("ALTER TABLE Location RENAME to Location_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS Location (  
                         title  TEXT , description  TEXT , lng  TEXT , lat  TEXT , 
                         parentLocationUid  INTEGER NOT NULL , locationLocalChangeSeqNum  INTEGER NOT NULL , 
                         locationMasterChangeSeqNum  INTEGER NOT NULL , locationLastChangedBy  INTEGER NOT NULL , 
                         timeZone  TEXT , locationActive  INTEGER NOT NULL , 
                         locationUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    database.execSQL("INSERT INTO Location (" +
                            "locationUid, title, description, lng, lat, " +
                            "parentLocationUid, locationLocalChangeSeqNum, " +
                            "locationMasterChangeSeqNum, locationLastChangedBy, timeZone, " +
                            "locationActive) SELECT locationUid, title, description, lng, " +
                            "lat, parentLocationUid, locationLocalChangeSeqNum, " +
                            "locationMasterChangeSeqNum, locationLastChangedBy, " +
                            "'', locationActive FROM Location_OLD")
                    database.execSQL("DROP TABLE Location_OLD")

                    //Begin: Create table EntityRole for SQLite
                    database.execSQL("ALTER TABLE EntityRole RENAME to EntityRole_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS EntityRole (  
                        erMasterCsn  INTEGER NOT NULL , erLocalCsn  INTEGER NOT NULL , erLastChangedBy  INTEGER NOT NULL , 
                        erTableId  INTEGER NOT NULL , erEntityUid  INTEGER NOT NULL , erGroupUid  INTEGER NOT NULL , 
                        erRoleUid  INTEGER NOT NULL , erActive  INTEGER NOT NULL , 
                        erUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    database.execSQL("""INSERT INTO EntityRole (
                         erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, 
                         erEntityUid, erGroupUid, erRoleUid, erActive) 
                         SELECT erUid, erMasterCsn, erLocalCsn, erLastChangedBy, 
                         erTableId, erEntityUid, erGroupUid, erRoleUid, 1 
                         FROM EntityRole_OLD""")
                    database.execSQL("DROP TABLE EntityRole_OLD")

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_EntityRole_erEntityUid 
                    |ON EntityRole (erEntityUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_EntityRole_erGroupUid 
                    |ON EntityRole (erGroupUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_EntityRole_erRoleUid 
                    |ON EntityRole (erRoleUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_EntityRole_erTableId 
                    |ON EntityRole (erTableId)
                    """.trimMargin())

                    //Begin: Create table PersonGroup for SQLite
                    database.execSQL("ALTER TABLE PersonGroup RENAME to PersonGroup_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS PersonGroup (  
                    groupMasterCsn  INTEGER NOT NULL , groupLocalCsn  INTEGER NOT NULL , 
                    groupLastChangedBy  INTEGER NOT NULL , groupName  TEXT , 
                    groupActive  INTEGER NOT NULL , groupPersonUid  INTEGER NOT NULL , 
                    groupUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    database.execSQL("""INSERT INTO PersonGroup (
                        groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy,
                         groupName, groupActive, groupPersonUid) SELECT groupUid, 
                         groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, 
                         1, groupPersonUid FROM PersonGroup_OLD""")
                    database.execSQL("DROP TABLE PersonGroup_OLD")

                    //Begin: Person for SQLite
                    database.execSQL("ALTER TABLE Person RENAME to Person_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS Person (  
                        username  TEXT , firstNames  TEXT , lastName  TEXT , emailAddr  TEXT ,  
                        phoneNum  TEXT , gender  INTEGER NOT NULL , active  INTEGER NOT NULL ,  
                        admin  INTEGER NOT NULL , personNotes  TEXT , fatherName  TEXT ,  
                        fatherNumber  TEXT , motherName  TEXT , motherNum  TEXT ,  
                        dateOfBirth  INTEGER NOT NULL , personAddress  TEXT ,  
                        personMasterChangeSeqNum  INTEGER NOT NULL ,  
                        personLocalChangeSeqNum  INTEGER NOT NULL ,  
                        personLastChangedBy  INTEGER NOT NULL ,  
                        personUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    database.execSQL("""INSERT INTO Person ( 
                        personUid, username, firstNames, lastName, emailAddr, phoneNum,  
                        gender, active, admin, personNotes, fatherName, fatherNumber,  
                        motherName, motherNum, dateOfBirth, personAddress,  
                        personMasterChangeSeqNum, personLocalChangeSeqNum,  
                        personLastChangedBy)  
                        SELECT personUid, username, firstNames, lastName, emailAddr,  
                        phoneNum, gender, active, admin,
                        '', '', '', '', '', 0, '', 
                        personMasterChangeSeqNum, personLocalChangeSeqNum,  
                        personLastChangedBy FROM Person_OLD""")
                    database.execSQL("DROP TABLE Person_OLD")

                    //Begin: Migrate PersonCustomFieldValue for SQLite
                    database.execSQL("ALTER TABLE PersonCustomFieldValue RENAME to PersonCustomFieldValue_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS PersonCustomFieldValue (  
                        personCustomFieldValuePersonCustomFieldUid  INTEGER NOT NULL , 
                        personCustomFieldValuePersonUid  INTEGER NOT NULL , 
                        fieldValue  TEXT , 
                        personCustomFieldValueMasterChangeSeqNum  INTEGER NOT NULL , 
                        personCustomFieldValueLocalChangeSeqNum  INTEGER NOT NULL , 
                        personCustomFieldValueLastChangedBy  INTEGER NOT NULL , 
                        personCustomFieldValueUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    database.execSQL("""INSERT INTO PersonCustomFieldValue (
                        personCustomFieldValueUid, personCustomFieldValuePersonCustomFieldUid,  
                        personCustomFieldValuePersonUid, fieldValue, 
                        personCustomFieldValueMasterChangeSeqNum, 
                        personCustomFieldValueLocalChangeSeqNum, 
                        personCustomFieldValueLastChangedBy) 
                        SELECT personCustomFieldValueUid, 
                        personCustomFieldValuePersonCustomFieldUid, 
                        personCustomFieldValuePersonUid, fieldValue,  
                        0, 0, 0 
                        FROM PersonCustomFieldValue_OLD""")
                    database.execSQL("DROP TABLE PersonCustomFieldValue_OLD")

                    //Begin: migrate clazz for sqlite
                    //Begin: Create table Clazz for SQLite
                    database.execSQL("ALTER TABLE Clazz RENAME to Clazz_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Clazz (  " +
                            "clazzName  TEXT , clazzDesc  TEXT , " +
                            "attendanceAverage  REAL NOT NULL , " +
                            "clazzHolidayUMCalendarUid  INTEGER NOT NULL , " +
                            "clazzScheuleUMCalendarUid  INTEGER NOT NULL , " +
                            "isClazzActive  INTEGER NOT NULL , " +
                            "clazzLocationUid  INTEGER NOT NULL , clazzStartTime  INTEGER NOT NULL , " +
                            "clazzEndTime  INTEGER NOT NULL , clazzFeatures  INTEGER NOT NULL , " +
                            "clazzMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzLastChangedBy  INTEGER NOT NULL , " +
                            "clazzUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("DROP TABLE Clazz_OLD")

                    //Begin: migrate personauth for sqlite
                    //Begin: Create table PersonAuth for SQLite
                    database.execSQL("ALTER TABLE PersonAuth RENAME to PersonAuth_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth (  " +
                            "passwordHash  TEXT , " +
                            "personAuthStatus  INTEGER NOT NULL, " +
                            "personAuthLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "personAuthMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "lastChangedBy  INTEGER NOT NULL , " +
                            "personAuthUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("INSERT INTO PersonAuth (personAuthUid, passwordHash, " +
                            "personAuthStatus, personAuthLocalChangeSeqNum, " +
                            "personAuthMasterChangeSeqNum, lastChangedBy) " +
                            "SELECT personAuthUid, passwordHash, 0, " +
                            "0, " +
                            "0, 0 FROM PersonAuth_OLD")
                    database.execSQL("DROP TABLE PersonAuth_OLD")
                    //End: Create table PersonAuth for SQLite


                    //Begin: migrate role for SQLite
                    database.execSQL("ALTER TABLE Role RENAME to Role_OLD")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS Role (  
                        roleName  TEXT , 
                        roleActive  INTEGER NOT NULL , 
                        roleMasterCsn  INTEGER NOT NULL , 
                        roleLocalCsn  INTEGER NOT NULL , 
                        roleLastChangedBy  INTEGER NOT NULL , 
                        rolePermissions  INTEGER NOT NULL , 
                        roleUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    database.execSQL("""INSERT INTO Role (
                        roleUid, roleName, roleActive, roleMasterCsn, 
                        roleLocalCsn, roleLastChangedBy, rolePermissions
                        ) 
                        SELECT roleUid, roleName, 1, roleMasterCsn, 
                        roleLocalCsn, roleLastChangedBy, rolePermissions 
                        FROM Role_OLD""")
                    database.execSQL("DROP TABLE Role_OLD")


                    //Begin: Create table ClazzMember for SQLite
                    database.execSQL("ALTER TABLE ClazzMember RENAME to ClazzMember_OLD")
                    database.execSQL("DROP TABLE ClazzMember_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzMember (  " +
                            "clazzMemberPersonUid  INTEGER NOT NULL , " +
                            "clazzMemberClazzUid  INTEGER NOT NULL , " +
                            "clazzMemberDateJoined  INTEGER NOT NULL , " +
                            "clazzMemberDateLeft  INTEGER NOT NULL , " +
                            "clazzMemberRole  INTEGER NOT NULL , " +
                            "clazzMemberAttendancePercentage  REAL NOT NULL , " +
                            "clazzMemberActive  INTEGER NOT NULL , " +
                            "clazzMemberLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzMemberMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzMemberLastChangedBy  INTEGER NOT NULL , " +
                            "clazzMemberUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL " +
                            ")")

                    database.execSQL("""
                    |CREATE
                    | INDEX index_ClazzMember_clazzMemberPersonUid
                    |ON ClazzMember (clazzMemberPersonUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE
                    | INDEX index_ClazzMember_clazzMemberClazzUid
                    |ON ClazzMember (clazzMemberClazzUid)
                    """.trimMargin())
                    //End: Create table ClazzMember for SQLite


                    //Begin: Create table ClazzLog for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzLog RENAME to ClazzLog_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog (  clazzLogClazzUid  INTEGER NOT NULL , logDate  INTEGER NOT NULL , timeRecorded  INTEGER NOT NULL , clazzLogDone  INTEGER NOT NULL , clazzLogCancelled  INTEGER NOT NULL , clazzLogNumPresent  INTEGER NOT NULL, clazzLogNumAbsent  INTEGER NOT NULL, clazzLogNumPartial  INTEGER NOT NULL, clazzLogScheduleUid  INTEGER NOT NULL , clazzLogMSQN  INTEGER NOT NULL , clazzLogLCSN  INTEGER NOT NULL , clazzLogLCB  INTEGER NOT NULL, clazzLogUid  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzLog (clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogMSQN, clazzLogLCSN, clazzLogLCB) SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogMSQN, clazzLogLCSN, clazzLogLCB FROM ClazzLog_OLD")
                    database.execSQL("DROP TABLE ClazzLog_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  " +
                            "INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER NOT NULL " +
                            " PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzLog_trk_clientId_epk_rx_csn 
                    |ON ClazzLog_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzLog for SQLite

                    //Begin: Create table ClazzLogAttendanceRecord for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzLogAttendanceRecord RENAME to ClazzLogAttendanceRecord_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord (  " +
                            "clazzLogAttendanceRecordClazzLogUid  INTEGER NOT NULL , " +
                            "clazzLogAttendanceRecordClazzMemberUid  INTEGER NOT NULL , " +
                            "attendanceStatus  INTEGER NOT NULL, " +
                            "clazzLogAttendanceRecordMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzLogAttendanceRecordLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "clazzLogAttendanceRecordLastChangedBy  INTEGER NOT NULL, " +
                            "clazzLogAttendanceRecordUid  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzLogAttendanceRecord (clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy) SELECT clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy FROM ClazzLogAttendanceRecord_OLD")
                    database.execSQL("DROP TABLE ClazzLogAttendanceRecord_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord_trk (  " +
                            "epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, " +
                            "csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, " +
                            "ts  INTEGER NOT NULL , pk  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn 
                    |ON ClazzLogAttendanceRecord_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzLogAttendanceRecord for SQLite

                    //Begin: Create table FeedEntry for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE FeedEntry RENAME to FeedEntry_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS FeedEntry (  " +
                            "feedEntryPersonUid  INTEGER NOT NULL , title  TEXT , " +
                            "description  TEXT , link  TEXT , feedEntryClazzName  TEXT ," +
                            " deadline  INTEGER NOT NULL , feedEntryHash  INTEGER NOT NULL ," +
                            " feedEntryDone  INTEGER NOT NULL , feedEntryClazzLogUid  INTEGER NOT NULL , " +
                            "dateCreated  INTEGER NOT NULL , feedEntryCheckType  INTEGER NOT NULL, " +
                            "feedEntryLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "feedEntryMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "feedEntryLastChangedBy  INTEGER NOT NULL, feedEntryUid  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO FeedEntry (feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy) SELECT feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy FROM FeedEntry_OLD")
                    database.execSQL("DROP TABLE FeedEntry_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS FeedEntry_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_FeedEntry_trk_clientId_epk_rx_csn 
                    |ON FeedEntry_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table FeedEntry for SQLite

                    //Begin: Create table PersonField for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE PersonField RENAME to PersonField_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonField (  fieldName  TEXT , labelMessageId  INTEGER NOT NULL, fieldIcon  TEXT , personFieldMasterChangeSeqNum  INTEGER NOT NULL , personFieldLocalChangeSeqNum  INTEGER NOT NULL , personFieldLastChangedBy  INTEGER NOT NULL, personCustomFieldUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO PersonField (personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy) SELECT personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy FROM PersonField_OLD")
                    database.execSQL("DROP TABLE PersonField_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonField_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonField_trk_clientId_epk_rx_csn 
                    |ON PersonField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table PersonField for SQLite

                    //Begin: Create table PersonDetailPresenterField for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE PersonDetailPresenterField RENAME to PersonDetailPresenterField_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS PersonDetailPresenterField (  
                        fieldUid  INTEGER NOT NULL , 
                        fieldType  INTEGER NOT NULL, 
                        fieldIndex  INTEGER NOT NULL, 
                        labelMessageId  INTEGER NOT NULL, 
                        fieldIcon  TEXT , 
                        headerMessageId  INTEGER NOT NULL, 
                        viewModeVisible  INTEGER NOT NULL , 
                        editModeVisible  INTEGER NOT NULL , 
                        isReadyOnly  INTEGER NOT NULL , 
                        personDetailPresenterFieldMasterChangeSeqNum  INTEGER NOT NULL , 
                        personDetailPresenterFieldLocalChangeSeqNum  INTEGER NOT NULL , 
                        personDetailPresenterFieldLastChangedBy  INTEGER NOT NULL, 
                        personDetailPresenterFieldUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO PersonDetailPresenterField (personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, isReadyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy) SELECT personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, isReadyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy FROM PersonDetailPresenterField_OLD")
                    database.execSQL("DROP TABLE PersonDetailPresenterField_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonDetailPresenterField_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonDetailPresenterField_trk_clientId_epk_rx_csn 
                    |ON PersonDetailPresenterField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table PersonDetailPresenterField for SQLite

                    //Begin: Create table SelQuestion for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestion RENAME to SelQuestion_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestion (  questionText  TEXT , selQuestionSelQuestionSetUid  INTEGER NOT NULL , questionIndex  INTEGER NOT NULL, assignToAllClasses  INTEGER NOT NULL , multiNominations  INTEGER NOT NULL , questionType  INTEGER NOT NULL, questionActive  INTEGER NOT NULL , selQuestionMasterChangeSeqNum  INTEGER NOT NULL , selQuestionLocalChangeSeqNum  INTEGER NOT NULL , selQuestionLastChangedBy  INTEGER NOT NULL, selQuestionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestion (selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy) SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion_OLD")
                    database.execSQL("DROP TABLE SelQuestion_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestion_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestion_trk_clientId_epk_rx_csn 
                    |ON SelQuestion_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestion for SQLite

                    //Begin: Create table SelQuestionResponse for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionResponse RENAME to SelQuestionResponse_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS SelQuestionResponse (  
                        selQuestionResponseSelQuestionSetResponseUid  INTEGER NOT NULL , 
                        selQuestionResponseSelQuestionUid  INTEGER NOT NULL , 
                        selQuestionResponseMasterChangeSeqNum  INTEGER NOT NULL , 
                        selQuestionResponseLocalChangeSeqNum  INTEGER NOT NULL , 
                        selQuestionResponseLastChangedBy  INTEGER NOT NULL, 
                        selQuestionResponseUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionResponse (selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy) SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse_OLD")
                    database.execSQL("DROP TABLE SelQuestionResponse_OLD")
                    END MIGRATION*/
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
                    database.execSQL("""CREATE TABLE IF NOT EXISTS SelQuestionResponse_trk (  
                        epk  INTEGER NOT NULL , 
                        clientId  INTEGER NOT NULL, 
                        csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, 
                        ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionResponse_trk_clientId_epk_rx_csn 
                    |ON SelQuestionResponse_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionResponse for SQLite

                    //Begin: Create table SelQuestionResponseNomination for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionResponseNomination RENAME to SelQuestionResponseNomination_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponseNomination (  selqrnClazzMemberUid  INTEGER NOT NULL , selqrnSelQuestionResponseUId  INTEGER NOT NULL , nominationActive  INTEGER NOT NULL , selqrnMCSN  INTEGER NOT NULL , selqrnMCSNLCSN  INTEGER NOT NULL , selqrnMCSNLCB  INTEGER NOT NULL, selqrnUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionResponseNomination (selqrnUid, selqrnClazzMemberUid, selqrnSelQuestionResponseUId, nominationActive, selqrnMCSN, selqrnMCSNLCSN, selqrnMCSNLCB) SELECT selqrnUid, selqrnClazzMemberUid, selqrnSelQuestionResponseUId, nominationActive, selqrnMCSN, selqrnMCSNLCSN, selqrnMCSNLCB FROM SelQuestionResponseNomination_OLD")
                    database.execSQL("DROP TABLE SelQuestionResponseNomination_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponseNomination_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionResponseNomination_trk_clientId_epk_rx_csn 
                    |ON SelQuestionResponseNomination_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionResponseNomination for SQLite

                    //Begin: Create table SelQuestionSet for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSet RENAME to SelQuestionSet_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSet (  title  TEXT , selQuestionSetMasterChangeSeqNum  INTEGER NOT NULL , selQuestionSetLocalChangeSeqNum  INTEGER NOT NULL , selQuestionSetLastChangedBy  INTEGER NOT NULL, selQuestionSetUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSet (selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy) SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet_OLD")
                    database.execSQL("DROP TABLE SelQuestionSet_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSet_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSet_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSet_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSet for SQLite

                    //Begin: Create table SelQuestionSetRecognition for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSetRecognition RENAME to SelQuestionSetRecognition_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetRecognition (  selqsrSelQuestionSetResponseUid  INTEGER NOT NULL , selQuestionSetRecognitionClazzMemberUid  INTEGER NOT NULL , isSelQuestionSetRecognitionRecognized  INTEGER NOT NULL , selQuestionSetRecognitionMasterChangeSeqNum  INTEGER NOT NULL , selQuestionSetRecognitionLocalChangeSeqNum  INTEGER NOT NULL , selQuestionSetRecognitionLastChangedBy  INTEGER NOT NULL, selQuestionSetRecognitionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSetRecognition (selQuestionSetRecognitionUid, selqsrSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, isSelQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy) SELECT selQuestionSetRecognitionUid, selqsrSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, isSelQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition_OLD")
                    database.execSQL("DROP TABLE SelQuestionSetRecognition_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetRecognition_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSetRecognition_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSetRecognition_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSetRecognition for SQLite

                    //Begin: Create table SelQuestionSetResponse for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSetResponse RENAME to SelQuestionSetResponse_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetResponse (  " +
                            "selQuestionSetResponseSelQuestionSetUid  INTEGER NOT NULL , " +
                            "selQuestionSetResponseClazzMemberUid  INTEGER NOT NULL , " +
                            "selQuestionSetResponseStartTime  INTEGER NOT NULL , " +
                            "selQuestionSetResponseFinishTime  INTEGER NOT NULL , " +
                            "selQuestionSetResponseRecognitionPercentage  REAL NOT NULL, " +
                            "selQuestionSetResponseMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "selQuestionSetResponseLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "selQuestionSetResponseLastChangedBy  INTEGER NOT NULL, " +
                            "selQuestionSetResposeUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSetResponse (selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy) SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse_OLD")
                    database.execSQL("DROP TABLE SelQuestionSetResponse_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetResponse_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSetResponse_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSetResponse_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSetResponse for SQLite

                    //Begin: Create table Schedule for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE Schedule RENAME to Schedule_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS Schedule (  sceduleStartTime  INTEGER NOT NULL , scheduleEndTime  INTEGER NOT NULL , scheduleDay  INTEGER NOT NULL, scheduleMonth  INTEGER NOT NULL, scheduleFrequency  INTEGER NOT NULL, umCalendarUid  INTEGER NOT NULL , scheduleClazzUid  INTEGER NOT NULL , scheduleMasterChangeSeqNum  INTEGER NOT NULL , scheduleLocalChangeSeqNum  INTEGER NOT NULL , scheduleLastChangedBy  INTEGER NOT NULL, scheduleActive  INTEGER NOT NULL , scheduleUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO Schedule (scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive) SELECT scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive FROM Schedule_OLD")
                    database.execSQL("DROP TABLE Schedule_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS Schedule_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Schedule_trk_clientId_epk_rx_csn 
                    |ON Schedule_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table Schedule for SQLite

                    //Begin: Create table DateRange for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE DateRange RENAME to DateRange_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS DateRange (  dateRangeLocalChangeSeqNum  INTEGER NOT NULL , dateRangeMasterChangeSeqNum  INTEGER NOT NULL , dateRangLastChangedBy  INTEGER NOT NULL, dateRangeFromDate  INTEGER NOT NULL , dateRangeToDate  INTEGER NOT NULL , dateRangeUMCalendarUid  INTEGER NOT NULL , dateRangeName  TEXT , dateRangeDesc  TEXT , dateRangeActive  INTEGER NOT NULL , dateRangeUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO DateRange (dateRangeUid, dateRangeLocalChangeSeqNum, dateRangeMasterChangeSeqNum, dateRangLastChangedBy, dateRangeFromDate, dateRangeToDate, dateRangeUMCalendarUid, dateRangeName, dateRangeDesc, dateRangeActive) SELECT dateRangeUid, dateRangeLocalChangeSeqNum, dateRangeMasterChangeSeqNum, dateRangLastChangedBy, dateRangeFromDate, dateRangeToDate, dateRangeUMCalendarUid, dateRangeName, dateRangeDesc, dateRangeActive FROM DateRange_OLD")
                    database.execSQL("DROP TABLE DateRange_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS DateRange_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_DateRange_trk_clientId_epk_rx_csn 
                    |ON DateRange_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table DateRange for SQLite

                    //Begin: Create table UMCalendar for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE UMCalendar RENAME to UMCalendar_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS UMCalendar (  umCalendarName  TEXT , umCalendarCategory  INTEGER NOT NULL, umCalendarActive  INTEGER NOT NULL , isUmCalendarFlag  INTEGER NOT NULL , umCalendarMasterChangeSeqNum  INTEGER NOT NULL , umCalendarLocalChangeSeqNum  INTEGER NOT NULL , umCalendarLastChangedBy  INTEGER NOT NULL, umCalendarUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO UMCalendar (umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, isUmCalendarFlag, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy) SELECT umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, isUmCalendarFlag, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy FROM UMCalendar_OLD")
                    database.execSQL("DROP TABLE UMCalendar_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS UMCalendar_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_UMCalendar_trk_clientId_epk_rx_csn 
                    |ON UMCalendar_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table UMCalendar for SQLite

                    //Begin: Create table ClazzActivity for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzActivity RENAME to ClazzActivity_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivity (  clazzActivityClazzActivityChangeUid  INTEGER NOT NULL , isClazzActivityGoodFeedback  INTEGER NOT NULL , clazzActivityNotes  TEXT , clazzActivityLogDate  INTEGER NOT NULL , clazzActivityClazzUid  INTEGER NOT NULL , clazzActivityDone  INTEGER NOT NULL , clazzActivityQuantity  INTEGER NOT NULL , clazzActivityMasterChangeSeqNum  INTEGER NOT NULL , clazzActivityLocalChangeSeqNum  INTEGER NOT NULL , clazzActivityLastChangedBy  INTEGER NOT NULL, clazzActivityUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzActivity (clazzActivityUid, clazzActivityClazzActivityChangeUid, isClazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy) SELECT clazzActivityUid, clazzActivityClazzActivityChangeUid, isClazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivity_OLD")
                    database.execSQL("DROP TABLE ClazzActivity_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivity_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzActivity_trk_clientId_epk_rx_csn 
                    |ON ClazzActivity_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzActivity for SQLite

                    //Begin: Create table ClazzActivityChange for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzActivityChange RENAME to ClazzActivityChange_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS ClazzActivityChange( 
                        clazzActivityChangeTitle  TEXT , 
                        clazzActivityDesc  TEXT , 
                        clazzActivityUnitOfMeasure  INTEGER NOT NULL, 
                        isClazzActivityChangeActive  INTEGER NOT NULL, 
                        clazzActivityChangeLastChangedBy  INTEGER NOT NULL, 
                        clazzActivityChangeMasterChangeSeqNum  INTEGER NOT NULL, 
                        clazzActivityChangeLocalChangeSeqNum  INTEGER NOT NULL,
                        clazzActivityLastChangedBy  INTEGER NOT NULL, 
                        clazzActivityChangeUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzActivityChange (clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, isClazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy) SELECT clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, isClazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivityChange_OLD")
                    database.execSQL("DROP TABLE ClazzActivityChange_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivityChange_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzActivityChange_trk_clientId_epk_rx_csn 
                    |ON ClazzActivityChange_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzActivityChange for SQLite

                    //Begin: Create table SelQuestionOption for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionOption RENAME to SelQuestionOption_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionOption (  optionText  TEXT , selQuestionOptionQuestionUid  INTEGER NOT NULL , selQuestionOptionMasterChangeSeqNum  INTEGER NOT NULL , selQuestionOptionLocalChangeSeqNum  INTEGER NOT NULL , selQuestionOptionLastChangedBy  INTEGER NOT NULL, optionActive  INTEGER NOT NULL , selQuestionOptionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionOption (selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive) SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption_OLD")
                    database.execSQL("DROP TABLE SelQuestionOption_OLD")
                    END MIGRATION*/
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
                    database.execSQL("""CREATE TABLE IF NOT EXISTS SelQuestionOption_trk (  
                        epk  INTEGER NOT NULL , 
                        clientId  INTEGER NOT NULL, 
                        csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , 
                        reqId  INTEGER NOT NULL, 
                        ts  INTEGER NOT NULL , 
                        pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionOption_trk_clientId_epk_rx_csn 
                    |ON SelQuestionOption_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionOption for SQLite

                    //Begin: Create table ScheduledCheck for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ScheduledCheck RENAME to ScheduledCheck_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS ScheduledCheck (  
                        checkTime  INTEGER NOT NULL , 
                        checkType  INTEGER NOT NULL, 
                        checkUuid  TEXT , 
                        checkParameters  TEXT , 
                        scClazzLogUid  INTEGER NOT NULL , 
                        scheduledCheckMasterCsn  INTEGER NOT NULL , 
                        scheduledCheckLocalCsn  INTEGER NOT NULL , 
                        scheduledCheckLastChangedBy  INTEGER NOT NULL, 
                        scheduledCheckUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ScheduledCheck (scheduledCheckUid, checkTime, checkType, checkUuid, checkParameters, scClazzLogUid, scheduledCheckMasterCsn, scheduledCheckLocalCsn, scheduledCheckLastChangedBy) SELECT scheduledCheckUid, checkTime, checkType, checkUuid, checkParameters, scClazzLogUid, scheduledCheckMasterCsn, scheduledCheckLocalCsn, scheduledCheckLastChangedBy FROM ScheduledCheck_OLD")
                    database.execSQL("DROP TABLE ScheduledCheck_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScheduledCheck_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ScheduledCheck_trk_clientId_epk_rx_csn 
                    |ON ScheduledCheck_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ScheduledCheck for SQLite

                    //Begin: Create table AuditLog for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE AuditLog RENAME to AuditLog_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS AuditLog (  
                        auditLogMasterChangeSeqNum  INTEGER NOT NULL , 
                        auditLogLocalChangeSeqNum  INTEGER NOT NULL , 
                        auditLogLastChangedBy  INTEGER NOT NULL,
                        auditLogActorPersonUid  INTEGER NOT NULL , 
                        auditLogTableUid  INTEGER NOT NULL, 
                        auditLogEntityUid  INTEGER NOT NULL , 
                        auditLogDate  INTEGER NOT NULL , 
                        notes  TEXT , 
                        auditLogUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL 
                        )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO AuditLog (auditLogUid, auditLogMasterChangeSeqNum, auditLogLocalChangeSeqNum, auditLogLastChangedBy, auditLogActorPersonUid, auditLogTableUid, auditLogEntityUid, auditLogDate, notes) SELECT auditLogUid, auditLogMasterChangeSeqNum, auditLogLocalChangeSeqNum, auditLogLastChangedBy, auditLogActorPersonUid, auditLogTableUid, auditLogEntityUid, auditLogDate, notes FROM AuditLog_OLD")
                    database.execSQL("DROP TABLE AuditLog_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS AuditLog_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_AuditLog_trk_clientId_epk_rx_csn 
                    |ON AuditLog_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table AuditLog for SQLite

                    //Begin: Create table CustomField for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomField RENAME to CustomField_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomField (  customFieldName  TEXT , customFieldNameAlt  TEXT , customFieldLabelMessageID  INTEGER NOT NULL, customFieldIcon  TEXT , customFieldType  INTEGER NOT NULL, customFieldEntityType  INTEGER NOT NULL, customFieldActive  INTEGER NOT NULL , customFieldDefaultValue  TEXT , customFieldMCSN  INTEGER NOT NULL , customFieldLCSN  INTEGER NOT NULL , customFieldLCB  INTEGER NOT NULL, customFieldUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomField (customFieldUid, customFieldName, customFieldNameAlt, customFieldLabelMessageID, customFieldIcon, customFieldType, customFieldEntityType, customFieldActive, customFieldDefaultValue, customFieldMCSN, customFieldLCSN, customFieldLCB) SELECT customFieldUid, customFieldName, customFieldNameAlt, customFieldLabelMessageID, customFieldIcon, customFieldType, customFieldEntityType, customFieldActive, customFieldDefaultValue, customFieldMCSN, customFieldLCSN, customFieldLCB FROM CustomField_OLD")
                    database.execSQL("DROP TABLE CustomField_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomField_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomField_trk_clientId_epk_rx_csn 
                    |ON CustomField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomField for SQLite

                    //Begin: Create table CustomFieldValue for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomFieldValue RENAME to CustomFieldValue_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValue (  customFieldValueFieldUid  INTEGER NOT NULL , customFieldValueEntityUid  INTEGER NOT NULL , customFieldValueValue  TEXT , customFieldValueMCSN  INTEGER NOT NULL , customFieldValueLCSN  INTEGER NOT NULL , customFieldValueLCB  INTEGER NOT NULL, customFieldValueUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomFieldValue (customFieldValueUid, customFieldValueFieldUid, customFieldValueEntityUid, customFieldValueValue, customFieldValueMCSN, customFieldValueLCSN, customFieldValueLCB) SELECT customFieldValueUid, customFieldValueFieldUid, customFieldValueEntityUid, customFieldValueValue, customFieldValueMCSN, customFieldValueLCSN, customFieldValueLCB FROM CustomFieldValue_OLD")
                    database.execSQL("DROP TABLE CustomFieldValue_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValue_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomFieldValue_trk_clientId_epk_rx_csn 
                    |ON CustomFieldValue_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomFieldValue for SQLite

                    //Begin: Create table CustomFieldValueOption for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomFieldValueOption RENAME to CustomFieldValueOption_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValueOption (  customFieldValueOptionName  TEXT , customFieldValueOptionFieldUid  INTEGER NOT NULL , customFieldValueOptionIcon  TEXT , customFieldValueOptionMessageId  INTEGER NOT NULL, customFieldValueOptionActive  INTEGER NOT NULL , customFieldValueOptionMCSN  INTEGER NOT NULL , customFieldValueOptionLCSN  INTEGER NOT NULL , customFieldValueOptionLCB  INTEGER NOT NULL, customFieldValueOptionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomFieldValueOption (customFieldValueOptionUid, customFieldValueOptionName, customFieldValueOptionFieldUid, customFieldValueOptionIcon, customFieldValueOptionMessageId, customFieldValueOptionActive, customFieldValueOptionMCSN, customFieldValueOptionLCSN, customFieldValueOptionLCB) SELECT customFieldValueOptionUid, customFieldValueOptionName, customFieldValueOptionFieldUid, customFieldValueOptionIcon, customFieldValueOptionMessageId, customFieldValueOptionActive, customFieldValueOptionMCSN, customFieldValueOptionLCSN, customFieldValueOptionLCB FROM CustomFieldValueOption_OLD")
                    database.execSQL("DROP TABLE CustomFieldValueOption_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValueOption_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomFieldValueOption_trk_clientId_epk_rx_csn 
                    |ON CustomFieldValueOption_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomFieldValueOption for SQLite


                    //Begin: Create table School for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE School RENAME to School_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS School (  " +
                            "schoolName  TEXT , schoolDesc  TEXT , schoolAddress  TEXT , " +
                            "schoolActive  INTEGER NOT NULL , schoolFeatures  INTEGER NOT NULL , " +
                            "schoolLocationLong  REAL NOT NULL , schoolLocationLatt  REAL NOT NULL , " +
                            "schoolMasterChangeSeqNum  INTEGER NOT NULL , " +
                            "schoolLocalChangeSeqNum  INTEGER NOT NULL , " +
                            "schoolLastChangedBy  INTEGER NOT NULL, " +
                            "schoolUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO School (schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy) SELECT schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy FROM School_OLD")
                    database.execSQL("DROP TABLE School_OLD")
                    END MIGRATION*/
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
                    database.execSQL("CREATE TABLE IF NOT EXISTS School_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_School_trk_clientId_epk_rx_csn 
                    |ON School_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table School for SQLite

                    //Begin: Create table ClazzAssignment for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzAssignment RENAME to ClazzAssignment_OLD")
                    END MIGRATION */
                    database.execSQL("""CREATE TABLE IF NOT EXISTS ClazzAssignment (  
                        clazzAssignmentTitle  TEXT , 
                        clazzAssignmentClazzUid  INTEGER NOT NULL , 
                        clazzAssignmentInactive  INTEGER NOT NULL , 
                        clazzAssignmentStartDate  INTEGER NOT NULL , 
                        clazzAssignmentDueDate  INTEGER NOT NULL , 
                        clazzAssignmentCreationDate  INTEGER NOT NULL ,
                         clazzAssignmentUpdateDate  INTEGER NOT NULL , 
                         clazzAssignmentInstructions  TEXT , 
                         clazzAssignmentGrading  INTEGER NOT NULL,
                          clazzAssignmentRequireAttachment  INTEGER NOT NULL , 
                          clazzAssignmentMCSN  INTEGER NOT NULL , 
                          clazzAssignmentLCSN  INTEGER NOT NULL , 
                          clazzAssignmentLCB  INTEGER NOT NULL, 
                          clazzAssignmentUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )""")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzAssignment (clazzAssignmentUid, clazzAssignmentTitle, clazzAssignmentClazzUid, clazzAssignmentInactive, clazzAssignmentStartDate, clazzAssignmentDueDate, clazzAssignmentCreationDate, clazzAssignmentUpdateDate, clazzAssignmentInstructions, clazzAssignmentGrading, clazzAssignmentRequireAttachment, clazzAssignmentMCSN, clazzAssignmentLCSN, clazzAssignmentLCB) SELECT clazzAssignmentUid, clazzAssignmentTitle, clazzAssignmentClazzUid, clazzAssignmentInactive, clazzAssignmentStartDate, clazzAssignmentDueDate, clazzAssignmentCreationDate, clazzAssignmentUpdateDate, clazzAssignmentInstructions, clazzAssignmentGrading, clazzAssignmentRequireAttachment, clazzAssignmentMCSN, clazzAssignmentLCSN, clazzAssignmentLCB FROM ClazzAssignment_OLD")
                    database.execSQL("DROP TABLE ClazzAssignment_OLD")
                    END MIGRATION*/
                    database.execSQL("""
                    |CREATE TRIGGER UPD_176
                    |AFTER UPDATE ON ClazzAssignment FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.clazzAssignmentMCSN = 0 
                    |OR OLD.clazzAssignmentMCSN = NEW.clazzAssignmentMCSN
                    |)
                    |ELSE
                    |(NEW.clazzAssignmentLCSN = 0  
                    |OR OLD.clazzAssignmentLCSN = NEW.clazzAssignmentLCSN
                    |) END)
                    |BEGIN 
                    |UPDATE ClazzAssignment SET clazzAssignmentLCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentLCSN 
                    |ELSE (SELECT MAX(MAX(clazzAssignmentLCSN), OLD.clazzAssignmentLCSN) + 1 FROM ClazzAssignment) END),
                    |clazzAssignmentMCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(MAX(clazzAssignmentMCSN), OLD.clazzAssignmentMCSN) + 1 FROM ClazzAssignment)
                    |ELSE NEW.clazzAssignmentMCSN END)
                    |WHERE clazzAssignmentUid = NEW.clazzAssignmentUid
                    |; END
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER INS_176
                    |AFTER INSERT ON ClazzAssignment FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.clazzAssignmentMCSN = 0 
                    |
                    |)
                    |ELSE
                    |(NEW.clazzAssignmentLCSN = 0  
                    |
                    |) END)
                    |BEGIN 
                    |UPDATE ClazzAssignment SET clazzAssignmentLCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentLCSN 
                    |ELSE (SELECT MAX(clazzAssignmentLCSN) + 1 FROM ClazzAssignment) END),
                    |clazzAssignmentMCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(clazzAssignmentMCSN) + 1 FROM ClazzAssignment)
                    |ELSE NEW.clazzAssignmentMCSN END)
                    |WHERE clazzAssignmentUid = NEW.clazzAssignmentUid
                    |; END
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment_trk (  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzAssignment_trk_clientId_epk_rx_csn 
                    |ON ClazzAssignment_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzAssignment for SQLite

                    //Begin: Create table ClazzAssignmentContentJoin for SQLite
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzAssignmentContentJoin RENAME to ClazzAssignmentContentJoin_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (  " +
                            "clazzAssignmentContentJoinContentUid  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinClazzAssignmentUid  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinInactive  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinDateAdded  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinMCSN  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinLCSN  INTEGER NOT NULL , " +
                            "clazzAssignmentContentJoinLCB  INTEGER NOT NULL, " +
                            "clazzAssignmentContentJoinUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzAssignmentContentJoin (clazzAssignmentContentJoinUid, clazzAssignmentContentJoinContentUid, clazzAssignmentContentJoinClazzAssignmentUid, clazzAssignmentContentJoinInactive, clazzAssignmentContentJoinDateAdded, clazzAssignmentContentJoinMCSN, clazzAssignmentContentJoinLCSN, clazzAssignmentContentJoinLCB) SELECT clazzAssignmentContentJoinUid, clazzAssignmentContentJoinContentUid, clazzAssignmentContentJoinClazzAssignmentUid, clazzAssignmentContentJoinInactive, clazzAssignmentContentJoinDateAdded, clazzAssignmentContentJoinMCSN, clazzAssignmentContentJoinLCSN, clazzAssignmentContentJoinLCB FROM ClazzAssignmentContentJoin_OLD")
                    database.execSQL("DROP TABLE ClazzAssignmentContentJoin_OLD")
                    END MIGRATION*/
                    database.execSQL("""
                    |CREATE TRIGGER UPD_177
                    |AFTER UPDATE ON ClazzAssignmentContentJoin FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.clazzAssignmentContentJoinMCSN = 0 
                    |OR OLD.clazzAssignmentContentJoinMCSN = NEW.clazzAssignmentContentJoinMCSN
                    |)
                    |ELSE
                    |(NEW.clazzAssignmentContentJoinLCSN = 0  
                    |OR OLD.clazzAssignmentContentJoinLCSN = NEW.clazzAssignmentContentJoinLCSN
                    |) END)
                    |BEGIN 
                    |UPDATE ClazzAssignmentContentJoin SET clazzAssignmentContentJoinLCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentContentJoinLCSN 
                    |ELSE (SELECT MAX(MAX(clazzAssignmentContentJoinLCSN), OLD.clazzAssignmentContentJoinLCSN) + 1 FROM ClazzAssignmentContentJoin) END),
                    |clazzAssignmentContentJoinMCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(MAX(clazzAssignmentContentJoinMCSN), OLD.clazzAssignmentContentJoinMCSN) + 1 FROM ClazzAssignmentContentJoin)
                    |ELSE NEW.clazzAssignmentContentJoinMCSN END)
                    |WHERE clazzAssignmentContentJoinUid = NEW.clazzAssignmentContentJoinUid
                    |; END
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER INS_177
                    |AFTER INSERT ON ClazzAssignmentContentJoin FOR EACH ROW WHEN
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(NEW.clazzAssignmentContentJoinMCSN = 0 
                    |
                    |)
                    |ELSE
                    |(NEW.clazzAssignmentContentJoinLCSN = 0  
                    |
                    |) END)
                    |BEGIN 
                    |UPDATE ClazzAssignmentContentJoin SET clazzAssignmentContentJoinLCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentContentJoinLCSN 
                    |ELSE (SELECT MAX(clazzAssignmentContentJoinLCSN) + 1 FROM ClazzAssignmentContentJoin) END),
                    |clazzAssignmentContentJoinMCSN = 
                    |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                    |(SELECT MAX(clazzAssignmentContentJoinMCSN) + 1 FROM ClazzAssignmentContentJoin)
                    |ELSE NEW.clazzAssignmentContentJoinMCSN END)
                    |WHERE clazzAssignmentContentJoinUid = NEW.clazzAssignmentContentJoinUid
                    |; END
                    """.trimMargin())
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin_trk (" +
                            "  epk  INTEGER NOT NULL , clientId  INTEGER NOT NULL, csn  INTEGER NOT NULL, " +
                            " rx  INTEGER NOT NULL , reqId  INTEGER NOT NULL, ts  INTEGER NOT NULL , " +
                            " pk  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzAssignmentContentJoin_trk_clientId_epk_rx_csn 
                    |ON ClazzAssignmentContentJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzAssignmentContentJoin for SQLite




                }
                else if(database.dbType() == DoorDbType.POSTGRES){

                    //Drop PersonCustomField ( cause its not PersonField)
                    database.execSQL("DROP TABLE PersonCustomField")

                    //PersonGroupMember migration
                    //Begin: Create table PersonGroupMember for PostgreSQL
                    database.execSQL("ALTER TABLE PersonGroupMember RENAME to PersonGroupMember_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS persongroupmember_groupmemberuid_seq RENAME to persongroupmember_groupmemberuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroupMember (  groupMemberActive  BOOL , groupMemberPersonUid  BIGINT , groupMemberGroupUid  BIGINT , groupMemberMasterCsn  BIGINT , groupMemberLocalCsn  BIGINT , groupMemberLastChangedBy  INTEGER , groupMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonGroupMember (" +
                            "groupMemberUid, groupMemberActive, groupMemberPersonUid, " +
                            "groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, " +
                            "groupMemberLastChangedBy) SELECT groupMemberUid, true, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy FROM PersonGroupMember_OLD")
                    database.execSQL("DROP TABLE PersonGroupMember_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS persongroupmember_groupmemberuid_seq_old""")

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroupMember_groupMemberGroupUid 
                    |ON PersonGroupMember (groupMemberGroupUid)
                    """.trimMargin())

                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonGroupMember_groupMemberPersonUid 
                    |ON PersonGroupMember (groupMemberPersonUid)
                    """.trimMargin())

                    //StatementEntity
                    //Begin: Create table StatementEntity for PostgreSQL
                    database.execSQL("ALTER TABLE StatementEntity RENAME to StatementEntity_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS statemententity_statementuid_seq RENAME to statemententity_statementuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS StatementEntity (  statementId  TEXT , personUid  BIGINT , verbUid  BIGINT , xObjectUid  BIGINT , subStatementActorUid  BIGINT , substatementVerbUid  BIGINT , subStatementObjectUid  BIGINT , agentUid  BIGINT , instructorUid  BIGINT , authorityUid  BIGINT , teamUid  BIGINT , resultCompletion  BOOL , resultSuccess  SMALLINT , resultScoreScaled  BIGINT , resultScoreRaw  BIGINT , resultScoreMin  BIGINT , resultScoreMax  BIGINT , resultDuration  BIGINT , resultResponse  TEXT , timestamp  BIGINT , stored  BIGINT , contextRegistration  TEXT , contextPlatform  TEXT , contextStatementId  TEXT , fullStatement  TEXT , statementMasterChangeSeqNum  BIGINT , statementLocalChangeSeqNum  BIGINT , statementLastChangedBy  INTEGER , extensionProgress  INTEGER , statementContentEntryUid  BIGINT , statementUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""INSERT INTO StatementEntity (
                        |statementUid, statementId, personUid, verbUid, xObjectUid, 
                        |subStatementActorUid, substatementVerbUid, subStatementObjectUid, 
                        |agentUid, instructorUid, authorityUid, teamUid, resultCompletion, 
                        |resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, 
                        |resultScoreMax, resultDuration, resultResponse, timestamp, stored, 
                        |contextRegistration, contextPlatform, contextStatementId, 
                        |fullStatement, statementMasterChangeSeqNum, 
                        |statementLocalChangeSeqNum, statementLastChangedBy, 
                        |extensionProgress, statementContentEntryUid) 
                        |SELECT statementUid, statementId, personUid, verbUid, xObjectUid, 
                        |subStatementActorUid, substatementVerbUid, subStatementObjectUid, 
                        |agentUid, instructorUid, authorityUid, teamUid, resultCompletion, 
                        |resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, 
                        |resultScoreMax, resultDuration, resultResponse, timestamp, stored, 
                        |contextRegistration, contextPlatform, contextStatementId, 
                        |fullStatement, statementMasterChangeSeqNum, 
                        |statementLocalChangeSeqNum, statementLastChangedBy, 
                        |0, 0 FROM StatementEntity_OLD""".trimMargin())
                    database.execSQL("DROP TABLE StatementEntity_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS persongroupmember_groupmemberuid_seq_old""")

                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonCustomFieldValue_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonCustomFieldValue_trk_clientId_epk_rx_csn 
                    |ON PersonCustomFieldValue_trk (clientId, epk, rx, csn)
                    """.trimMargin())

                    //Begin: Create table Location for PostgreSQL
                    database.execSQL("ALTER TABLE Location RENAME to Location_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS location_locationuid_seq RENAME to location_locationuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Location (  title  TEXT , description  TEXT , lng  TEXT , lat  TEXT , parentLocationUid  BIGINT , locationLocalChangeSeqNum  BIGINT , locationMasterChangeSeqNum  BIGINT , locationLastChangedBy  INTEGER , timeZone  TEXT , locationActive  BOOL , locationUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO Location (locationUid, title, description, " +
                            "lng, lat, parentLocationUid, locationLocalChangeSeqNum, locationMasterChangeSeqNum, " +
                            "locationLastChangedBy, timeZone, locationActive) " +
                            "SELECT locationUid, title, description, lng, lat, parentLocationUid, " +
                            "locationLocalChangeSeqNum, locationMasterChangeSeqNum, " +
                            "locationLastChangedBy, '', locationActive FROM Location_OLD")
                    database.execSQL("DROP TABLE Location_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS location_locationuid_seq_old""")

                    //EntityRole migration
                    database.execSQL("ALTER TABLE EntityRole RENAME to EntityRole_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS entityrole_eruid_seq RENAME to entityrole_eruid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS EntityRole (  erMasterCsn  BIGINT , erLocalCsn  BIGINT , erLastChangedBy  INTEGER , erTableId  INTEGER , erEntityUid  BIGINT , erGroupUid  BIGINT , erRoleUid  BIGINT , erActive  BOOL , erUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""INSERT INTO EntityRole (erUid, erMasterCsn, 
                        erLocalCsn, erLastChangedBy, erTableId, erEntityUid, erGroupUid, 
                        erRoleUid, erActive) 
                        SELECT erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, 
                        erEntityUid, erGroupUid, erRoleUid, true FROM EntityRole_OLD""")
                    database.execSQL("DROP TABLE EntityRole_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS entityrole_eruid_seq_old""")


                    //Begin: Create table PersonGroup for PostgreSQL
                    database.execSQL("ALTER TABLE PersonGroup RENAME to PersonGroup_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS persongroup_groupuid_seq RENAME to persongroup_groupuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonGroup (  groupMasterCsn  BIGINT , groupLocalCsn  BIGINT , groupLastChangedBy  INTEGER , groupName  TEXT , groupActive  BOOL , groupPersonUid  BIGINT , groupUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""INSERT INTO PersonGroup (
                         groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, 
                         groupName, groupActive, groupPersonUid) SELECT groupUid, 
                         groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, 
                         true, groupPersonUid FROM PersonGroup_OLD""")
                    database.execSQL("DROP TABLE PersonGroup_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS persongroup_groupuid_seq_old""")

                    //Begin: Create table Person for PostgreSQL
                    database.execSQL("ALTER TABLE Person RENAME to Person_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS person_personuid_seq RENAME to person_personuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Person (  username  TEXT , firstNames  TEXT , lastName  TEXT , emailAddr  TEXT , phoneNum  TEXT , gender  INTEGER , active  BOOL , admin  BOOL , personNotes  TEXT , fatherName  TEXT , fatherNumber  TEXT , motherName  TEXT , motherNum  TEXT , dateOfBirth  BIGINT , personAddress  TEXT , personMasterChangeSeqNum  BIGINT , personLocalChangeSeqNum  BIGINT , personLastChangedBy  INTEGER , personUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""INSERT INTO Person (personUid, username, firstNames, 
                        lastName, emailAddr, phoneNum, gender, active, admin, personNotes, 
                        fatherName, fatherNumber, motherName, motherNum, dateOfBirth, 
                        personAddress, personMasterChangeSeqNum, personLocalChangeSeqNum, 
                        personLastChangedBy)
                         SELECT personUid, username, firstNames, lastName, emailAddr, 
                         phoneNum, gender, active, admin, '', '', 
                         '', '', '', 0, '', 
                         personMasterChangeSeqNum, personLocalChangeSeqNum, 
                         personLastChangedBy FROM Person_OLD""")
                    database.execSQL("DROP TABLE Person_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS person_personuid_seq_old""")


                    //Begin: Create table PersonCustomFieldValue for PostgreSQL
                    database.execSQL("ALTER TABLE PersonCustomFieldValue RENAME to PersonCustomFieldValue_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonCustomFieldValue (  personCustomFieldValuePersonCustomFieldUid  BIGINT , personCustomFieldValuePersonUid  BIGINT , fieldValue  TEXT , personCustomFieldValueMasterChangeSeqNum  BIGINT , personCustomFieldValueLocalChangeSeqNum  BIGINT , personCustomFieldValueLastChangedBy  INTEGER , personCustomFieldValueUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""INSERT INTO PersonCustomFieldValue (
                        personCustomFieldValueUid, personCustomFieldValuePersonCustomFieldUid,
                         personCustomFieldValuePersonUid, fieldValue,
                          personCustomFieldValueMasterChangeSeqNum, 
                          personCustomFieldValueLocalChangeSeqNum, 
                          personCustomFieldValueLastChangedBy) 
                          SELECT personCustomFieldValueUid, 
                          personCustomFieldValuePersonCustomFieldUid,
                           personCustomFieldValuePersonUid, fieldValue, 
                           0, 
                           0, 
                           0 FROM PersonCustomFieldValue_OLD""")
                    database.execSQL("DROP TABLE PersonCustomFieldValue_OLD")

                    //Begin: clazz
                    //Begin: Create table Clazz for PostgreSQL
                    database.execSQL("ALTER TABLE Clazz RENAME to Clazz_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS clazz_clazzuid_seq RENAME to clazz_clazzuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS Clazz (  clazzName  TEXT , clazzDesc  TEXT , attendanceAverage  FLOAT , clazzHolidayUMCalendarUid  BIGINT , clazzScheuleUMCalendarUid  BIGINT , isClazzActive  BOOL , clazzLocationUid  BIGINT , clazzStartTime  BIGINT , clazzEndTime  BIGINT , clazzFeatures  BIGINT , clazzMasterChangeSeqNum  BIGINT , clazzLocalChangeSeqNum  BIGINT , clazzLastChangedBy  INTEGER , clazzUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("DROP TABLE Clazz_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS clazz_clazzuid_seq_old""")

                    //Begin : PersonAuth stuff
                    //Begin: Create table PersonAuth for PostgreSQL
                    database.execSQL("ALTER TABLE PersonAuth RENAME to PersonAuth_OLD")
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonAuth (  passwordHash  TEXT , personAuthStatus  INTEGER , personAuthLocalChangeSeqNum  BIGINT , personAuthMasterChangeSeqNum  BIGINT , lastChangedBy  INTEGER , personAuthUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("INSERT INTO PersonAuth (personAuthUid, passwordHash, " +
                            "personAuthStatus, personAuthLocalChangeSeqNum, " +
                            "personAuthMasterChangeSeqNum, lastChangedBy)" +
                            " SELECT personAuthUid, passwordHash, 0, " +
                            "0, 0," +
                            " 0 FROM PersonAuth_OLD")
                    database.execSQL("DROP TABLE PersonAuth_OLD")
                    //End: Create table PersonAuth for PostgreSQL

                    //Begin: Create table ClazzMember for PostgreSQL
                    database.execSQL("ALTER TABLE ClazzMember RENAME to ClazzMember_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS clazzmember_clazzmemberuid_seq RENAME to clazzmember_clazzmemberuid_seq_old;")
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzMember (  clazzMemberPersonUid  BIGINT , clazzMemberClazzUid  BIGINT , clazzMemberDateJoined  BIGINT , clazzMemberDateLeft  BIGINT , clazzMemberRole  INTEGER , clazzMemberAttendancePercentage  FLOAT , clazzMemberActive  BOOL , clazzMemberLocalChangeSeqNum  BIGINT , clazzMemberMasterChangeSeqNum  BIGINT , clazzMemberLastChangedBy  INTEGER , clazzMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("DROP TABLE ClazzMember_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS clazzmember_clazzmemberuid_seq_old""")



                    database.execSQL("ALTER TABLE Role RENAME to Role_OLD")
                    database.execSQL("ALTER SEQUENCE IF EXISTS role_roleuid_seq RENAME to role_roleuid_seq_old;")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS Role (
                        roleName  TEXT , roleActive  BOOL , roleMasterCsn  BIGINT , 
                        roleLocalCsn  BIGINT , roleLastChangedBy  INTEGER , 
                        rolePermissions  BIGINT , roleUid  BIGSERIAL  PRIMARY KEY  NOT NULL )""")
                    database.execSQL("""INSERT INTO Role (
                        roleUid, roleName, roleActive, roleMasterCsn, 
                        roleLocalCsn, roleLastChangedBy, rolePermissions
                        ) 
                        SELECT roleUid, roleName, true, roleMasterCsn, 
                        roleLocalCsn, roleLastChangedBy, rolePermissions 
                        FROM Role_OLD""")
                    database.execSQL("DROP TABLE Role_OLD")
                    database.execSQL("""DROP SEQUENCE IF EXISTS role_roleuid_seq_old""")

                    //Begin: Create table ClazzLog for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzLog RENAME to ClazzLog_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog (  clazzLogClazzUid  BIGINT , logDate  BIGINT , timeRecorded  BIGINT , clazzLogDone  BOOL , clazzLogCancelled  BOOL , clazzLogNumPresent  INTEGER , clazzLogNumAbsent  INTEGER , clazzLogNumPartial  INTEGER , clazzLogScheduleUid  BIGINT , clazzLogMSQN  BIGINT , clazzLogLCSN  BIGINT , clazzLogLCB  INTEGER , clazzLogUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzLog (clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogMSQN, clazzLogLCSN, clazzLogLCB) SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogMSQN, clazzLogLCSN, clazzLogLCB FROM ClazzLog_OLD")
                    database.execSQL("DROP TABLE ClazzLog_OLD")
                    END MIGRATION*/
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
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_14_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_14")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLog_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER NOT NULL, rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzLog_trk_clientId_epk_rx_csn 
                    |ON ClazzLog_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzLog for PostgreSQL

                    //Begin: Create table ClazzLogAttendanceRecord for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzLogAttendanceRecord RENAME to ClazzLogAttendanceRecord_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord (  clazzLogAttendanceRecordClazzLogUid  BIGINT , clazzLogAttendanceRecordClazzMemberUid  BIGINT , attendanceStatus  INTEGER , clazzLogAttendanceRecordMasterChangeSeqNum  BIGINT , clazzLogAttendanceRecordLocalChangeSeqNum  BIGINT , clazzLogAttendanceRecordLastChangedBy  INTEGER , clazzLogAttendanceRecordUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzLogAttendanceRecord (clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy) SELECT clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy FROM ClazzLogAttendanceRecord_OLD")
                    database.execSQL("DROP TABLE ClazzLogAttendanceRecord_OLD")
                    END MIGRATION*/
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
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_15_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_15")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecord_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn 
                    |ON ClazzLogAttendanceRecord_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzLogAttendanceRecord for PostgreSQL

                    //Begin: Create table FeedEntry for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE FeedEntry RENAME to FeedEntry_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS FeedEntry (  feedEntryPersonUid  BIGINT , title  TEXT , description  TEXT , link  TEXT , feedEntryClazzName  TEXT , deadline  BIGINT , feedEntryHash  BIGINT , feedEntryDone  BOOL , feedEntryClazzLogUid  BIGINT , dateCreated  BIGINT , feedEntryCheckType  INTEGER , feedEntryLocalChangeSeqNum  BIGINT , feedEntryMasterChangeSeqNum  BIGINT , feedEntryLastChangedBy  INTEGER , feedEntryUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO FeedEntry (feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy) SELECT feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy FROM FeedEntry_OLD")
                    database.execSQL("DROP TABLE FeedEntry_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS FeedEntry_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS FeedEntry_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_121_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE FeedEntry SET feedEntryLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.feedEntryLocalChangeSeqNum 
                    | ELSE NEXTVAL('FeedEntry_lcsn_seq') END),
                    | feedEntryMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('FeedEntry_mcsn_seq') 
                    | ELSE NEW.feedEntryMasterChangeSeqNum END)
                    | WHERE feedEntryUid = NEW.feedEntryUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_121_trig 
                    |AFTER UPDATE OR INSERT ON FeedEntry 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_121_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_121_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_121")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS FeedEntry_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_FeedEntry_trk_clientId_epk_rx_csn 
                    |ON FeedEntry_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table FeedEntry for PostgreSQL

                    //Begin: Create table PersonField for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE PersonField RENAME to PersonField_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonField (  fieldName  TEXT , labelMessageId  INTEGER , fieldIcon  TEXT , personFieldMasterChangeSeqNum  BIGINT , personFieldLocalChangeSeqNum  BIGINT , personFieldLastChangedBy  INTEGER , personCustomFieldUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO PersonField (personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy) SELECT personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy FROM PersonField_OLD")
                    database.execSQL("DROP TABLE PersonField_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonField_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonField_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_20_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonField SET personFieldLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personFieldLocalChangeSeqNum 
                    | ELSE NEXTVAL('PersonField_lcsn_seq') END),
                    | personFieldMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonField_mcsn_seq') 
                    | ELSE NEW.personFieldMasterChangeSeqNum END)
                    | WHERE personCustomFieldUid = NEW.personCustomFieldUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_20_trig 
                    |AFTER UPDATE OR INSERT ON PersonField 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_20_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_20_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_20")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonField_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonField_trk_clientId_epk_rx_csn 
                    |ON PersonField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table PersonField for PostgreSQL

                    //Begin: Create table PersonDetailPresenterField for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE PersonDetailPresenterField RENAME to PersonDetailPresenterField_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonDetailPresenterField (  fieldUid  BIGINT , fieldType  INTEGER , fieldIndex  INTEGER , labelMessageId  INTEGER , fieldIcon  TEXT , headerMessageId  INTEGER , viewModeVisible  BOOL , editModeVisible  BOOL , isReadyOnly  BOOL , personDetailPresenterFieldMasterChangeSeqNum  BIGINT , personDetailPresenterFieldLocalChangeSeqNum  BIGINT , personDetailPresenterFieldLastChangedBy  INTEGER , personDetailPresenterFieldUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO PersonDetailPresenterField (personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, isReadyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy) SELECT personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, isReadyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy FROM PersonDetailPresenterField_OLD")
                    database.execSQL("DROP TABLE PersonDetailPresenterField_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonDetailPresenterField_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS PersonDetailPresenterField_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_19_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE PersonDetailPresenterField SET personDetailPresenterFieldLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum 
                    | ELSE NEXTVAL('PersonDetailPresenterField_lcsn_seq') END),
                    | personDetailPresenterFieldMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('PersonDetailPresenterField_mcsn_seq') 
                    | ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END)
                    | WHERE personDetailPresenterFieldUid = NEW.personDetailPresenterFieldUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_19_trig 
                    |AFTER UPDATE OR INSERT ON PersonDetailPresenterField 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_19_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_19_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_19")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS PersonDetailPresenterField_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_PersonDetailPresenterField_trk_clientId_epk_rx_csn 
                    |ON PersonDetailPresenterField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table PersonDetailPresenterField for PostgreSQL

                    //Begin: Create table SelQuestion for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestion RENAME to SelQuestion_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestion (  questionText  TEXT , selQuestionSelQuestionSetUid  BIGINT , questionIndex  INTEGER , assignToAllClasses  BOOL , multiNominations  BOOL , questionType  INTEGER , questionActive  BOOL , selQuestionMasterChangeSeqNum  BIGINT , selQuestionLocalChangeSeqNum  BIGINT , selQuestionLastChangedBy  INTEGER , selQuestionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestion (selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy) SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion_OLD")
                    database.execSQL("DROP TABLE SelQuestion_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestion_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestion_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_22_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestion SET selQuestionLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestion_lcsn_seq') END),
                    | selQuestionMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestion_mcsn_seq') 
                    | ELSE NEW.selQuestionMasterChangeSeqNum END)
                    | WHERE selQuestionUid = NEW.selQuestionUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_22_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestion 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_22_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_22_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_22")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestion_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestion_trk_clientId_epk_rx_csn 
                    |ON SelQuestion_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestion for PostgreSQL

                    //Begin: Create table SelQuestionResponse for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionResponse RENAME to SelQuestionResponse_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponse (  selQuestionResponseSelQuestionSetResponseUid  BIGINT , selQuestionResponseSelQuestionUid  BIGINT , selQuestionResponseMasterChangeSeqNum  BIGINT , selQuestionResponseLocalChangeSeqNum  BIGINT , selQuestionResponseLastChangedBy  INTEGER , selQuestionResponseUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionResponse (selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy) SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse_OLD")
                    database.execSQL("DROP TABLE SelQuestionResponse_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionResponse_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionResponse_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_23_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionResponseLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestionResponse_lcsn_seq') END),
                    | selQuestionResponseMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionResponse_mcsn_seq') 
                    | ELSE NEW.selQuestionResponseMasterChangeSeqNum END)
                    | WHERE selQuestionResponseUid = NEW.selQuestionResponseUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_23_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionResponse 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_23_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_23_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_23")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponse_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionResponse_trk_clientId_epk_rx_csn 
                    |ON SelQuestionResponse_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionResponse for PostgreSQL

                    //Begin: Create table SelQuestionResponseNomination for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionResponseNomination RENAME to SelQuestionResponseNomination_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponseNomination (  selqrnClazzMemberUid  BIGINT , selqrnSelQuestionResponseUId  BIGINT , nominationActive  BOOL , selqrnMCSN  BIGINT , selqrnMCSNLCSN  BIGINT , selqrnMCSNLCB  INTEGER , selqrnUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionResponseNomination (selqrnUid, selqrnClazzMemberUid, selqrnSelQuestionResponseUId, nominationActive, selqrnMCSN, selqrnMCSNLCSN, selqrnMCSNLCB) SELECT selqrnUid, selqrnClazzMemberUid, selqrnSelQuestionResponseUId, nominationActive, selqrnMCSN, selqrnMCSNLCSN, selqrnMCSNLCB FROM SelQuestionResponseNomination_OLD")
                    database.execSQL("DROP TABLE SelQuestionResponseNomination_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionResponseNomination_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionResponseNomination_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_24_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionResponseNomination SET selqrnMCSNLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selqrnMCSNLCSN 
                    | ELSE NEXTVAL('SelQuestionResponseNomination_lcsn_seq') END),
                    | selqrnMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionResponseNomination_mcsn_seq') 
                    | ELSE NEW.selqrnMCSN END)
                    | WHERE selqrnUid = NEW.selqrnUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_24_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionResponseNomination 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_24_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_24_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_24")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionResponseNomination_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionResponseNomination_trk_clientId_epk_rx_csn 
                    |ON SelQuestionResponseNomination_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionResponseNomination for PostgreSQL

                    //Begin: Create table SelQuestionSet for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSet RENAME to SelQuestionSet_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSet (  title  TEXT , selQuestionSetMasterChangeSeqNum  BIGINT , selQuestionSetLocalChangeSeqNum  BIGINT , selQuestionSetLastChangedBy  INTEGER , selQuestionSetUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSet (selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy) SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet_OLD")
                    database.execSQL("DROP TABLE SelQuestionSet_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSet_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSet_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_25_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestionSet_lcsn_seq') END),
                    | selQuestionSetMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionSet_mcsn_seq') 
                    | ELSE NEW.selQuestionSetMasterChangeSeqNum END)
                    | WHERE selQuestionSetUid = NEW.selQuestionSetUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_25_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionSet 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_25_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_25_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_25")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSet_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSet_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSet_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSet for PostgreSQL

                    //Begin: Create table SelQuestionSetRecognition for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSetRecognition RENAME to SelQuestionSetRecognition_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetRecognition (  selqsrSelQuestionSetResponseUid  BIGINT , selQuestionSetRecognitionClazzMemberUid  BIGINT , isSelQuestionSetRecognitionRecognized  BOOL , selQuestionSetRecognitionMasterChangeSeqNum  BIGINT , selQuestionSetRecognitionLocalChangeSeqNum  BIGINT , selQuestionSetRecognitionLastChangedBy  INTEGER , selQuestionSetRecognitionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSetRecognition (selQuestionSetRecognitionUid, selqsrSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, isSelQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy) SELECT selQuestionSetRecognitionUid, selqsrSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, isSelQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition_OLD")
                    database.execSQL("DROP TABLE SelQuestionSetRecognition_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSetRecognition_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSetRecognition_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_26_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestionSetRecognition_lcsn_seq') END),
                    | selQuestionSetRecognitionMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionSetRecognition_mcsn_seq') 
                    | ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END)
                    | WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_26_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionSetRecognition 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_26_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_26_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_26")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetRecognition_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSetRecognition_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSetRecognition_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSetRecognition for PostgreSQL

                    //Begin: Create table SelQuestionSetResponse for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionSetResponse RENAME to SelQuestionSetResponse_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetResponse (  selQuestionSetResponseSelQuestionSetUid  BIGINT , selQuestionSetResponseClazzMemberUid  BIGINT , selQuestionSetResponseStartTime  BIGINT , selQuestionSetResponseFinishTime  BIGINT , selQuestionSetResponseRecognitionPercentage  FLOAT , selQuestionSetResponseMasterChangeSeqNum  BIGINT , selQuestionSetResponseLocalChangeSeqNum  BIGINT , selQuestionSetResponseLastChangedBy  INTEGER , selQuestionSetResposeUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionSetResponse (selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy) SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse_OLD")
                    database.execSQL("DROP TABLE SelQuestionSetResponse_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSetResponse_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionSetResponse_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_27_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionSetResponseLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestionSetResponse_lcsn_seq') END),
                    | selQuestionSetResponseMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionSetResponse_mcsn_seq') 
                    | ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END)
                    | WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_27_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionSetResponse 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_27_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_27_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_27")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionSetResponse_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionSetResponse_trk_clientId_epk_rx_csn 
                    |ON SelQuestionSetResponse_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionSetResponse for PostgreSQL

                    //Begin: Create table Schedule for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE Schedule RENAME to Schedule_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS Schedule (  sceduleStartTime  BIGINT , scheduleEndTime  BIGINT , scheduleDay  INTEGER , scheduleMonth  INTEGER , scheduleFrequency  INTEGER , umCalendarUid  BIGINT , scheduleClazzUid  BIGINT , scheduleMasterChangeSeqNum  BIGINT , scheduleLocalChangeSeqNum  BIGINT , scheduleLastChangedBy  INTEGER , scheduleActive  BOOL , scheduleUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO Schedule (scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive) SELECT scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive FROM Schedule_OLD")
                    database.execSQL("DROP TABLE Schedule_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Schedule_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS Schedule_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_21_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE Schedule SET scheduleLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduleLocalChangeSeqNum 
                    | ELSE NEXTVAL('Schedule_lcsn_seq') END),
                    | scheduleMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('Schedule_mcsn_seq') 
                    | ELSE NEW.scheduleMasterChangeSeqNum END)
                    | WHERE scheduleUid = NEW.scheduleUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_21_trig 
                    |AFTER UPDATE OR INSERT ON Schedule 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_21_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_21_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_21")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS Schedule_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_Schedule_trk_clientId_epk_rx_csn 
                    |ON Schedule_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table Schedule for PostgreSQL

                    //Begin: Create table DateRange for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE DateRange RENAME to DateRange_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS DateRange (  dateRangeLocalChangeSeqNum  BIGINT , dateRangeMasterChangeSeqNum  BIGINT , dateRangLastChangedBy  INTEGER , dateRangeFromDate  BIGINT , dateRangeToDate  BIGINT , dateRangeUMCalendarUid  BIGINT , dateRangeName  TEXT , dateRangeDesc  TEXT , dateRangeActive  BOOL , dateRangeUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO DateRange (dateRangeUid, dateRangeLocalChangeSeqNum, dateRangeMasterChangeSeqNum, dateRangLastChangedBy, dateRangeFromDate, dateRangeToDate, dateRangeUMCalendarUid, dateRangeName, dateRangeDesc, dateRangeActive) SELECT dateRangeUid, dateRangeLocalChangeSeqNum, dateRangeMasterChangeSeqNum, dateRangLastChangedBy, dateRangeFromDate, dateRangeToDate, dateRangeUMCalendarUid, dateRangeName, dateRangeDesc, dateRangeActive FROM DateRange_OLD")
                    database.execSQL("DROP TABLE DateRange_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS DateRange_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS DateRange_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_17_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE DateRange SET dateRangeLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.dateRangeLocalChangeSeqNum 
                    | ELSE NEXTVAL('DateRange_lcsn_seq') END),
                    | dateRangeMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('DateRange_mcsn_seq') 
                    | ELSE NEW.dateRangeMasterChangeSeqNum END)
                    | WHERE dateRangeUid = NEW.dateRangeUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_17_trig 
                    |AFTER UPDATE OR INSERT ON DateRange 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_17_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_17_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_17")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS DateRange_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_DateRange_trk_clientId_epk_rx_csn 
                    |ON DateRange_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table DateRange for PostgreSQL

                    //Begin: Create table UMCalendar for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE UMCalendar RENAME to UMCalendar_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS UMCalendar (  umCalendarName  TEXT , umCalendarCategory  INTEGER , umCalendarActive  BOOL , isUmCalendarFlag  BOOL , umCalendarMasterChangeSeqNum  BIGINT , umCalendarLocalChangeSeqNum  BIGINT , umCalendarLastChangedBy  INTEGER , umCalendarUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO UMCalendar (umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, isUmCalendarFlag, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy) SELECT umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, isUmCalendarFlag, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy FROM UMCalendar_OLD")
                    database.execSQL("DROP TABLE UMCalendar_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS UMCalendar_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS UMCalendar_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_28_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE UMCalendar SET umCalendarLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
                    | ELSE NEXTVAL('UMCalendar_lcsn_seq') END),
                    | umCalendarMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('UMCalendar_mcsn_seq') 
                    | ELSE NEW.umCalendarMasterChangeSeqNum END)
                    | WHERE umCalendarUid = NEW.umCalendarUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_28_trig 
                    |AFTER UPDATE OR INSERT ON UMCalendar 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_28_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_28_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_28")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS UMCalendar_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_UMCalendar_trk_clientId_epk_rx_csn 
                    |ON UMCalendar_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table UMCalendar for PostgreSQL

                    //Begin: Create table ClazzActivity for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzActivity RENAME to ClazzActivity_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivity (  clazzActivityClazzActivityChangeUid  BIGINT , isClazzActivityGoodFeedback  BOOL , clazzActivityNotes  TEXT , clazzActivityLogDate  BIGINT , clazzActivityClazzUid  BIGINT , clazzActivityDone  BOOL , clazzActivityQuantity  BIGINT , clazzActivityMasterChangeSeqNum  BIGINT , clazzActivityLocalChangeSeqNum  BIGINT , clazzActivityLastChangedBy  INTEGER , clazzActivityUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzActivity (clazzActivityUid, clazzActivityClazzActivityChangeUid, isClazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy) SELECT clazzActivityUid, clazzActivityClazzActivityChangeUid, isClazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivity_OLD")
                    database.execSQL("DROP TABLE ClazzActivity_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzActivity_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzActivity_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_11_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ClazzActivity SET clazzActivityLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityLocalChangeSeqNum 
                    | ELSE NEXTVAL('ClazzActivity_lcsn_seq') END),
                    | clazzActivityMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ClazzActivity_mcsn_seq') 
                    | ELSE NEW.clazzActivityMasterChangeSeqNum END)
                    | WHERE clazzActivityUid = NEW.clazzActivityUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_11_trig 
                    |AFTER UPDATE OR INSERT ON ClazzActivity 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_11_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_11_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_11")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivity_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzActivity_trk_clientId_epk_rx_csn 
                    |ON ClazzActivity_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzActivity for PostgreSQL

                    //Begin: Create table ClazzActivityChange for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzActivityChange RENAME to ClazzActivityChange_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivityChange (  clazzActivityChangeTitle  TEXT , clazzActivityDesc  TEXT , clazzActivityUnitOfMeasure  INTEGER , isClazzActivityChangeActive  BOOL , clazzActivityChangeLastChangedBy  INTEGER , clazzActivityChangeMasterChangeSeqNum  BIGINT , clazzActivityChangeLocalChangeSeqNum  BIGINT , clazzActivityLastChangedBy  INTEGER , clazzActivityChangeUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzActivityChange (clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, isClazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy) SELECT clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, isClazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivityChange_OLD")
                    database.execSQL("DROP TABLE ClazzActivityChange_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzActivityChange_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzActivityChange_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_32_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ClazzActivityChange SET clazzActivityChangeLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzActivityChangeLocalChangeSeqNum 
                    | ELSE NEXTVAL('ClazzActivityChange_lcsn_seq') END),
                    | clazzActivityChangeMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ClazzActivityChange_mcsn_seq') 
                    | ELSE NEW.clazzActivityChangeMasterChangeSeqNum END)
                    | WHERE clazzActivityChangeUid = NEW.clazzActivityChangeUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_32_trig 
                    |AFTER UPDATE OR INSERT ON ClazzActivityChange 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_32_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_32_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_32")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzActivityChange_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzActivityChange_trk_clientId_epk_rx_csn 
                    |ON ClazzActivityChange_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzActivityChange for PostgreSQL

                    //Begin: Create table SelQuestionOption for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE SelQuestionOption RENAME to SelQuestionOption_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionOption (  optionText  TEXT , selQuestionOptionQuestionUid  BIGINT , selQuestionOptionMasterChangeSeqNum  BIGINT , selQuestionOptionLocalChangeSeqNum  BIGINT , selQuestionOptionLastChangedBy  INTEGER , optionActive  BOOL , selQuestionOptionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO SelQuestionOption (selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive) SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption_OLD")
                    database.execSQL("DROP TABLE SelQuestionOption_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionOption_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS SelQuestionOption_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_52_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.selQuestionOptionLocalChangeSeqNum 
                    | ELSE NEXTVAL('SelQuestionOption_lcsn_seq') END),
                    | selQuestionOptionMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('SelQuestionOption_mcsn_seq') 
                    | ELSE NEW.selQuestionOptionMasterChangeSeqNum END)
                    | WHERE selQuestionOptionUid = NEW.selQuestionOptionUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_52_trig 
                    |AFTER UPDATE OR INSERT ON SelQuestionOption 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_52_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_52_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_52")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS SelQuestionOption_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_SelQuestionOption_trk_clientId_epk_rx_csn 
                    |ON SelQuestionOption_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table SelQuestionOption for PostgreSQL

                    //Begin: Create table ScheduledCheck for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ScheduledCheck RENAME to ScheduledCheck_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScheduledCheck (  checkTime  BIGINT , checkType  INTEGER , checkUuid  TEXT , checkParameters  TEXT , scClazzLogUid  BIGINT , scheduledCheckMasterCsn  BIGINT , scheduledCheckLocalCsn  BIGINT , scheduledCheckLastChangedBy  INTEGER , scheduledCheckUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ScheduledCheck (scheduledCheckUid, checkTime, checkType, checkUuid, checkParameters, scClazzLogUid, scheduledCheckMasterCsn, scheduledCheckLocalCsn, scheduledCheckLastChangedBy) SELECT scheduledCheckUid, checkTime, checkType, checkUuid, checkParameters, scClazzLogUid, scheduledCheckMasterCsn, scheduledCheckLocalCsn, scheduledCheckLastChangedBy FROM ScheduledCheck_OLD")
                    database.execSQL("DROP TABLE ScheduledCheck_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ScheduledCheck_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ScheduledCheck_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_173_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ScheduledCheck SET scheduledCheckLocalCsn =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduledCheckLocalCsn 
                    | ELSE NEXTVAL('ScheduledCheck_lcsn_seq') END),
                    | scheduledCheckMasterCsn = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ScheduledCheck_mcsn_seq') 
                    | ELSE NEW.scheduledCheckMasterCsn END)
                    | WHERE scheduledCheckUid = NEW.scheduledCheckUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_173_trig 
                    |AFTER UPDATE OR INSERT ON ScheduledCheck 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_173_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_173_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_173")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScheduledCheck_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ScheduledCheck_trk_clientId_epk_rx_csn 
                    |ON ScheduledCheck_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ScheduledCheck for PostgreSQL

                    //Begin: Create table AuditLog for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE AuditLog RENAME to AuditLog_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS AuditLog (  auditLogMasterChangeSeqNum  BIGINT , auditLogLocalChangeSeqNum  BIGINT , auditLogLastChangedBy  INTEGER , auditLogActorPersonUid  BIGINT , auditLogTableUid  INTEGER , auditLogEntityUid  BIGINT , auditLogDate  BIGINT , notes  TEXT , auditLogUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO AuditLog (auditLogUid, auditLogMasterChangeSeqNum, auditLogLocalChangeSeqNum, auditLogLastChangedBy, auditLogActorPersonUid, auditLogTableUid, auditLogEntityUid, auditLogDate, notes) SELECT auditLogUid, auditLogMasterChangeSeqNum, auditLogLocalChangeSeqNum, auditLogLastChangedBy, auditLogActorPersonUid, auditLogTableUid, auditLogEntityUid, auditLogDate, notes FROM AuditLog_OLD")
                    database.execSQL("DROP TABLE AuditLog_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS AuditLog_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS AuditLog_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_53_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE AuditLog SET auditLogLocalChangeSeqNum =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.auditLogLocalChangeSeqNum 
                    | ELSE NEXTVAL('AuditLog_lcsn_seq') END),
                    | auditLogMasterChangeSeqNum = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('AuditLog_mcsn_seq') 
                    | ELSE NEW.auditLogMasterChangeSeqNum END)
                    | WHERE auditLogUid = NEW.auditLogUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_53_trig 
                    |AFTER UPDATE OR INSERT ON AuditLog 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_53_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_53_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_53")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS AuditLog_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_AuditLog_trk_clientId_epk_rx_csn 
                    |ON AuditLog_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table AuditLog for PostgreSQL

                    //Begin: Create table CustomField for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomField RENAME to CustomField_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomField (  customFieldName  TEXT , customFieldNameAlt  TEXT , customFieldLabelMessageID  INTEGER , customFieldIcon  TEXT , customFieldType  INTEGER , customFieldEntityType  INTEGER , customFieldActive  BOOL , customFieldDefaultValue  TEXT , customFieldMCSN  BIGINT , customFieldLCSN  BIGINT , customFieldLCB  INTEGER , customFieldUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomField (customFieldUid, customFieldName, customFieldNameAlt, customFieldLabelMessageID, customFieldIcon, customFieldType, customFieldEntityType, customFieldActive, customFieldDefaultValue, customFieldMCSN, customFieldLCSN, customFieldLCB) SELECT customFieldUid, customFieldName, customFieldNameAlt, customFieldLabelMessageID, customFieldIcon, customFieldType, customFieldEntityType, customFieldActive, customFieldDefaultValue, customFieldMCSN, customFieldLCSN, customFieldLCB FROM CustomField_OLD")
                    database.execSQL("DROP TABLE CustomField_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomField_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomField_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_56_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE CustomField SET customFieldLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldLCSN 
                    | ELSE NEXTVAL('CustomField_lcsn_seq') END),
                    | customFieldMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('CustomField_mcsn_seq') 
                    | ELSE NEW.customFieldMCSN END)
                    | WHERE customFieldUid = NEW.customFieldUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_56_trig 
                    |AFTER UPDATE OR INSERT ON CustomField 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_56_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_56_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_56")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomField_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomField_trk_clientId_epk_rx_csn 
                    |ON CustomField_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomField for PostgreSQL

                    //Begin: Create table CustomFieldValue for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomFieldValue RENAME to CustomFieldValue_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValue (  customFieldValueFieldUid  BIGINT , customFieldValueEntityUid  BIGINT , customFieldValueValue  TEXT , customFieldValueMCSN  BIGINT , customFieldValueLCSN  BIGINT , customFieldValueLCB  INTEGER , customFieldValueUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomFieldValue (customFieldValueUid, customFieldValueFieldUid, customFieldValueEntityUid, customFieldValueValue, customFieldValueMCSN, customFieldValueLCSN, customFieldValueLCB) SELECT customFieldValueUid, customFieldValueFieldUid, customFieldValueEntityUid, customFieldValueValue, customFieldValueMCSN, customFieldValueLCSN, customFieldValueLCB FROM CustomFieldValue_OLD")
                    database.execSQL("DROP TABLE CustomFieldValue_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomFieldValue_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomFieldValue_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_57_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE CustomFieldValue SET customFieldValueLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueLCSN 
                    | ELSE NEXTVAL('CustomFieldValue_lcsn_seq') END),
                    | customFieldValueMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('CustomFieldValue_mcsn_seq') 
                    | ELSE NEW.customFieldValueMCSN END)
                    | WHERE customFieldValueUid = NEW.customFieldValueUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_57_trig 
                    |AFTER UPDATE OR INSERT ON CustomFieldValue 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_57_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_57_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_57")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValue_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomFieldValue_trk_clientId_epk_rx_csn 
                    |ON CustomFieldValue_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomFieldValue for PostgreSQL

                    //Begin: Create table CustomFieldValueOption for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE CustomFieldValueOption RENAME to CustomFieldValueOption_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValueOption (  customFieldValueOptionName  TEXT , customFieldValueOptionFieldUid  BIGINT , customFieldValueOptionIcon  TEXT , customFieldValueOptionMessageId  INTEGER , customFieldValueOptionActive  BOOL , customFieldValueOptionMCSN  BIGINT , customFieldValueOptionLCSN  BIGINT , customFieldValueOptionLCB  INTEGER , customFieldValueOptionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO CustomFieldValueOption (customFieldValueOptionUid, customFieldValueOptionName, customFieldValueOptionFieldUid, customFieldValueOptionIcon, customFieldValueOptionMessageId, customFieldValueOptionActive, customFieldValueOptionMCSN, customFieldValueOptionLCSN, customFieldValueOptionLCB) SELECT customFieldValueOptionUid, customFieldValueOptionName, customFieldValueOptionFieldUid, customFieldValueOptionIcon, customFieldValueOptionMessageId, customFieldValueOptionActive, customFieldValueOptionMCSN, customFieldValueOptionLCSN, customFieldValueOptionLCB FROM CustomFieldValueOption_OLD")
                    database.execSQL("DROP TABLE CustomFieldValueOption_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomFieldValueOption_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS CustomFieldValueOption_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_55_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE CustomFieldValueOption SET customFieldValueOptionLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueOptionLCSN 
                    | ELSE NEXTVAL('CustomFieldValueOption_lcsn_seq') END),
                    | customFieldValueOptionMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('CustomFieldValueOption_mcsn_seq') 
                    | ELSE NEW.customFieldValueOptionMCSN END)
                    | WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_55_trig 
                    |AFTER UPDATE OR INSERT ON CustomFieldValueOption 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_55_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_55_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_55")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS CustomFieldValueOption_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_CustomFieldValueOption_trk_clientId_epk_rx_csn 
                    |ON CustomFieldValueOption_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table CustomFieldValueOption for PostgreSQL


                    //Begin: Create table School for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE School RENAME to School_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS School (  schoolName  TEXT , schoolDesc  TEXT , schoolAddress  TEXT , schoolActive  BOOL , schoolFeatures  BIGINT , schoolLocationLong  DOUBLE precision , schoolLocationLatt  DOUBLE precision, schoolMasterChangeSeqNum  BIGINT , schoolLocalChangeSeqNum  BIGINT , schoolLastChangedBy  INTEGER , schoolUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO School (schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy) SELECT schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy FROM School_OLD")
                    database.execSQL("DROP TABLE School_OLD")
                    END MIGRATION*/
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
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_164_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_164")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS School_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_School_trk_clientId_epk_rx_csn 
                    |ON School_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table School for PostgreSQL

                    //Begin: Create table ClazzAssignment for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzAssignment RENAME to ClazzAssignment_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment (  clazzAssignmentTitle  TEXT , clazzAssignmentClazzUid  BIGINT , clazzAssignmentInactive  BOOL , clazzAssignmentStartDate  BIGINT , clazzAssignmentDueDate  BIGINT , clazzAssignmentCreationDate  BIGINT , clazzAssignmentUpdateDate  BIGINT , clazzAssignmentInstructions  TEXT , clazzAssignmentGrading  INTEGER , clazzAssignmentRequireAttachment  BOOL , clazzAssignmentMCSN  BIGINT , clazzAssignmentLCSN  BIGINT , clazzAssignmentLCB  INTEGER , clazzAssignmentUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzAssignment (clazzAssignmentUid, clazzAssignmentTitle, clazzAssignmentClazzUid, clazzAssignmentInactive, clazzAssignmentStartDate, clazzAssignmentDueDate, clazzAssignmentCreationDate, clazzAssignmentUpdateDate, clazzAssignmentInstructions, clazzAssignmentGrading, clazzAssignmentRequireAttachment, clazzAssignmentMCSN, clazzAssignmentLCSN, clazzAssignmentLCB) SELECT clazzAssignmentUid, clazzAssignmentTitle, clazzAssignmentClazzUid, clazzAssignmentInactive, clazzAssignmentStartDate, clazzAssignmentDueDate, clazzAssignmentCreationDate, clazzAssignmentUpdateDate, clazzAssignmentInstructions, clazzAssignmentGrading, clazzAssignmentRequireAttachment, clazzAssignmentMCSN, clazzAssignmentLCSN, clazzAssignmentLCB FROM ClazzAssignment_OLD")
                    database.execSQL("DROP TABLE ClazzAssignment_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignment_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignment_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_176_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ClazzAssignment SET clazzAssignmentLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentLCSN 
                    | ELSE NEXTVAL('ClazzAssignment_lcsn_seq') END),
                    | clazzAssignmentMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ClazzAssignment_mcsn_seq') 
                    | ELSE NEW.clazzAssignmentMCSN END)
                    | WHERE clazzAssignmentUid = NEW.clazzAssignmentUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_176_trig 
                    |AFTER UPDATE OR INSERT ON ClazzAssignment 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_176_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_176_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_176")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignment_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzAssignment_trk_clientId_epk_rx_csn 
                    |ON ClazzAssignment_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzAssignment for PostgreSQL

                    //Begin: Create table ClazzAssignmentContentJoin for PostgreSQL
                    /* START MIGRATION:
                    database.execSQL("ALTER TABLE ClazzAssignmentContentJoin RENAME to ClazzAssignmentContentJoin_OLD")
                    END MIGRATION */
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (  clazzAssignmentContentJoinContentUid  BIGINT , clazzAssignmentContentJoinClazzAssignmentUid  BIGINT , clazzAssignmentContentJoinInactive  BOOL , clazzAssignmentContentJoinDateAdded  BIGINT , clazzAssignmentContentJoinMCSN  BIGINT , clazzAssignmentContentJoinLCSN  BIGINT , clazzAssignmentContentJoinLCB  INTEGER , clazzAssignmentContentJoinUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    /* START MIGRATION:
                    database.execSQL("INSERT INTO ClazzAssignmentContentJoin (clazzAssignmentContentJoinUid, clazzAssignmentContentJoinContentUid, clazzAssignmentContentJoinClazzAssignmentUid, clazzAssignmentContentJoinInactive, clazzAssignmentContentJoinDateAdded, clazzAssignmentContentJoinMCSN, clazzAssignmentContentJoinLCSN, clazzAssignmentContentJoinLCB) SELECT clazzAssignmentContentJoinUid, clazzAssignmentContentJoinContentUid, clazzAssignmentContentJoinClazzAssignmentUid, clazzAssignmentContentJoinInactive, clazzAssignmentContentJoinDateAdded, clazzAssignmentContentJoinMCSN, clazzAssignmentContentJoinLCSN, clazzAssignmentContentJoinLCB FROM ClazzAssignmentContentJoin_OLD")
                    database.execSQL("DROP TABLE ClazzAssignmentContentJoin_OLD")
                    END MIGRATION*/
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignmentContentJoin_mcsn_seq")
                    database.execSQL("CREATE SEQUENCE IF NOT EXISTS ClazzAssignmentContentJoin_lcsn_seq")
                    database.execSQL("""
                    |CREATE OR REPLACE FUNCTION 
                    | inccsn_177_fn() RETURNS trigger AS ${'$'}${'$'}
                    | BEGIN  
                    | UPDATE ClazzAssignmentContentJoin SET clazzAssignmentContentJoinLCSN =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzAssignmentContentJoinLCSN 
                    | ELSE NEXTVAL('ClazzAssignmentContentJoin_lcsn_seq') END),
                    | clazzAssignmentContentJoinMCSN = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('ClazzAssignmentContentJoin_mcsn_seq') 
                    | ELSE NEW.clazzAssignmentContentJoinMCSN END)
                    | WHERE clazzAssignmentContentJoinUid = NEW.clazzAssignmentContentJoinUid;
                    | RETURN null;
                    | END ${'$'}${'$'}
                    | LANGUAGE plpgsql
                    """.trimMargin())
                    database.execSQL("""
                    |CREATE TRIGGER inccsn_177_trig 
                    |AFTER UPDATE OR INSERT ON ClazzAssignmentContentJoin 
                    |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                    |EXECUTE PROCEDURE inccsn_177_fn()
                    """.trimMargin())
                    /* START MIGRATION:
                    database.execSQL("DROP FUNCTION IF EXISTS inc_csn_177_fn")
                    database.execSQL("DROP SEQUENCE IF EXISTS spk_seq_177")
                    END MIGRATION*/
                    database.execSQL("CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin_trk (  epk  BIGINT , clientId  INTEGER , csn  INTEGER , rx  BOOL , reqId  INTEGER , ts  BIGINT , pk  BIGSERIAL  PRIMARY KEY  NOT NULL )")
                    database.execSQL("""
                    |CREATE 
                    | INDEX index_ClazzAssignmentContentJoin_trk_clientId_epk_rx_csn 
                    |ON ClazzAssignmentContentJoin_trk (clientId, epk, rx, csn)
                    """.trimMargin())
                    //End: Create table ClazzAssignmentContentJoin for PostgreSQL
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


        val MIGRATION_28_29 = object : DoorMigration(28, 29) {
            override fun migrate(database: DoorSqlDatabase) {
                if (database.dbType() == DoorDbType.SQLITE) {
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_9")
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
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_11")
                    database.execSQL("""
        |CREATE TRIGGER UPD_11
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
        |CREATE TRIGGER INS_11
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
                    database.execSQL("DROP TRIGGER IF EXISTS UPD_42")
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


        private fun addMigrations(builder: DatabaseBuilder<UmAppDatabase>): DatabaseBuilder<UmAppDatabase> {

            builder.addMigrations(object : DoorMigration(27,28){

                override fun migrate(database: DoorSqlDatabase) {
                    try {
                        println("Migrating from 27 to 28")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationUid TO selqrnUid")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationClazzMemberUid TO selqrnClazzMemberUid")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationSelQuestionResponseUId TO selqrnSelQuestionResponseUId")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationMasterChangeSeqNum TO selqrnMCSN")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationLocalChangeSeqNum TO selqrnMCSNLCSN")
                        database.execSQL("ALTER TABLE SelQuestionResponseNomination " +
                                " RENAME COLUMN selQuestionResponseNominationLastChangedBy TO selqrnMCSNLCB")

                        database.execSQL("ALTER TABLE selQuestionSetRecognition RENAME COLUMN selQuestionSetRecognitionSelQuestionSetResponseUid TO selqsrSelQuestionSetResponseUid")
                    } catch (e:Exception) {
                        print("Migration exception: " + e.message)
                    }
                }
            })

            builder.addMigrations(object : DoorMigration(26,27){
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
                    try{
                        database.execSQL("ALTER TABLE Container RENAME COLUMN lastModified TO cntLastModified")
                    } catch (e:Exception) {
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

            builder.addMigrations(object : DoorMigration(25, 26) {
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN imported, ADD COLUMN status INTEGER NOT NULL DEFAULT 1")
                }

            })

            builder.addMigrations(object : DoorMigration(26, 27) {
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS LocallyAvailableContainer (  laContainerUid  BIGINT  PRIMARY KEY  NOT NULL )")
                }
            })

            builder.addMigrations(MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36)
            return builder
        }
    }


}
