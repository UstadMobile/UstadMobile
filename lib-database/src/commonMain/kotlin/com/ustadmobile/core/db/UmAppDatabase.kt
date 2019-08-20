package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.SyncableDoorDatabase
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

@Database(entities = [NetworkNode::class, EntryStatusResponse::class, DownloadJobItemHistory::class,
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
    SyncNode::class

    //#DOORDB_TRACKER_ENTITIES

], version = 24)
abstract class UmAppDatabase : DoorDatabase(), SyncableDoorDatabase {

    var attachmentsDir: String? = null

    override val master: Boolean
        get() = false

    abstract val networkNodeDao: NetworkNodeDao

    abstract val entryStatusResponseDao: EntryStatusResponseDao

    abstract val downloadJobDao: DownloadJobDao

    abstract val downloadJobItemDao: DownloadJobItemDao

    abstract val downloadJobItemParentChildJoinDao: DownloadJobItemParentChildJoinDao

    abstract val downloadJobItemHistoryDao: DownloadJobItemHistoryDao

    abstract val personDao: PersonDao

    abstract val clazzDao: ClazzDao

    abstract val clazzMemberDao: ClazzMemberDao

    abstract val contentEntryDao: ContentEntryDao

    abstract val personCustomFieldDao: PersonCustomFieldDao

    abstract val personCustomFieldValueDao: PersonCustomFieldValueDao

    abstract val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao

    abstract val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao

    abstract val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao

    // abstract val syncStatusDao: SyncStatusDao

    abstract val contentCategorySchemaDao: ContentCategorySchemaDao

    abstract val contentCategoryDao: ContentCategoryDao

    abstract val languageDao: LanguageDao

    abstract val languageVariantDao: LanguageVariantDao

    abstract val scrapeQueueItemDao: ScrapeQueueItemDao

    abstract val personAuthDao: PersonAuthDao

    abstract val accessTokenDao: AccessTokenDao

    abstract val roleDao: RoleDao

    abstract val personGroupDao: PersonGroupDao

    abstract val personGroupMemberDao: PersonGroupMemberDao

    abstract val entityRoleDao: EntityRoleDao

    abstract val locationDao: LocationDao

    abstract val locationAncestorJoinDao: LocationAncestorJoinDao

    abstract val personLocationJoinDao: PersonLocationJoinDao

    abstract val personPictureDao: PersonPictureDao

    abstract val scrapeRunDao: ScrapeRunDao

    abstract val contentEntryStatusDao: ContentEntryStatusDao

    abstract val connectivityStatusDao: ConnectivityStatusDao

    abstract val containerDao: ContainerDao

    abstract val containerEntryDao: ContainerEntryDao

    abstract val containerEntryFileDao: ContainerEntryFileDao

    abstract val verbDao: VerbDao

    abstract val xObjectDao: XObjectDao

    abstract val statementDao: StatementDao

    abstract val contextXObjectStatementJoinDao: ContextXObjectStatementJoinDao

    abstract val stateDao: StateDao

    abstract val stateContentDao: StateContentDao

    abstract val agentDao: AgentDao

    abstract val xLangMapEntryDao: XLangMapEntryDao

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

        fun getInstance(context: Any) = lazy { Companion.getInstance(context, "UmAppDatabase") }.value

        @Synchronized
        fun getInstance(context: Any, dbName: String): UmAppDatabase {
            var db = namedInstances[dbName]

            if (db == null) {
                var builder = DatabaseBuilder.databaseBuilder(
                        context, UmAppDatabase::class, dbName)
                //builder = addMigrations(builder)
                //db = addCallbacks(builder).build()
                db = builder.build()
                namedInstances[dbName] = db
            }
            return db
        }

      /*  private fun addMigrations(
                builder: AbstractDoorwayDbBuilder<UmAppDatabase>): AbstractDoorwayDbBuilder<UmAppDatabase> {
            builder.addMigration(object : UmDbMigration(1, 2) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> {
                            //Must use new device bits, otherwise
                            val deviceBits = Random().nextInt()

                            db.execSql("ALTER TABLE SyncDeviceBits ADD COLUMN master BOOL")
                            db.execSql("UPDATE SyncDeviceBits SET deviceBits = " + deviceBits +
                                    ", master = TRUE")

                            db.execSql("CREATE TABLE IF NOT EXISTS  " +
                                    "ScrapeRun  ( scrapeRunUid  SERIAL PRIMARY KEY  NOT NULL ,  " +
                                    "scrapeType  TEXT,  status  INTEGER)")
                            db.execSql("CREATE TABLE IF NOT EXISTS  ScrapeQueueItem  " +
                                    "( sqiUid  SERIAL PRIMARY KEY  NOT NULL ,  sqiContentEntryParentUid  BIGINT,  " +
                                    "destDir  TEXT,  scrapeUrl  TEXT,  status  INTEGER,  runId  INTEGER,  time  TEXT, " +
                                    " itemType  INTEGER,  contentType  TEXT)")


                            db.execSql("ALTER TABLE SyncStatus ADD COLUMN nextChangeSeqNum BIGINT")
                            db.execSql("ALTER TABLE SyncStatus DROP COLUMN masterchangeseqnum")
                            db.execSql("ALTER TABLE SyncStatus DROP COLUMN localchangeseqnum ")
                            db.execSql("DELETE FROM SyncStatus")

                            db.execSql("DROP TRIGGER IF EXISTS  increment_csn_clazz_trigger ON clazz")
                            db.execSql("DROP FUNCTION IF EXISTS  increment_csn_clazz_fn")

                              db.execSql("DROP TRIGGER IF EXISTS  increment_csn_clazzmember_trigger ON clazzmember")
                              db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzmember_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_contentcategory_trigger ON contentcategory")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategory_fn")

                            db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contentcategoryschema_trigger ON contentcategoryschema")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategoryschema_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententry_trigger ON contententry")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententry_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententrycontentcategoryjoin_trigger ON contententrycontentcategoryjoin")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontentcategoryjoin_fn")

                            db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contententrycontententryfilejoin_trigger ON contententrycontententryfilejoin")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontententryfilejoin_fn")

                            db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contententryfile_trigger ON contententryfile")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryfile_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryparentchildjoin_trigger ON contententryparentchildjoin")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryparentchildjoin_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryrelatedentryjoin_trigger ON contententryrelatedentryjoin")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryrelatedentryjoin_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_entityrole_trigger ON entityrole")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_entityrole_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_language_trigger ON language")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_language_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_languagevariant_trigger ON languagevariant")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_languagevariant_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_entityrole_trigger ON entityrole")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_entityrole_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_location_trigger ON location")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_location_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_person_trigger ON person")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_person_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_personauth_trigger ON personauth")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_personauth_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroup_trigger ON persongroup")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroup_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroupmember_trigger ON persongroupmember")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroupmember_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_personlocationjoin_trigger ON personlocationjoin")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_personlocationjoin_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_personpicture_trigger ON personpicture")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_personpicture_fn")

                            db.execSql("DROP TRIGGER IF EXISTS increment_csn_role_trigger ON role")
                            db.execSql("DROP FUNCTION IF EXISTS increment_csn_role_fn")

                            db.execSql("CREATE SEQUENCE spk_seq_42 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntry ALTER COLUMN contentEntryUid SET DEFAULT NEXTVAL('spk_seq_42')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (42, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_42_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) END),contentEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) ELSE NEW.contentEntryMasterChangeSeqNum END) WHERE contentEntryUid = NEW.contentEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 42; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_42_trig AFTER UPDATE OR INSERT ON ContentEntry FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_42_fn()")

                            db.execSql("CREATE SEQUENCE spk_seq_3 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntryContentCategoryJoin ALTER COLUMN ceccjUid SET DEFAULT NEXTVAL('spk_seq_3')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (3, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_3_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.ceccjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) END),ceccjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) ELSE NEW.ceccjMasterChangeSeqNum END) WHERE ceccjUid = NEW.ceccjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 3; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_3_trig AFTER UPDATE OR INSERT ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_3_fn()")

                            db.execSql("CREATE SEQUENCE spk_seq_4 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntryContentEntryFileJoin ALTER COLUMN cecefjUid SET DEFAULT NEXTVAL('spk_seq_4')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (4, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_4_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentEntryFileJoin SET cecefjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cecefjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) END),cecefjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) ELSE NEW.cecefjMasterChangeSeqNum END) WHERE cecefjUid = NEW.cecefjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 4; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_4_trig AFTER UPDATE OR INSERT ON ContentEntryContentEntryFileJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_4_fn()")
                            //END Create ContentEntryContentEntryFileJoin (PostgreSQL)

                            //BEGIN Create ContentEntryFile (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_5 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntryFile ALTER COLUMN contentEntryFileUid SET DEFAULT NEXTVAL('spk_seq_5')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (5, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_5_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryFile SET contentEntryFileLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryFileLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) END),contentEntryFileMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) ELSE NEW.contentEntryFileMasterChangeSeqNum END) WHERE contentEntryFileUid = NEW.contentEntryFileUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 5; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_5_trig AFTER UPDATE OR INSERT ON ContentEntryFile FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_5_fn()")
                            //END Create ContentEntryFile (PostgreSQL)

                            //BEGIN Create ContentEntryParentChildJoin (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_7 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntryParentChildJoin ALTER COLUMN cepcjUid SET DEFAULT NEXTVAL('spk_seq_7')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (7, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_7_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cepcjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) END),cepcjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) ELSE NEW.cepcjMasterChangeSeqNum END) WHERE cepcjUid = NEW.cepcjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 7; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_7_trig AFTER UPDATE OR INSERT ON ContentEntryParentChildJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_7_fn()")
                            //END Create ContentEntryParentChildJoin (PostgreSQL)

                            //BEGIN Create ContentEntryRelatedEntryJoin (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_8 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentEntryRelatedEntryJoin ALTER COLUMN cerejUid SET DEFAULT NEXTVAL('spk_seq_8')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (8, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_8_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cerejLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) END),cerejMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) ELSE NEW.cerejMasterChangeSeqNum END) WHERE cerejUid = NEW.cerejUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 8; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_8_trig AFTER UPDATE OR INSERT ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_8_fn()")
                            //END Create ContentEntryRelatedEntryJoin (PostgreSQL)

                            db.execSql("CREATE SEQUENCE spk_seq_2 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ContentCategorySchema ALTER COLUMN contentCategorySchemaUid SET DEFAULT NEXTVAL('spk_seq_2')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (2, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_2_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategorySchemaLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) END),contentCategorySchemaMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) ELSE NEW.contentCategorySchemaMasterChangeSeqNum END) WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 2; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_2_trig AFTER UPDATE OR INSERT ON ContentCategorySchema FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_2_fn()")
                            //END Create ContentCategorySchema (PostgreSQL)

                            //BEGIN Create ContentCategory (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_1 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE  ContentCategory ALTER COLUMN contentCategoryUid SET DEFAULT NEXTVAL('spk_seq_1')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (1, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_1_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategoryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) END),contentCategoryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) ELSE NEW.contentCategoryMasterChangeSeqNum END) WHERE contentCategoryUid = NEW.contentCategoryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 1; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_1_trig AFTER UPDATE OR INSERT ON ContentCategory FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_1_fn()")
                            //END Create ContentCategory (PostgreSQL)

                            //BEGIN Create Language (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_13 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE Language ALTER COLUMN langUid SET DEFAULT NEXTVAL('spk_seq_13')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (13, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_13_fn() RETURNS trigger AS $$ BEGIN UPDATE Language SET langLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) END),langMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) ELSE NEW.langMasterChangeSeqNum END) WHERE langUid = NEW.langUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 13; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_13_trig AFTER UPDATE OR INSERT ON Language FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_13_fn()")
                            //END Create Language (PostgreSQL)

                            //BEGIN Create LanguageVariant (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_10 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE LanguageVariant  ALTER COLUMN langVariantUid  SET DEFAULT NEXTVAL('spk_seq_10')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (10, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_10_fn() RETURNS trigger AS $$ BEGIN UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langVariantLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) END),langVariantMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) ELSE NEW.langVariantMasterChangeSeqNum END) WHERE langVariantUid = NEW.langVariantUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 10; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_10_trig AFTER UPDATE OR INSERT ON LanguageVariant FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_10_fn()")
                            //END Create LanguageVariant (PostgreSQL)

                            //BEGIN Create Person (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_9 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE  Person ALTER COLUMN personUid SET DEFAULT NEXTVAL('spk_seq_9')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (9, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_9_fn() RETURNS trigger AS $$ BEGIN UPDATE Person SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) ELSE NEW.personMasterChangeSeqNum END) WHERE personUid = NEW.personUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 9; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_9_trig AFTER UPDATE OR INSERT ON Person FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_9_fn()")
                            //END Create Person (PostgreSQL)

                            //BEGIN Create Clazz (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_6 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE Clazz ALTER COLUMN clazzUid SET DEFAULT NEXTVAL('spk_seq_6')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (6, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_6_fn() RETURNS trigger AS $$ BEGIN UPDATE Clazz SET clazzLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) END),clazzMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) ELSE NEW.clazzMasterChangeSeqNum END) WHERE clazzUid = NEW.clazzUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 6; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_6_trig AFTER UPDATE OR INSERT ON Clazz FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_6_fn()")
                            //END Create Clazz (PostgreSQL)

                            //BEGIN Create ClazzMember (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_11 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE ClazzMember ALTER COLUMN clazzMemberUid SET DEFAULT NEXTVAL('spk_seq_11')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (11, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_11_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzMemberLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) END),clazzMemberMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) ELSE NEW.clazzMemberMasterChangeSeqNum END) WHERE clazzMemberUid = NEW.clazzMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 11; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_11_trig AFTER UPDATE OR INSERT ON ClazzMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_11_fn()")
                            //END Create ClazzMember (PostgreSQL)

                            //BEGIN Create PersonAuth (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_30 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE PersonAuth  ALTER COLUMN personAuthUid SET DEFAULT NEXTVAL('spk_seq_30')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (30, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_30_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonAuth SET personAuthLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personAuthLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) END),personAuthMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) ELSE NEW.personAuthMasterChangeSeqNum END) WHERE personAuthUid = NEW.personAuthUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 30; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_30_trig AFTER UPDATE OR INSERT ON PersonAuth FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_30_fn()")
                            //END Create PersonAuth (PostgreSQL)

                            //BEGIN Create Role (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_45 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE Role ALTER COLUMN roleUid SET  DEFAULT NEXTVAL('spk_seq_45')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (45, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_45_fn() RETURNS trigger AS $$ BEGIN UPDATE Role SET roleLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.roleLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) END),roleMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) ELSE NEW.roleMasterCsn END) WHERE roleUid = NEW.roleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 45; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_45_trig AFTER UPDATE OR INSERT ON Role FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_45_fn()")
                            //END Create Role (PostgreSQL)

                            //BEGIN Create EntityRole (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_47 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE EntityRole ALTER COLUMN erUid SET DEFAULT NEXTVAL('spk_seq_47')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (47, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_47_fn() RETURNS trigger AS $$ BEGIN UPDATE EntityRole SET erLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.erLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) END),erMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) ELSE NEW.erMasterCsn END) WHERE erUid = NEW.erUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 47; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_47_trig AFTER UPDATE OR INSERT ON EntityRole FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_47_fn()")
                            //END Create EntityRole (PostgreSQL)

                            //BEGIN Create PersonGroup (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_43 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE  PersonGroup ALTER COLUMN  groupUid SET DEFAULT NEXTVAL('spk_seq_43')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (43, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_43_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroup SET groupLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) END),groupMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) ELSE NEW.groupMasterCsn END) WHERE groupUid = NEW.groupUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 43; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_43_trig AFTER UPDATE OR INSERT ON PersonGroup FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_43_fn()")
                            //END Create PersonGroup (PostgreSQL)

                            //BEGIN Create PersonGroupMember (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_44 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE  PersonGroupMember  ALTER COLUMN groupMemberUid SET DEFAULT NEXTVAL('spk_seq_44')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (44, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_44_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroupMember SET groupMemberLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupMemberLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) END),groupMemberMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) ELSE NEW.groupMemberMasterCsn END) WHERE groupMemberUid = NEW.groupMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 44; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_44_trig AFTER UPDATE OR INSERT ON PersonGroupMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_44_fn()")
                            //END Create PersonGroupMember (PostgreSQL)

                            //BEGIN Create Location (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_29 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE  Location  ALTER COLUMN locationUid  SET DEFAULT NEXTVAL('spk_seq_29')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (29, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_29_fn() RETURNS trigger AS $$ BEGIN UPDATE Location SET locationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.locationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) END),locationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) ELSE NEW.locationMasterChangeSeqNum END) WHERE locationUid = NEW.locationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 29; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_29_trig AFTER UPDATE OR INSERT ON Location FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_29_fn()")
                            //END Create Location (PostgreSQL)

                            //BEGIN Create PersonLocationJoin (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_48 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE PersonLocationJoin  ALTER COLUMN  personLocationUid SET DEFAULT NEXTVAL('spk_seq_48')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (48, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_48_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonLocationJoin SET plLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.plLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) END),plMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) ELSE NEW.plMasterCsn END) WHERE personLocationUid = NEW.personLocationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 48; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_48_trig AFTER UPDATE OR INSERT ON PersonLocationJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_48_fn()")
                            //END Create PersonLocationJoin (PostgreSQL)

                            //BEGIN Create PersonPicture (PostgreSQL)
                            db.execSql("CREATE SEQUENCE spk_seq_50 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("ALTER TABLE PersonPicture  ALTER COLUMN personPictureUid  SET DEFAULT NEXTVAL('spk_seq_50')")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (50, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_50_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonPicture SET personPictureLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personPictureLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) END),personPictureMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) ELSE NEW.personPictureMasterCsn END) WHERE personPictureUid = NEW.personPictureUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 50; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_50_trig AFTER UPDATE OR INSERT ON PersonPicture FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_50_fn()")
                        }
                    }//END Create PersonPicture (PostgreSQL)
                }
            })*/
          /*  builder.addMigration(object : UmDbMigration(2, 4) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES ->
                            //Must use new device bits, otherwise
                            db.execSql("ALTER TABLE ScrapeQueueItem " +
                                    "ADD COLUMN timeAdded BIGINT DEFAULT 0, " +
                                    "ADD COLUMN timeStarted BIGINT DEFAULT 0, " +
                                    "ADD COLUMN timeFinished BIGINT DEFAULT 0, " +
                                    "DROP COLUMN time "
                            )
                    }
                }
            }) */

          /*  builder.addMigration(object : UmDbMigration(4, 6) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> {
                            //Must use new device bits, otherwise
                            //BEGIN Create ContentEntryStatus (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryStatus  ( cesUid  BIGINT PRIMARY KEY  NOT NULL ,  totalSize  BIGINT,  bytesDownloadSoFar  BIGINT,  downloadStatus  INTEGER,  invalidated  BOOL,  cesLeaf  BOOL)")
                            //END Create ContentEntryStatus (PostgreSQL)

                            //BEGIN Create ConnectivityStatus (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  ConnectivityStatus  ( csUid  INTEGER PRIMARY KEY  NOT NULL ,  connectivityState  INTEGER,  wifiSsid  TEXT,  connectedOrConnecting  BOOL)")
                            //END Create ConnectivityStatus (PostgreSQL)

                            db.execSql("DROP TABLE  IF EXISTS DownloadJob")
                            //BEGIN Create DownloadJob (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJob  ( djUid  SERIAL PRIMARY KEY  NOT NULL ,  djDsUid  INTEGER,  timeCreated  BIGINT,  timeRequested  BIGINT,  timeCompleted  BIGINT,  djStatus  INTEGER)")
                            //END Create DownloadJob (PostgreSQL)

                            db.execSql("DROP TABLE  IF EXISTS DownloadJobItem")
                            //BEGIN Create DownloadJobItem (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJobItem  ( djiUid  SERIAL PRIMARY KEY  NOT NULL ,  djiDsiUid  INTEGER,  djiDjUid  INTEGER,  djiContentEntryFileUid  BIGINT,  downloadedSoFar  BIGINT,  downloadLength  BIGINT,  currentSpeed  BIGINT,  timeStarted  BIGINT,  timeFinished  BIGINT,  djiStatus  INTEGER,  destinationFile  TEXT,  numAttempts  INTEGER)")
                            db.execSql("CREATE INDEX  index_DownloadJobItem_djiStatus  ON  DownloadJobItem  ( djiStatus  )")
                            //END Create DownloadJobItem (PostgreSQL)

                            db.execSql("DROP TABLE  IF EXISTS DownloadJobItemHistory")
                            //BEGIN Create DownloadJobItemHistory (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJobItemHistory  ( id  SERIAL PRIMARY KEY  NOT NULL ,  url  TEXT,  networkNode  BIGINT,  downloadJobItemId  INTEGER,  mode  INTEGER,  numBytes  BIGINT,  successful  BOOL,  startTime  BIGINT,  endTime  BIGINT)")
                            //END Create DownloadJobItemHistory (PostgreSQL)

                            db.execSql("DROP TABLE  IF EXISTS DownloadSet")
                            //BEGIN Create DownloadSet (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSet  ( dsUid  SERIAL PRIMARY KEY  NOT NULL ,  destinationDir  TEXT,  meteredNetworkAllowed  BOOL,  dsRootContentEntryUid  BIGINT)")
                            //END Create DownloadSet (PostgreSQL)

                            db.execSql("DROP TABLE  IF EXISTS DownloadSetItem")
                            //BEGIN Create DownloadSetItem (PostgreSQL)
                            db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSetItem  ( dsiUid  SERIAL PRIMARY KEY  NOT NULL ,  dsiDsUid  INTEGER,  dsiContentEntryUid  BIGINT)")
                            db.execSql("CREATE INDEX  index_DownloadSetItem_dsiContentEntryUid  ON  DownloadSetItem  ( dsiContentEntryUid  )")
                            db.execSql("CREATE INDEX  index_DownloadSetItem_dsiDsUid  ON  DownloadSetItem  ( dsiDsUid  )")
                        }
                    }//END Create DownloadSetItem (PostgreSQL)
                }
            })

            builder.addMigration(object : UmDbMigration(6, 8) {
                fun migrate(db: DoorDbAdapter) {
                    db.execSql("DROP TABLE IF EXISTS CrawlJob")
                    db.execSql("DROP TABLE IF EXISTS CrawlJobItem")
                    db.execSql("DROP TABLE IF EXISTS OpdsEntry")
                    db.execSql("DROP TABLE IF EXISTS OpdsEntryParentToChildJoin")
                    db.execSql("DROP TABLE IF EXISTS OpdsEntryRelative")
                    db.execSql("DROP TABLE IF EXISTS OpdsEntryStatusCache")
                    db.execSql("DROP TABLE IF EXISTS OpdsEntryStatusCacheAncestor")
                    db.execSql("DROP TABLE IF EXISTS OpdsLink")
                }
            })
            builder.addMigration(object : UmDbMigration(8, 10) {
                fun migrate(db: DoorDbAdapter) {

                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> db.execSql("ALTER TABLE ContentEntry ADD COLUMN contentTypeFlag INTEGER DEFAULT 0")
                    }
                }
            })

            builder.addMigration(object : UmDbMigration(10, 12) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> {
                            val deviceBits = Integer.parseInt(
                                    db.selectSingleValue("SELECT deviceBits FROM SyncDeviceBits"))


                            db.execSql("ALTER TABLE ContentEntryContentEntryFileJoin ADD COLUMN cecefjContainerUid BIGINT")
                            db.execSql("CREATE INDEX  index_ContentEntryContentEntryFileJoin_cecefjContainerUid  ON  ContentEntryContentEntryFileJoin  ( cecefjContainerUid  )")


                            // BEGIN Create Container
                            db.execSql("CREATE SEQUENCE spk_seq_51 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits))
                            db.execSql("CREATE TABLE IF NOT EXISTS  Container  ( containerUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_51') ,  cntLocalCsn  BIGINT,  cntMasterCsn  BIGINT,  cntLastModBy  INTEGER,  fileSize  BIGINT,  containerContentEntryUid  BIGINT,  lastModified  BIGINT,  mimeType  TEXT,  remarks  TEXT,  mobileOptimized  BOOL)")
                            db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (51, 1, 0, 0)")
                            db.execSql("CREATE OR REPLACE FUNCTION inc_csn_51_fn() RETURNS trigger AS $$ BEGIN UPDATE Container SET cntLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cntLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 51) END),cntMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 51) ELSE NEW.cntMasterCsn END) WHERE containerUid = NEW.containerUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 51; RETURN null; END $\$LANGUAGE plpgsql")
                            db.execSql("CREATE TRIGGER inc_csn_51_trig AFTER UPDATE OR INSERT ON Container FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_51_fn()")
                            db.execSql("CREATE INDEX  index_Container_lastModified  ON  Container  ( lastModified  )")
                            //END Create Container (

                            //BEGIN Create ContainerEntry
                            db.execSql("CREATE TABLE IF NOT EXISTS  ContainerEntry  ( ceUid  SERIAL PRIMARY KEY  NOT NULL ,  cePath  TEXT,  ceCefUid  BIGINT)")
                            //END Create ContainerEntry (PostgreSQL)

                            //BEGIN Create ContainerEntryFile
                            db.execSql("CREATE TABLE IF NOT EXISTS  ContainerEntryFile  ( cefUid  SERIAL PRIMARY KEY  NOT NULL ,  cefMd5  TEXT,  cefPath  TEXT,  ceTotalSize  BIGINT,  ceCompressedSize  BIGINT,  compression  INTEGER)")
                        }
                    }//END Create ContainerEntryFile
                }
            })

            builder.addMigration(object : UmDbMigration(12, 14) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> {
                            db.execSql("ALTER TABLE ContainerEntry ADD COLUMN ceContainerUid BIGINT")
                            db.execSql("CREATE INDEX  index_ContainerEntry_ceContainerUid  ON  ContainerEntry  ( ceContainerUid  )")
                        }
                    }
                }
            })

            builder.addMigration(object : UmDbMigration(14, 16) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> db.execSql("ALTER TABLE Container ADD COLUMN cntNumEntries INTEGER")
                    }
                }
            })

            builder.addMigration(object : UmDbMigration(16, 18) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                        UmDbType.TYPE_POSTGRES -> {
                            db.execSql("DROP TABLE ContentEntryFile")
                            db.execSql("DROP TABLE ContentEntryFileStatus")
                            db.execSql("DROP TABLE ContentEntryContentEntryFileJoin")
                        }
                    }
                }
            })

            builder.addMigration(object : UmDbMigration(18, 20) {
                fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                        UmDbType.TYPE_SQLITE -> {
                            db.execSql("ALTER TABLE DownloadJob ADD COLUMN djRootContentEntryUid INTEGER NOT NULL")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN djiContentEntryUid INTEGER NOT NULL")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN meteredNetworkAllowed INTEGER NOT NULL")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN djDestinationDir TEXT")
                        }

                        UmDbType.TYPE_POSTGRES -> {
                            db.execSql("ALTER TABLE DownloadJob ADD COLUMN djRootContentEntryUid BIGINT DEFAULT 0")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN djiContentEntryUid BIGINT DEFAULT 0")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN meteredNetworkAllowed BOOL DEFAULT FALSE")
                            db.execSql("ALTER TABLE DownloadJobItem ADD COLUMN djDestinationDir TEXT")
                        }
                    }
                }
            })

              builder.addMigration(object : UmDbMigration(18, 20) {
                    fun migrate(db: DoorDbAdapter) {
                    when (db.getDbType()) {
                     UmDbType.TYPE_SQLITE -> throw RuntimeException("Not supported on SQLite")

                      UmDbType.TYPE_POSTGRES -> {
                               db.execSql("ALTER TABLE ContainerEntryFile ADD COLUMN lastModified BIGINT");
                        }
                  }

                 }

                 })


          builder.addMigration(object : UmDbMigration(20, 22) {
              fun migrate(db: DoorDbAdapter) {
                  db.execSql("DROP TABLE DownloadSet")
                  db.execSql("DROP TABLE DownloadSetItem")
              }
          })

          builder.addMigration(new UmDbMigration(22, 24) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                     case UmDbType.TYPE_POSTGRES:

                         int deviceBits = Integer.parseInt(
                                db.selectSingleValue("SELECT deviceBits FROM SyncDeviceBits"));

                         //BEGIN Create VerbEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_62 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  VerbEntity  ( verbUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_62') ,  urlId  TEXT,  verbMasterChangeSeqNum  BIGINT,  verbLocalChangeSeqNum  BIGINT,  verbLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (62, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_62_fn() RETURNS trigger AS $$ BEGIN UPDATE VerbEntity SET verbLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.verbLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 62) END),verbMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 62) ELSE NEW.verbMasterChangeSeqNum END) WHERE verbUid = NEW.verbUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 62; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_62_trig AFTER UPDATE OR INSERT ON VerbEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_62_fn()");
                        //END Create VerbEntity (PostgreSQL)

                         //BEGIN Create XObjectEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_64 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  XObjectEntity  ( XObjectUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_64') ,  objectType  TEXT,  objectId  TEXT,  definitionType  TEXT,  interactionType  TEXT,  correctResponsePattern  TEXT,  XObjectMasterChangeSeqNum  BIGINT,  XObjectocalChangeSeqNum  BIGINT,  XObjectLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (64, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_64_fn() RETURNS trigger AS $$ BEGIN UPDATE XObjectEntity SET XObjectocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.XObjectocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 64) END),XObjectMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 64) ELSE NEW.XObjectMasterChangeSeqNum END) WHERE XObjectUid = NEW.XObjectUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 64; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_64_trig AFTER UPDATE OR INSERT ON XObjectEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_64_fn()");
                        //END Create XObjectEntity (PostgreSQL)

                         //BEGIN Create StatementEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_60 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  StatementEntity  ( statementUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_60') ,  statementId  TEXT,  personUid  BIGINT,  verbUid  BIGINT,  XObjectUid  BIGINT,  subStatementActorUid  BIGINT,  substatementVerbUid  BIGINT,  subStatementObjectUid  BIGINT,  agentUid  BIGINT,  instructorUid  BIGINT,  authorityUid  BIGINT,  teamUid  BIGINT,  resultCompletion  BOOL,  resultSuccess  BOOL,  resultScoreScaled  BIGINT,  resultScoreRaw  BIGINT,  resultScoreMin  BIGINT,  resultScoreMax  BIGINT,  resultDuration  BIGINT,  resultResponse  TEXT,  timestamp  BIGINT,  stored  BIGINT,  contextRegistration  TEXT,  contextPlatform  TEXT,  contextStatementId  TEXT,  fullStatement  TEXT,  statementMasterChangeSeqNum  BIGINT,  statementLocalChangeSeqNum  BIGINT,  statementLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (60, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_60_fn() RETURNS trigger AS $$ BEGIN UPDATE StatementEntity SET statementLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.statementLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 60) END),statementMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 60) ELSE NEW.statementMasterChangeSeqNum END) WHERE statementUid = NEW.statementUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 60; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_60_trig AFTER UPDATE OR INSERT ON StatementEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_60_fn()");
                        //END Create StatementEntity (PostgreSQL)

                         //BEGIN Create ContextXObjectStatementJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_66 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContextXObjectStatementJoin  ( contextXObjectStatementJoinUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_66') ,  contextActivityFlag  INTEGER,  contextStatementUid  BIGINT,  contextXObjectUid  BIGINT,  verbMasterChangeSeqNum  BIGINT,  verbLocalChangeSeqNum  BIGINT,  verbLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (66, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_66_fn() RETURNS trigger AS $$ BEGIN UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.verbLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 66) END),verbMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 66) ELSE NEW.verbMasterChangeSeqNum END) WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 66; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_66_trig AFTER UPDATE OR INSERT ON ContextXObjectStatementJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_66_fn()");
                        //END Create ContextXObjectStatementJoin (PostgreSQL)

                         //BEGIN Create AgentEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_68 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  AgentEntity  ( agentUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_68') ,  agentMbox  TEXT,  agentMbox_sha1sum  TEXT,  agentOpenid  TEXT,  agentAccountName  TEXT,  agentHomePage  TEXT,  agentPersonUid  BIGINT,  statementMasterChangeSeqNum  BIGINT,  statementLocalChangeSeqNum  BIGINT,  statementLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (68, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_68_fn() RETURNS trigger AS $$ BEGIN UPDATE AgentEntity SET statementLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.statementLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 68) END),statementMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 68) ELSE NEW.statementMasterChangeSeqNum END) WHERE agentUid = NEW.agentUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 68; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_68_trig AFTER UPDATE OR INSERT ON AgentEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_68_fn()");
                        //END Create AgentEntity (PostgreSQL)

                         //BEGIN Create StateEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_70 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  StateEntity  ( stateUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_70') ,  stateId  TEXT,  agentUid  BIGINT,  activityId  TEXT,  registration  TEXT,  isactive  BOOL,  timestamp  BIGINT,  stateMasterChangeSeqNum  BIGINT,  stateLocalChangeSeqNum  BIGINT,  stateLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (70, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_70_fn() RETURNS trigger AS $$ BEGIN UPDATE StateEntity SET stateLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.stateLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 70) END),stateMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 70) ELSE NEW.stateMasterChangeSeqNum END) WHERE stateUid = NEW.stateUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 70; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_70_trig AFTER UPDATE OR INSERT ON StateEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_70_fn()");
                        //END Create StateEntity (PostgreSQL)

                         //BEGIN Create StateContentEntity (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_72 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  StateContentEntity  ( stateContentUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_72') ,  stateContentStateUid  BIGINT,  stateContentKey  TEXT,  stateContentValue  TEXT,  isactive  BOOL,  stateContentMasterChangeSeqNum  BIGINT,  stateContentLocalChangeSeqNum  BIGINT,  stateContentLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (72, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_72_fn() RETURNS trigger AS $$ BEGIN UPDATE StateContentEntity SET stateContentLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.stateContentLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 72) END),stateContentMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 72) ELSE NEW.stateContentMasterChangeSeqNum END) WHERE stateContentUid = NEW.stateContentUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 72; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_72_trig AFTER UPDATE OR INSERT ON StateContentEntity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_72_fn()");
                        //END Create StateContentEntity (PostgreSQL)

                         break;


                 }
            }
        });

          return builder
      }

      @Synchronized
      private fun addCallbacks(
              builder: AbstractDoorwayDbBuilder<UmAppDatabase>): AbstractDoorwayDbBuilder<UmAppDatabase> {

          return builder
      } */
    }

}
