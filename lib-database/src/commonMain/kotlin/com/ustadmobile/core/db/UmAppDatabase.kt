package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

@Database(entities = [NetworkNode::class, DownloadJobItemHistory::class,
    DownloadJob::class, DownloadJobItem::class, DownloadJobItemParentChildJoin::class, Person::class,
    Clazz::class, ClazzMember::class, PersonCustomField::class, PersonCustomFieldValue::class,
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
    SyncNode::class, LocallyAvailableContainer::class

    //#DOORDB_TRACKER_ENTITIES

], version = 29)
abstract class UmAppDatabase : DoorDatabase(), SyncableDoorDatabase {

    var attachmentsDir: String? = null

    override val master: Boolean
        get() = false

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

    @JsName("xLangMapEntryDao")
    abstract val xLangMapEntryDao: XLangMapEntryDao

    abstract val locallyAvailableContainerDao: LocallyAvailableContainerDao

    //#DOORDB_SYNCDAO

    //abstract val syncablePrimaryKeyDao: SyncablePrimaryKeyDao

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

//        @Synchronized
//        fun getInstance(context: Any): UmAppDatabase {
//            if (instance == null) {
//                var builder = DatabaseBuilder.databaseBuilder(
//                        context, UmAppDatabase::class, "UmAppDatabase")
//               // builder = addMigrations(builder)
//               //instance = addCallbacks(builder).build()
//                instance = builder.build()
//            }
//
//            return instance!!
//        }

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

        /**
         * Fix SQLite update triggers
         */
        val MIGRATION_28_29 = object: DoorMigration(28, 29) {
            override fun migrate(database: DoorSqlDatabase) {
                if(database.dbType() == DoorDbType.SQLITE) {
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

        private fun addMigrations(builder: DatabaseBuilder<UmAppDatabase>): DatabaseBuilder<UmAppDatabase> {

            builder.addMigrations(object : DoorMigration(26,27){
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN status, ADD COLUMN contentFlags INTEGER NOT NULL DEFAULT 0, ADD COLUMN ceInactive BOOL")
                }

            })


            builder.addMigrations(object : DoorMigration(25,26){
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN imported, ADD COLUMN status INTEGER NOT NULL DEFAULT 0")
                }

            })

            builder.addMigrations(object :DoorMigration(24, 25){
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE Container RENAME COLUMN lastModified TO cntLastModified")
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

            builder.addMigrations(object : DoorMigration(25,26){
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("ALTER TABLE ContentEntry DROP COLUMN imported, ADD COLUMN status INTEGER NOT NULL DEFAULT 1")
                }

            })

            builder.addMigrations(object : DoorMigration(26, 27) {
                override fun migrate(database: DoorSqlDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS LocallyAvailableContainer (  laContainerUid  BIGINT  PRIMARY KEY  NOT NULL )")
                }
            })

            builder.addMigrations(MIGRATION_27_28, MIGRATION_28_29)

            return builder
        }
    }



}
