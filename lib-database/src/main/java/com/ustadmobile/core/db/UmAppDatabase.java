package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.AccessTokenDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.ConnectivityStatusDao;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContainerEntryDao;
import com.ustadmobile.core.db.dao.ContainerEntryFileDao;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.HttpCachedEntryDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.core.db.dao.LocationAncestorJoinDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.PersonLocationJoinDao;
import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
import com.ustadmobile.lib.database.UmDbBuilder;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DoorDbAdapter;
import com.ustadmobile.lib.db.DoorUtils;
import com.ustadmobile.lib.db.UmDbMigration;
import com.ustadmobile.lib.db.UmDbType;
import com.ustadmobile.lib.db.UmDbWithAttachmentsDir;
import com.ustadmobile.lib.db.UmDbWithAuthenticator;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.LocationAncestorJoin;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonCustomField;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.PersonLocationJoin;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.Hashtable;
import java.util.Random;


@UmDatabase(version = 16, entities = {
        DownloadSet.class,
        DownloadSetItem.class, NetworkNode.class, EntryStatusResponse.class,
        DownloadJobItemHistory.class,
        HttpCachedEntry.class, DownloadJob.class, DownloadJobItem.class,
        Person.class, Clazz.class, ClazzMember.class,
        PersonCustomField.class, PersonCustomFieldValue.class,
        ContentEntry.class, ContentEntryContentCategoryJoin.class,
        ContentEntryContentEntryFileJoin.class, ContentEntryFile.class,
        ContentEntryParentChildJoin.class, ContentEntryRelatedEntryJoin.class,
        ContentEntryFileStatus.class, ContentCategorySchema.class,
        ContentCategory.class, Language.class, LanguageVariant.class,
        SyncStatus.class, SyncablePrimaryKey.class, SyncDeviceBits.class,
        AccessToken.class, PersonAuth.class, Role.class, EntityRole.class,
        PersonGroup.class, PersonGroupMember.class, Location.class, LocationAncestorJoin.class,
        PersonLocationJoin.class, PersonPicture.class, ScrapeQueueItem.class, ScrapeRun.class,
        ContentEntryStatus.class, ConnectivityStatus.class,
        Container.class, ContainerEntry.class, ContainerEntryFile.class
})
public abstract class UmAppDatabase implements UmSyncableDatabase, UmDbWithAuthenticator,
        UmDbWithAttachmentsDir {

    private static volatile UmAppDatabase instance;

    private static volatile Hashtable<String, UmAppDatabase> namedInstances = new Hashtable<>();

    private boolean master;

    private String attachmentsDir;

    /**
     * For use by other projects using this app as a library. By calling setInstance before
     * any other usage (e.g. in the Android Application class) a child class of this database (eg.
     * with additional entities) can be used.
     *
     * @param instance
     */
    public static synchronized void setInstance(UmAppDatabase instance) {
        UmAppDatabase.instance = instance;
    }

    /**
     * For use by other projects using this app as a library. By calling setInstance before
     * any other usage (e.g. in the Android Application class) a child class of this database (eg.
     * with additional entities) can be used.
     *
     * @param instance
     * @param dbName
     */
    public static synchronized void setInstance(UmAppDatabase instance, String dbName) {
        namedInstances.put(dbName, instance);
    }

    public static synchronized UmAppDatabase getInstance(Object context) {
        if (instance == null) {
            AbstractDoorwayDbBuilder<UmAppDatabase> builder = UmDbBuilder
                    .builder(UmAppDatabase.class, context);
            builder = addMigrations(builder);
            instance = addCallbacks(builder).build();
        }

        return instance;
    }

    public static synchronized UmAppDatabase getInstance(Object context, String dbName) {
        UmAppDatabase db = namedInstances.get(dbName);

        if (db == null) {
            AbstractDoorwayDbBuilder<UmAppDatabase> builder = UmDbBuilder.builder(
                    UmAppDatabase.class, context, dbName);
            builder = addMigrations(builder);
            db = addCallbacks(builder).build();
            namedInstances.put(dbName, db);
        }
        return db;
    }

    private static AbstractDoorwayDbBuilder<UmAppDatabase> addMigrations(
            AbstractDoorwayDbBuilder<UmAppDatabase> builder) {
        builder.addMigration(new UmDbMigration(1, 2) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        //Must use new device bits, otherwise
                        int deviceBits = new Random().nextInt();

                        db.execSql("ALTER TABLE SyncDeviceBits ADD COLUMN master BOOL");
                        db.execSql("UPDATE SyncDeviceBits SET deviceBits = " + deviceBits +
                                ", master = TRUE");

                        db.execSql("CREATE TABLE IF NOT EXISTS  " +
                                "ScrapeRun  ( scrapeRunUid  SERIAL PRIMARY KEY  NOT NULL ,  " +
                                "scrapeType  TEXT,  status  INTEGER)");
                        db.execSql("CREATE TABLE IF NOT EXISTS  ScrapeQueueItem  " +
                                "( sqiUid  SERIAL PRIMARY KEY  NOT NULL ,  sqiContentEntryParentUid  BIGINT,  " +
                                "destDir  TEXT,  scrapeUrl  TEXT,  status  INTEGER,  runId  INTEGER,  time  TEXT, " +
                                " itemType  INTEGER,  contentType  TEXT)");


                        db.execSql("ALTER TABLE SyncStatus ADD COLUMN nextChangeSeqNum BIGINT");
                        db.execSql("ALTER TABLE SyncStatus DROP COLUMN masterchangeseqnum");
                        db.execSql("ALTER TABLE SyncStatus DROP COLUMN localchangeseqnum ");
                        db.execSql("DELETE FROM SyncStatus");

                        db.execSql("DROP TRIGGER IF EXISTS  increment_csn_clazz_trigger ON clazz");
                        db.execSql("DROP FUNCTION IF EXISTS  increment_csn_clazz_fn");

                        db.execSql("DROP TRIGGER IF EXISTS  increment_csn_clazzmember_trigger ON clazzmember");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzmember_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contentcategory_trigger ON contentcategory");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategory_fn");

                        db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contentcategoryschema_trigger ON contentcategoryschema");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategoryschema_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententry_trigger ON contententry");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententry_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententrycontentcategoryjoin_trigger ON contententrycontentcategoryjoin");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontentcategoryjoin_fn");

                        db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contententrycontententryfilejoin_trigger ON contententrycontententryfilejoin");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontententryfilejoin_fn");

                        db.execSql("DROP TRIGGER IF EXISTS  increment_csn_contententryfile_trigger ON contententryfile");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryfile_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryparentchildjoin_trigger ON contententryparentchildjoin");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryparentchildjoin_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryrelatedentryjoin_trigger ON contententryrelatedentryjoin");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryrelatedentryjoin_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_entityrole_trigger ON entityrole");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_entityrole_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_language_trigger ON language");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_language_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_languagevariant_trigger ON languagevariant");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_languagevariant_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_entityrole_trigger ON entityrole");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_entityrole_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_location_trigger ON location");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_location_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_person_trigger ON person");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_person_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personauth_trigger ON personauth");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personauth_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroup_trigger ON persongroup");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroup_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroupmember_trigger ON persongroupmember");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroupmember_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personlocationjoin_trigger ON personlocationjoin");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personlocationjoin_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personpicture_trigger ON personpicture");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personpicture_fn");

                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_role_trigger ON role");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_role_fn");

                        db.execSql("CREATE SEQUENCE spk_seq_42 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntry ALTER COLUMN contentEntryUid SET DEFAULT NEXTVAL('spk_seq_42')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (42, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_42_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) END),contentEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) ELSE NEW.contentEntryMasterChangeSeqNum END) WHERE contentEntryUid = NEW.contentEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 42; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_42_trig AFTER UPDATE OR INSERT ON ContentEntry FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_42_fn()");

                        db.execSql("CREATE SEQUENCE spk_seq_3 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntryContentCategoryJoin ALTER COLUMN ceccjUid SET DEFAULT NEXTVAL('spk_seq_3')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (3, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_3_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.ceccjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) END),ceccjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) ELSE NEW.ceccjMasterChangeSeqNum END) WHERE ceccjUid = NEW.ceccjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 3; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_3_trig AFTER UPDATE OR INSERT ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_3_fn()");

                        db.execSql("CREATE SEQUENCE spk_seq_4 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntryContentEntryFileJoin ALTER COLUMN cecefjUid SET DEFAULT NEXTVAL('spk_seq_4')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (4, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_4_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentEntryFileJoin SET cecefjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cecefjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) END),cecefjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) ELSE NEW.cecefjMasterChangeSeqNum END) WHERE cecefjUid = NEW.cecefjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 4; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_4_trig AFTER UPDATE OR INSERT ON ContentEntryContentEntryFileJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_4_fn()");
                        //END Create ContentEntryContentEntryFileJoin (PostgreSQL)

                        //BEGIN Create ContentEntryFile (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_5 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntryFile ALTER COLUMN contentEntryFileUid SET DEFAULT NEXTVAL('spk_seq_5')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (5, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_5_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryFile SET contentEntryFileLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryFileLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) END),contentEntryFileMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) ELSE NEW.contentEntryFileMasterChangeSeqNum END) WHERE contentEntryFileUid = NEW.contentEntryFileUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 5; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_5_trig AFTER UPDATE OR INSERT ON ContentEntryFile FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_5_fn()");
                        //END Create ContentEntryFile (PostgreSQL)

                        //BEGIN Create ContentEntryParentChildJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_7 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntryParentChildJoin ALTER COLUMN cepcjUid SET DEFAULT NEXTVAL('spk_seq_7')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (7, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_7_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cepcjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) END),cepcjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) ELSE NEW.cepcjMasterChangeSeqNum END) WHERE cepcjUid = NEW.cepcjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 7; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_7_trig AFTER UPDATE OR INSERT ON ContentEntryParentChildJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_7_fn()");
                        //END Create ContentEntryParentChildJoin (PostgreSQL)

                        //BEGIN Create ContentEntryRelatedEntryJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_8 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentEntryRelatedEntryJoin ALTER COLUMN cerejUid SET DEFAULT NEXTVAL('spk_seq_8')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (8, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_8_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cerejLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) END),cerejMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) ELSE NEW.cerejMasterChangeSeqNum END) WHERE cerejUid = NEW.cerejUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 8; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_8_trig AFTER UPDATE OR INSERT ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_8_fn()");
                        //END Create ContentEntryRelatedEntryJoin (PostgreSQL)

                        db.execSql("CREATE SEQUENCE spk_seq_2 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ContentCategorySchema ALTER COLUMN contentCategorySchemaUid SET DEFAULT NEXTVAL('spk_seq_2')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (2, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_2_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategorySchemaLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) END),contentCategorySchemaMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) ELSE NEW.contentCategorySchemaMasterChangeSeqNum END) WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 2; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_2_trig AFTER UPDATE OR INSERT ON ContentCategorySchema FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_2_fn()");
                        //END Create ContentCategorySchema (PostgreSQL)

                        //BEGIN Create ContentCategory (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_1 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE  ContentCategory ALTER COLUMN contentCategoryUid SET DEFAULT NEXTVAL('spk_seq_1')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (1, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_1_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategoryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) END),contentCategoryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) ELSE NEW.contentCategoryMasterChangeSeqNum END) WHERE contentCategoryUid = NEW.contentCategoryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 1; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_1_trig AFTER UPDATE OR INSERT ON ContentCategory FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_1_fn()");
                        //END Create ContentCategory (PostgreSQL)

                        //BEGIN Create Language (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_13 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE Language ALTER COLUMN langUid SET DEFAULT NEXTVAL('spk_seq_13')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (13, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_13_fn() RETURNS trigger AS $$ BEGIN UPDATE Language SET langLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) END),langMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) ELSE NEW.langMasterChangeSeqNum END) WHERE langUid = NEW.langUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 13; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_13_trig AFTER UPDATE OR INSERT ON Language FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_13_fn()");
                        //END Create Language (PostgreSQL)

                        //BEGIN Create LanguageVariant (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_10 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE LanguageVariant  ALTER COLUMN langVariantUid  SET DEFAULT NEXTVAL('spk_seq_10')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (10, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_10_fn() RETURNS trigger AS $$ BEGIN UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langVariantLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) END),langVariantMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) ELSE NEW.langVariantMasterChangeSeqNum END) WHERE langVariantUid = NEW.langVariantUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 10; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_10_trig AFTER UPDATE OR INSERT ON LanguageVariant FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_10_fn()");
                        //END Create LanguageVariant (PostgreSQL)

                        //BEGIN Create Person (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_9 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE  Person ALTER COLUMN personUid SET DEFAULT NEXTVAL('spk_seq_9')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (9, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_9_fn() RETURNS trigger AS $$ BEGIN UPDATE Person SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) ELSE NEW.personMasterChangeSeqNum END) WHERE personUid = NEW.personUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 9; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_9_trig AFTER UPDATE OR INSERT ON Person FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_9_fn()");
                        //END Create Person (PostgreSQL)

                        //BEGIN Create Clazz (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_6 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE Clazz ALTER COLUMN clazzUid SET DEFAULT NEXTVAL('spk_seq_6')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (6, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_6_fn() RETURNS trigger AS $$ BEGIN UPDATE Clazz SET clazzLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) END),clazzMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) ELSE NEW.clazzMasterChangeSeqNum END) WHERE clazzUid = NEW.clazzUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 6; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_6_trig AFTER UPDATE OR INSERT ON Clazz FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_6_fn()");
                        //END Create Clazz (PostgreSQL)

                        //BEGIN Create ClazzMember (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_11 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE ClazzMember ALTER COLUMN clazzMemberUid SET DEFAULT NEXTVAL('spk_seq_11')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (11, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_11_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzMemberLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) END),clazzMemberMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) ELSE NEW.clazzMemberMasterChangeSeqNum END) WHERE clazzMemberUid = NEW.clazzMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 11; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_11_trig AFTER UPDATE OR INSERT ON ClazzMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_11_fn()");
                        //END Create ClazzMember (PostgreSQL)

                        //BEGIN Create PersonAuth (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_30 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE PersonAuth  ALTER COLUMN personAuthUid SET DEFAULT NEXTVAL('spk_seq_30')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (30, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_30_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonAuth SET personAuthLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personAuthLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) END),personAuthMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) ELSE NEW.personAuthMasterChangeSeqNum END) WHERE personAuthUid = NEW.personAuthUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 30; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_30_trig AFTER UPDATE OR INSERT ON PersonAuth FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_30_fn()");
                        //END Create PersonAuth (PostgreSQL)

                        //BEGIN Create Role (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_45 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE Role ALTER COLUMN roleUid SET  DEFAULT NEXTVAL('spk_seq_45')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (45, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_45_fn() RETURNS trigger AS $$ BEGIN UPDATE Role SET roleLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.roleLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) END),roleMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) ELSE NEW.roleMasterCsn END) WHERE roleUid = NEW.roleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 45; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_45_trig AFTER UPDATE OR INSERT ON Role FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_45_fn()");
                        //END Create Role (PostgreSQL)

                        //BEGIN Create EntityRole (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_47 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE EntityRole ALTER COLUMN erUid SET DEFAULT NEXTVAL('spk_seq_47')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (47, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_47_fn() RETURNS trigger AS $$ BEGIN UPDATE EntityRole SET erLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.erLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) END),erMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) ELSE NEW.erMasterCsn END) WHERE erUid = NEW.erUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 47; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_47_trig AFTER UPDATE OR INSERT ON EntityRole FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_47_fn()");
                        //END Create EntityRole (PostgreSQL)

                        //BEGIN Create PersonGroup (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_43 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE  PersonGroup ALTER COLUMN  groupUid SET DEFAULT NEXTVAL('spk_seq_43')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (43, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_43_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroup SET groupLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) END),groupMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) ELSE NEW.groupMasterCsn END) WHERE groupUid = NEW.groupUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 43; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_43_trig AFTER UPDATE OR INSERT ON PersonGroup FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_43_fn()");
                        //END Create PersonGroup (PostgreSQL)

                        //BEGIN Create PersonGroupMember (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_44 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE  PersonGroupMember  ALTER COLUMN groupMemberUid SET DEFAULT NEXTVAL('spk_seq_44')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (44, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_44_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroupMember SET groupMemberLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupMemberLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) END),groupMemberMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) ELSE NEW.groupMemberMasterCsn END) WHERE groupMemberUid = NEW.groupMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 44; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_44_trig AFTER UPDATE OR INSERT ON PersonGroupMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_44_fn()");
                        //END Create PersonGroupMember (PostgreSQL)

                        //BEGIN Create Location (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_29 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE  Location  ALTER COLUMN locationUid  SET DEFAULT NEXTVAL('spk_seq_29')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (29, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_29_fn() RETURNS trigger AS $$ BEGIN UPDATE Location SET locationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.locationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) END),locationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) ELSE NEW.locationMasterChangeSeqNum END) WHERE locationUid = NEW.locationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 29; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_29_trig AFTER UPDATE OR INSERT ON Location FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_29_fn()");
                        //END Create Location (PostgreSQL)

                        //BEGIN Create PersonLocationJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_48 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE PersonLocationJoin  ALTER COLUMN  personLocationUid SET DEFAULT NEXTVAL('spk_seq_48')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (48, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_48_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonLocationJoin SET plLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.plLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) END),plMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) ELSE NEW.plMasterCsn END) WHERE personLocationUid = NEW.personLocationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 48; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_48_trig AFTER UPDATE OR INSERT ON PersonLocationJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_48_fn()");
                        //END Create PersonLocationJoin (PostgreSQL)

                        //BEGIN Create PersonPicture (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_50 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("ALTER TABLE PersonPicture  ALTER COLUMN personPictureUid  SET DEFAULT NEXTVAL('spk_seq_50')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (50, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_50_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonPicture SET personPictureLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personPictureLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) END),personPictureMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) ELSE NEW.personPictureMasterCsn END) WHERE personPictureUid = NEW.personPictureUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 50; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_50_trig AFTER UPDATE OR INSERT ON PersonPicture FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_50_fn()");
                        //END Create PersonPicture (PostgreSQL)

                }
            }
        });
        builder.addMigration(new UmDbMigration(2, 4) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        //Must use new device bits, otherwise
                        db.execSql("ALTER TABLE ScrapeQueueItem " +
                                "ADD COLUMN timeAdded BIGINT DEFAULT 0, " +
                                "ADD COLUMN timeStarted BIGINT DEFAULT 0, " +
                                "ADD COLUMN timeFinished BIGINT DEFAULT 0, " +
                                "DROP COLUMN time "
                        );
                }
            }
        });

        builder.addMigration(new UmDbMigration(4, 6) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        //Must use new device bits, otherwise
                        //BEGIN Create ContentEntryStatus (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryStatus  ( cesUid  BIGINT PRIMARY KEY  NOT NULL ,  totalSize  BIGINT,  bytesDownloadSoFar  BIGINT,  downloadStatus  INTEGER,  invalidated  BOOL,  cesLeaf  BOOL)");
                        //END Create ContentEntryStatus (PostgreSQL)

                        //BEGIN Create ConnectivityStatus (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ConnectivityStatus  ( csUid  INTEGER PRIMARY KEY  NOT NULL ,  connectivityState  INTEGER,  wifiSsid  TEXT,  connectedOrConnecting  BOOL)");
                        //END Create ConnectivityStatus (PostgreSQL)

                        db.execSql("DROP TABLE  IF EXISTS DownloadJob");
                        //BEGIN Create DownloadJob (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJob  ( djUid  SERIAL PRIMARY KEY  NOT NULL ,  djDsUid  INTEGER,  timeCreated  BIGINT,  timeRequested  BIGINT,  timeCompleted  BIGINT,  djStatus  INTEGER)");
                        //END Create DownloadJob (PostgreSQL)

                        db.execSql("DROP TABLE  IF EXISTS DownloadJobItem");
                        //BEGIN Create DownloadJobItem (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJobItem  ( djiUid  SERIAL PRIMARY KEY  NOT NULL ,  djiDsiUid  INTEGER,  djiDjUid  INTEGER,  djiContentEntryFileUid  BIGINT,  downloadedSoFar  BIGINT,  downloadLength  BIGINT,  currentSpeed  BIGINT,  timeStarted  BIGINT,  timeFinished  BIGINT,  djiStatus  INTEGER,  destinationFile  TEXT,  numAttempts  INTEGER)");
                        db.execSql("CREATE INDEX  index_DownloadJobItem_djiStatus  ON  DownloadJobItem  ( djiStatus  )");
                        //END Create DownloadJobItem (PostgreSQL)

                        db.execSql("DROP TABLE  IF EXISTS DownloadJobItemHistory");
                        //BEGIN Create DownloadJobItemHistory (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJobItemHistory  ( id  SERIAL PRIMARY KEY  NOT NULL ,  url  TEXT,  networkNode  BIGINT,  downloadJobItemId  INTEGER,  mode  INTEGER,  numBytes  BIGINT,  successful  BOOL,  startTime  BIGINT,  endTime  BIGINT)");
                        //END Create DownloadJobItemHistory (PostgreSQL)

                        db.execSql("DROP TABLE  IF EXISTS DownloadSet");
                        //BEGIN Create DownloadSet (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSet  ( dsUid  SERIAL PRIMARY KEY  NOT NULL ,  destinationDir  TEXT,  meteredNetworkAllowed  BOOL,  dsRootContentEntryUid  BIGINT)");
                        //END Create DownloadSet (PostgreSQL)

                        db.execSql("DROP TABLE  IF EXISTS DownloadSetItem");
                        //BEGIN Create DownloadSetItem (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSetItem  ( dsiUid  SERIAL PRIMARY KEY  NOT NULL ,  dsiDsUid  INTEGER,  dsiContentEntryUid  BIGINT)");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiContentEntryUid  ON  DownloadSetItem  ( dsiContentEntryUid  )");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiDsUid  ON  DownloadSetItem  ( dsiDsUid  )");
                        //END Create DownloadSetItem (PostgreSQL)


                }
            }
        });

        builder.addMigration(new UmDbMigration(6, 8) {
            @Override
            public void migrate(DoorDbAdapter db) {
                db.execSql("DROP TABLE IF EXISTS CrawlJob");
                db.execSql("DROP TABLE IF EXISTS CrawlJobItem");
                db.execSql("DROP TABLE IF EXISTS OpdsEntry");
                db.execSql("DROP TABLE IF EXISTS OpdsEntryParentToChildJoin");
                db.execSql("DROP TABLE IF EXISTS OpdsEntryRelative");
                db.execSql("DROP TABLE IF EXISTS OpdsEntryStatusCache");
                db.execSql("DROP TABLE IF EXISTS OpdsEntryStatusCacheAncestor");
                db.execSql("DROP TABLE IF EXISTS OpdsLink");
            }
        });
        builder.addMigration(new UmDbMigration(8, 10) {
            @Override
            public void migrate(DoorDbAdapter db) {

                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        db.execSql("ALTER TABLE ContentEntry ADD COLUMN contentTypeFlag INTEGER DEFAULT 0");

                }
            }
        });

        builder.addMigration(new UmDbMigration(10, 12) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        int deviceBits = Integer.parseInt(
                                db.selectSingleValue("SELECT deviceBits FROM SyncDeviceBits"));


                        db.execSql("ALTER TABLE ContentEntryContentEntryFileJoin ADD COLUMN cecefjContainerUid BIGINT");
                        db.execSql("CREATE INDEX  index_ContentEntryContentEntryFileJoin_cecefjContainerUid  ON  ContentEntryContentEntryFileJoin  ( cecefjContainerUid  )");


                        // BEGIN Create Container
                        db.execSql("CREATE SEQUENCE spk_seq_51 " + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  Container  ( containerUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_51') ,  cntLocalCsn  BIGINT,  cntMasterCsn  BIGINT,  cntLastModBy  INTEGER,  fileSize  BIGINT,  containerContentEntryUid  BIGINT,  lastModified  BIGINT,  mimeType  TEXT,  remarks  TEXT,  mobileOptimized  BOOL)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (51, 1, 0, 0)");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_51_fn() RETURNS trigger AS $$ BEGIN UPDATE Container SET cntLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cntLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 51) END),cntMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 51) ELSE NEW.cntMasterCsn END) WHERE containerUid = NEW.containerUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 51; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_51_trig AFTER UPDATE OR INSERT ON Container FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_51_fn()");
                        db.execSql("CREATE INDEX  index_Container_lastModified  ON  Container  ( lastModified  )");
                        //END Create Container (

                        //BEGIN Create ContainerEntry
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContainerEntry  ( ceUid  SERIAL PRIMARY KEY  NOT NULL ,  cePath  TEXT,  ceCefUid  BIGINT)");
                        //END Create ContainerEntry (PostgreSQL)

                        //BEGIN Create ContainerEntryFile
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContainerEntryFile  ( cefUid  SERIAL PRIMARY KEY  NOT NULL ,  cefMd5  TEXT,  cefPath  TEXT,  ceTotalSize  BIGINT,  ceCompressedSize  BIGINT,  compression  INTEGER)");
                        //END Create ContainerEntryFile

                }
            }
        });

        builder.addMigration(new UmDbMigration(12, 14) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        db.execSql("ALTER TABLE ContainerEntry ADD COLUMN ceContainerUid BIGINT");
                        db.execSql("CREATE INDEX  index_ContainerEntry_ceContainerUid  ON  ContainerEntry  ( ceContainerUid  )");


                }
            }
        });

        builder.addMigration(new UmDbMigration(14, 16) {
            @Override
            public void migrate(DoorDbAdapter db) {
                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:
                        throw new RuntimeException("Not supported on SQLite");

                    case UmDbType.TYPE_POSTGRES:
                        db.execSql("ALTER TABLE Container ADD COLUMN cntNumEntries INTEGER");
                        break;


                }
            }
        });

        return builder;
    }

    private static synchronized AbstractDoorwayDbBuilder<UmAppDatabase> addCallbacks(
            AbstractDoorwayDbBuilder<UmAppDatabase> builder) {

        return builder;
    }

    public abstract NetworkNodeDao getNetworkNodeDao();

    public abstract EntryStatusResponseDao getEntryStatusResponseDao();

    public abstract DownloadSetDao getDownloadSetDao();

    public abstract DownloadSetItemDao getDownloadSetItemDao();

    public abstract DownloadJobDao getDownloadJobDao();

    public abstract DownloadJobItemDao getDownloadJobItemDao();

    public abstract DownloadJobItemHistoryDao getDownloadJobItemHistoryDao();

    public abstract HttpCachedEntryDao getHttpCachedEntryDao();

    public abstract PersonDao getPersonDao();

    public abstract ClazzDao getClazzDao();

    public abstract ClazzMemberDao getClazzMemberDao();

    public abstract ContentEntryDao getContentEntryDao();

    public abstract PersonCustomFieldDao getPersonCustomFieldDao();

    public abstract PersonCustomFieldValueDao getPersonCustomFieldValueDao();


    public abstract ContentEntryContentCategoryJoinDao getContentEntryContentCategoryJoinDao();

    public abstract ContentEntryContentEntryFileJoinDao getContentEntryContentEntryFileJoinDao();

    public abstract ContentEntryFileDao getContentEntryFileDao();

    public abstract ContentEntryParentChildJoinDao getContentEntryParentChildJoinDao();

    public abstract ContentEntryRelatedEntryJoinDao getContentEntryRelatedEntryJoinDao();

    public abstract SyncStatusDao getSyncStatusDao();

    public abstract ContentEntryFileStatusDao getContentEntryFileStatusDao();

    public abstract ContentCategorySchemaDao getContentCategorySchemaDao();

    public abstract ContentCategoryDao getContentCategoryDao();

    public abstract LanguageDao getLanguageDao();

    public abstract LanguageVariantDao getLanguageVariantDao();

    public abstract ScrapeQueueItemDao getScrapeQueueItemDao();

    public abstract PersonAuthDao getPersonAuthDao();

    public abstract AccessTokenDao getAccessTokenDao();

    public abstract RoleDao getRoleDao();

    public abstract PersonGroupDao getPersonGroupDao();

    public abstract PersonGroupMemberDao getPersonGroupMemberDao();

    public abstract EntityRoleDao getEntityRoleDao();

    public abstract LocationDao getLocationDao();

    public abstract LocationAncestorJoinDao getLocationAncestorJoinDao();

    public abstract PersonLocationJoinDao getPersonLocationJoinDao();

    public abstract PersonPictureDao getPersonPictureDao();

    public abstract ScrapeRunDao getScrapeRunDao();

    public abstract ContentEntryStatusDao getContentEntryStatusDao();

    public abstract ConnectivityStatusDao getConnectivityStatusDao();

    public abstract ContainerDao getContainerDao();

    public abstract ContainerEntryDao getContainerEntryDao();

    public abstract ContainerEntryFileDao getContainerEntryFileDao();

    @UmDbContext
    public abstract Object getContext();

    @UmClearAll
    public abstract void clearAllTables();


    @Override
    public abstract SyncablePrimaryKeyDao getSyncablePrimaryKeyDao();

    @Override
    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    @UmRepository
    public abstract UmAppDatabase getRepository(String baseUrl, String auth);

    @UmSyncOutgoing
    public abstract void syncWith(UmAppDatabase otherDb, long accountUid, int sendLimit, int receiveLimit);


    @Override
    public boolean validateAuth(long personUid, String auth) {
        if (personUid == 0)
            return true;//Anonymous or guest access

        return getAccessTokenDao().isValidToken(personUid, auth);
    }

    @Override
    public int getDeviceBits() {
        return getSyncablePrimaryKeyDao().getDeviceBits();
    }

    @Override
    public void invalidateDeviceBits() {
        getSyncablePrimaryKeyDao().invalidateDeviceBits();
    }

    @Override
    public String getAttachmentsDir() {
        return attachmentsDir;
    }

    public void setAttachmentsDir(String attachmentsDir) {
        this.attachmentsDir = attachmentsDir;
    }

    @UmSyncCountLocalPendingChanges
    public abstract int countPendingLocalChanges(long accountUid, int deviceId);

}
