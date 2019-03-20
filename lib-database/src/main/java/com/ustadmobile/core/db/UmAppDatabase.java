package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.AccessTokenDao;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.CrawJoblItemDao;
import com.ustadmobile.core.db.dao.CrawlJobDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.HolidayDao;
import com.ustadmobile.core.db.dao.HttpCachedEntryDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.core.db.dao.LocationAncestorJoinDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheAncestorDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.PersonLocationJoinDao;
import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.db.dao.ScheduledCheckDao;
import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.core.db.dao.SelQuestionOptionDao;
import com.ustadmobile.core.db.dao.SelQuestionResponseDao;
import com.ustadmobile.core.db.dao.SelQuestionResponseNominationDao;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao;
import com.ustadmobile.core.db.dao.UMCalendarDao;
import com.ustadmobile.lib.database.UmDbBuilder;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DoorDbAdapter;
import com.ustadmobile.lib.db.UmDbMigration;
import com.ustadmobile.lib.db.UmDbType;
import com.ustadmobile.lib.db.UmDbWithAttachmentsDir;
import com.ustadmobile.lib.db.UmDbWithAuthenticator;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Holiday;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.LocationAncestorJoin;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.PersonLocationJoin;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.ScheduledCheck;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.entities.SelQuestionResponse;
import com.ustadmobile.lib.db.entities.SelQuestionOption;
import com.ustadmobile.lib.db.entities.SelQuestionResponseNomination;
import com.ustadmobile.lib.db.entities.SelQuestionSet;
import com.ustadmobile.lib.db.entities.SelQuestionSetRecognition;
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse;
import com.ustadmobile.lib.db.entities.UMCalendar;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.Hashtable;
import java.util.Random;

@UmDatabase(version = 3, entities = {
        OpdsEntry.class, OpdsLink.class, OpdsEntryParentToChildJoin.class,
        ContainerFile.class, ContainerFileEntry.class, DownloadSet.class,
        DownloadSetItem.class, NetworkNode.class, EntryStatusResponse.class,
        DownloadJobItemHistory.class, CrawlJob.class, CrawlJobItem.class,
        OpdsEntryStatusCache.class, OpdsEntryStatusCacheAncestor.class,
        HttpCachedEntry.class, DownloadJob.class, DownloadJobItem.class,
        Person.class, Clazz.class, ClazzMember.class, ClazzLog.class,
        ClazzLogAttendanceRecord.class, FeedEntry.class,
        PersonField.class, PersonCustomFieldValue.class,
        PersonDetailPresenterField.class,
        SelQuestion.class, SelQuestionResponse.class,
        SelQuestionResponseNomination.class, SelQuestionSet.class,
        SelQuestionSetRecognition.class, SelQuestionSetResponse.class,
        Schedule.class, Holiday.class, UMCalendar.class,
        ClazzActivity.class, ClazzActivityChange.class,
        ContentEntry.class, ContentEntryContentCategoryJoin.class,
        ContentEntryContentEntryFileJoin.class, ContentEntryFile.class,
        ContentEntryParentChildJoin.class, ContentEntryRelatedEntryJoin.class,
        Location.class, ContentEntryFileStatus.class, ContentCategorySchema.class,
        ContentCategory.class, Language.class, LanguageVariant.class,
        SyncStatus.class, SyncablePrimaryKey.class, SyncDeviceBits.class,
        AccessToken.class, PersonAuth.class, Role.class, EntityRole.class,
        PersonGroup.class, PersonGroupMember.class, LocationAncestorJoin.class,
        PersonLocationJoin.class, PersonPicture.class,
        SelQuestionOption.class, ScheduledCheck.class
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
     * @param instance  The database instance
     */
    public static synchronized void setInstance(UmAppDatabase instance) {
        UmAppDatabase.instance = instance;
    }

    /**
     * For use by other projects using this app as a library. By calling setInstance before
     * any other usage (e.g. in the Android Application class) a child class of this database (eg.
     * with additional entities) can be used.

     * @param instance
     * @param dbName
     */
    public static synchronized void setInstance(UmAppDatabase instance, String dbName) {
        namedInstances.put(dbName, instance);
    }

    public static synchronized UmAppDatabase getInstance(Object context) {
        if(instance == null){
            AbstractDoorwayDbBuilder<UmAppDatabase> builder = UmDbBuilder
                    .builder(UmAppDatabase.class, context);
            builder = addMigrations(builder);
            instance = addCallbacks(builder).build();
        }
            //instance = UmDbBuilder.builder(UmAppDatabase.class, context).build();

        return instance;
    }

    public static synchronized UmAppDatabase getInstance(Object context, String dbName) {
        UmAppDatabase db = namedInstances.get(dbName);
        if(db == null){
//            db = UmDbBuilder.builder(UmAppDatabase.class, context, dbName).build();
//            namedInstances.put(dbName, db);
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
        builder.addMigration(new UmDbMigration(1, 3) {

            @Override
            public void migrate(DoorDbAdapter db) {

                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:

                        //db.execSql();

                        /*ClazzLog : added two fields
                            private boolean canceled;
                            private long clazzLogScheduleUid;
                        */
                        db.execSql("ALTER TABLE ClazzLog ADD canceled BOOL");
                        db.execSql("ALTER TABLE ClazzLog ADD clazzLogScheduleUid BIGINT");
                        db.execSql("DROP VIEW ClazzLog_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzLog_spk_view AS SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, done, canceled, numPresent, numAbsent, numPartial, clazzLogScheduleUid, clazzLogChangeMasterChangeSeqNum, clazzLogChangeLocalChangeSeqNum, clazzLogLastChangedBy FROM ClazzLog");

                        /*
                        FeedEntry : added two fields
                            private long feedEntryClazzLogUid;
                            private long dateCreated;
                        */
                        db.execSql("ALTER TABLE FeedEntry ADD feedEntryClazzLogUid  BIGINT");
                        db.execSql("ALTER TABLE FeedEntry ADD dateCreated BIGINT");
                        db.execSql("DROP VIEW FeedEntry_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS FeedEntry_spk_view AS SELECT feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy FROM FeedEntry");


                        /*
                        Location: added one fields
                            private String timeZone;
                        */
                        db.execSql("ALTER TABLE Location ADD timeZone TEXT");
                        db.execSql("DROP VIEW Location_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS Location_spk_view AS SELECT locationUid, title, locationDesc, lng, lat, parentLocationUid, timeZone, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy FROM Location");


                        /*
                        ScheduledCheck : New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  ScheduledCheck  ( scheduledCheckId  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL ,  checkTime  BIGINT,  checkType  INTEGER,  checkUuid  TEXT,  checkParameters  TEXT,  scClazzLogUid  BIGINT)");

                        /*
                        SelQuestion - New entity
                        */

                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestion  ( selQuestionUid  BIGINT PRIMARY KEY ,  questionText  TEXT,  selQuestionSelQuestionSetUid  BIGINT,  questionIndex  INTEGER,  assignToAllClasses  BOOL,  multiNominations  BOOL,  questionType  INTEGER,  questionActive  BOOL,  selQuestionMasterChangeSeqNum  BIGINT,  selQuestionLocalChangeSeqNum  BIGINT,  selQuestionLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (22, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (22, 1)");
                        db.execSql("CREATE TRIGGER upd_22 AFTER update ON SelQuestion FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionMasterChangeSeqNum = 0 OR OLD.selQuestionMasterChangeSeqNum = NEW.selQuestionMasterChangeSeqNum) ELSE (NEW.selQuestionLocalChangeSeqNum = 0 OR OLD.selQuestionLocalChangeSeqNum = NEW.selQuestionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END),selQuestionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END) WHERE selQuestionUid = NEW.selQuestionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 22; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestion_spk_view AS SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion");
                        db.execSql("CREATE TRIGGER ins_22 INSTEAD OF INSERT ON SelQuestion_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 22)) ELSE NEW.selQuestionUid END; INSERT INTO SelQuestion(selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.questionText, NEW.selQuestionSelQuestionSetUid, NEW.questionIndex, NEW.assignToAllClasses, NEW.multiNominations, NEW.questionType, NEW.questionActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END), NEW.selQuestionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL) AND tableId = 22; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 22; END");

                        /*
                        SelQuestionOption - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionOption  ( selQuestionOptionUid  BIGINT PRIMARY KEY ,  optionText  TEXT,  selQuestionOptionQuestionUid  BIGINT,  selQuestionOptionMasterChangeSeqNum  BIGINT,  selQuestionOptionLocalChangeSeqNum  BIGINT,  selQuestionOptionLastChangedBy  INTEGER,  optionActive  BOOL)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (52, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (52, 1)");
                        db.execSql("CREATE TRIGGER upd_52 AFTER update ON SelQuestionOption FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionOptionMasterChangeSeqNum = 0 OR OLD.selQuestionOptionMasterChangeSeqNum = NEW.selQuestionOptionMasterChangeSeqNum) ELSE (NEW.selQuestionOptionLocalChangeSeqNum = 0 OR OLD.selQuestionOptionLocalChangeSeqNum = NEW.selQuestionOptionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END),selQuestionOptionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END) WHERE selQuestionOptionUid = NEW.selQuestionOptionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 52; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionOption_spk_view AS SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption");
                        db.execSql("CREATE TRIGGER ins_52 INSTEAD OF INSERT ON SelQuestionOption_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 52)) ELSE NEW.selQuestionOptionUid END; INSERT INTO SelQuestionOption(selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.optionText, NEW.selQuestionOptionQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END), NEW.selQuestionOptionLastChangedBy, NEW.optionActive); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL) AND tableId = 52; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 52; END");

                        /*
                        SelQuestionResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionResponse  ( selQuestionResponseUid  BIGINT PRIMARY KEY ,  selQuestionResponseSelQuestionSetResponseUid  BIGINT,  selQuestionResponseSelQuestionUid  BIGINT,  selQuestionResponseMasterChangeSeqNum  BIGINT,  selQuestionResponseLocalChangeSeqNum  BIGINT,  selQuestionResponseLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (23, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (23, 1)");
                        db.execSql("CREATE TRIGGER upd_23 AFTER update ON SelQuestionResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseMasterChangeSeqNum = 0 OR OLD.selQuestionResponseMasterChangeSeqNum = NEW.selQuestionResponseMasterChangeSeqNum) ELSE (NEW.selQuestionResponseLocalChangeSeqNum = 0 OR OLD.selQuestionResponseLocalChangeSeqNum = NEW.selQuestionResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END),selQuestionResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END) WHERE selQuestionResponseUid = NEW.selQuestionResponseUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 23; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponse_spk_view AS SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse");
                        db.execSql("CREATE TRIGGER ins_23 INSTEAD OF INSERT ON SelQuestionResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 23)) ELSE NEW.selQuestionResponseUid END; INSERT INTO SelQuestionResponse(selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseSelQuestionSetResponseUid, NEW.selQuestionResponseSelQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END), NEW.selQuestionResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL) AND tableId = 23; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 23; END");

                        /*
                        SelQuestionResponseNomination - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionResponseNomination  ( selQuestionResponseNominationUid  BIGINT PRIMARY KEY ,  selQuestionResponseNominationClazzMemberUid  BIGINT,  selQuestionResponseNominationSelQuestionResponseUId  BIGINT,  nominationActive  BOOL,  selQuestionResponseNominationMasterChangeSeqNum  BIGINT,  selQuestionResponseNominationLocalChangeSeqNum  BIGINT,  selQuestionResponseNominationLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (24, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (24, 1)");
                        db.execSql("CREATE TRIGGER upd_24 AFTER update ON SelQuestionResponseNomination FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseNominationMasterChangeSeqNum = 0 OR OLD.selQuestionResponseNominationMasterChangeSeqNum = NEW.selQuestionResponseNominationMasterChangeSeqNum) ELSE (NEW.selQuestionResponseNominationLocalChangeSeqNum = 0 OR OLD.selQuestionResponseNominationLocalChangeSeqNum = NEW.selQuestionResponseNominationLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponseNomination SET selQuestionResponseNominationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END),selQuestionResponseNominationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END) WHERE selQuestionResponseNominationUid = NEW.selQuestionResponseNominationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 24; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponseNomination_spk_view AS SELECT selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy FROM SelQuestionResponseNomination");
                        db.execSql("CREATE TRIGGER ins_24 INSTEAD OF INSERT ON SelQuestionResponseNomination_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 24)) ELSE NEW.selQuestionResponseNominationUid END; INSERT INTO SelQuestionResponseNomination(selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseNominationClazzMemberUid, NEW.selQuestionResponseNominationSelQuestionResponseUId, NEW.nominationActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END), NEW.selQuestionResponseNominationLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL) AND tableId = 24; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 24; END");

                        /*
                        SelQuestionSet - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSet  ( selQuestionSetUid  BIGINT PRIMARY KEY ,  title  TEXT,  selQuestionSetMasterChangeSeqNum  BIGINT,  selQuestionSetLocalChangeSeqNum  BIGINT,  selQuestionSetLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (25, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (25, 1)");
                        db.execSql("CREATE TRIGGER upd_25 AFTER update ON SelQuestionSet FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetMasterChangeSeqNum = 0 OR OLD.selQuestionSetMasterChangeSeqNum = NEW.selQuestionSetMasterChangeSeqNum) ELSE (NEW.selQuestionSetLocalChangeSeqNum = 0 OR OLD.selQuestionSetLocalChangeSeqNum = NEW.selQuestionSetLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END),selQuestionSetMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END) WHERE selQuestionSetUid = NEW.selQuestionSetUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 25; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSet_spk_view AS SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet");
                        db.execSql("CREATE TRIGGER ins_25 INSTEAD OF INSERT ON SelQuestionSet_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 25)) ELSE NEW.selQuestionSetUid END; INSERT INTO SelQuestionSet(selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.title, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END), NEW.selQuestionSetLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL) AND tableId = 25; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 25; END");

                        /*
                        SelQuestionSetRecognition - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSetRecognition  ( selQuestionSetRecognitionUid  BIGINT PRIMARY KEY ,  selQuestionSetRecognitionSelQuestionSetResponseUid  BIGINT,  selQuestionSetRecognitionClazzMemberUid  BIGINT,  selQuestionSetRecognitionRecognized  BOOL,  selQuestionSetRecognitionMasterChangeSeqNum  BIGINT,  selQuestionSetRecognitionLocalChangeSeqNum  BIGINT,  selQuestionSetRecognitionLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (26, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (26, 1)");
                        db.execSql("CREATE TRIGGER upd_26 AFTER update ON SelQuestionSetRecognition FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetRecognitionMasterChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionMasterChangeSeqNum = NEW.selQuestionSetRecognitionMasterChangeSeqNum) ELSE (NEW.selQuestionSetRecognitionLocalChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionLocalChangeSeqNum = NEW.selQuestionSetRecognitionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END),selQuestionSetRecognitionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END) WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 26; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetRecognition_spk_view AS SELECT selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition");
                        db.execSql("CREATE TRIGGER ins_26 INSTEAD OF INSERT ON SelQuestionSetRecognition_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 26)) ELSE NEW.selQuestionSetRecognitionUid END; INSERT INTO SelQuestionSetRecognition(selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetRecognitionSelQuestionSetResponseUid, NEW.selQuestionSetRecognitionClazzMemberUid, NEW.selQuestionSetRecognitionRecognized, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END), NEW.selQuestionSetRecognitionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL) AND tableId = 26; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 26; END");

                        /*
                        SelQuestionSetResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSetResponse  ( selQuestionSetResposeUid  BIGINT PRIMARY KEY ,  selQuestionSetResponseSelQuestionSetUid  BIGINT,  selQuestionSetResponseClazzMemberUid  BIGINT,  selQuestionSetResponseStartTime  BIGINT,  selQuestionSetResponseFinishTime  BIGINT,  selQuestionSetResponseRecognitionPercentage  FLOAT,  selQuestionSetResponseMasterChangeSeqNum  BIGINT,  selQuestionSetResponseLocalChangeSeqNum  BIGINT,  selQuestionSetResponseLastChangedBy  INTEGER)");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (27, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (27, 1)");
                        db.execSql("CREATE TRIGGER upd_27 AFTER update ON SelQuestionSetResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetResponseMasterChangeSeqNum = 0 OR OLD.selQuestionSetResponseMasterChangeSeqNum = NEW.selQuestionSetResponseMasterChangeSeqNum) ELSE (NEW.selQuestionSetResponseLocalChangeSeqNum = 0 OR OLD.selQuestionSetResponseLocalChangeSeqNum = NEW.selQuestionSetResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END),selQuestionSetResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END) WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 27; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetResponse_spk_view AS SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse");
                        db.execSql("CREATE TRIGGER ins_27 INSTEAD OF INSERT ON SelQuestionSetResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 27)) ELSE NEW.selQuestionSetResposeUid END; INSERT INTO SelQuestionSetResponse(selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetResponseSelQuestionSetUid, NEW.selQuestionSetResponseClazzMemberUid, NEW.selQuestionSetResponseStartTime, NEW.selQuestionSetResponseFinishTime, NEW.selQuestionSetResponseRecognitionPercentage, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END), NEW.selQuestionSetResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL) AND tableId = 27; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 27; END");

                        /*
                        SocialNominationQuestion - removed
                        SocialNominationQuestionResponse - removed
                        SocialNominationQuestionResponseNomination - removed
                        SocialNominationQuestionSet - removed
                        SocialNominationQuestionSetRecognition - removed
                        SocialNominationQuestionSetResponse - removed
                        */
                        //Check if we have to migrate first
//                        db.execSql("DROP TABLE SocialNominationQuestion");
//                        db.execSql("DROP TABLE SocialNominationQuestionResponse");
//                        db.execSql("DROP TABLE SocialNominationQuestionResponseNomination");
//                        db.execSql("DROP TABLE SocialNominationQuestionSet");
//                        db.execSql("DROP TABLE SocialNominationQuestionSetRecognition");
//                        db.execSql("DROP TABLE SocialNominationQuestionSetResponse");



                        break;
                    case UmDbType.TYPE_POSTGRES:
                        int deviceBits = new Random().nextInt();

                        /*ClazzLog : added two fields
                            private boolean canceled;
                            private long clazzLogScheduleUid;
                        */
                        db.execSql("ALTER TABLE ClazzLog ADD canceled BOOL");
                        db.execSql("ALTER TABLE ClazzLog ADD clazzLogScheduleUid BIGINT");



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

    public abstract OpdsEntryDao getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDao getOpdsEntryWithRelationsDao();

    public abstract OpdsEntryStatusCacheDao getOpdsEntryStatusCacheDao();

    public abstract OpdsEntryStatusCacheAncestorDao getOpdsEntryStatusCacheAncestorDao();

    public abstract OpdsLinkDao getOpdsLinkDao();

    public abstract OpdsEntryParentToChildJoinDao getOpdsEntryParentToChildJoinDao();

    public abstract ContainerFileDao getContainerFileDao();

    public abstract ContainerFileEntryDao getContainerFileEntryDao();

    public abstract NetworkNodeDao getNetworkNodeDao();

    public abstract EntryStatusResponseDao getEntryStatusResponseDao();

    public abstract DownloadSetDao getDownloadSetDao();

    public abstract DownloadSetItemDao getDownloadSetItemDao();

    public abstract DownloadJobDao getDownloadJobDao();

    public abstract DownloadJobItemDao getDownloadJobItemDao();

    public abstract DownloadJobItemHistoryDao getDownloadJobItemHistoryDao();

    public abstract CrawlJobDao getCrawlJobDao();

    public abstract CrawJoblItemDao getDownloadJobCrawlItemDao();

    public abstract HttpCachedEntryDao getHttpCachedEntryDao();

    public abstract PersonDao getPersonDao();

    public abstract ClazzDao getClazzDao();

    public abstract ClazzMemberDao getClazzMemberDao();

    public abstract ClazzLogDao getClazzLogDao();

    public abstract ClazzLogAttendanceRecordDao getClazzLogAttendanceRecordDao();

    public abstract FeedEntryDao getFeedEntryDao();

    public abstract ContentEntryDao getContentEntryDao();

    public abstract PersonCustomFieldDao getPersonCustomFieldDao();

    public abstract PersonCustomFieldValueDao getPersonCustomFieldValueDao();

    public abstract PersonDetailPresenterFieldDao getPersonDetailPresenterFieldDao();

    public abstract SelQuestionDao getSocialNominationQuestionDao();

    public abstract SelQuestionSetResponseDao getSocialNominationQuestionSetResponseDao();

    public abstract SelQuestionSetDao getSocialNominationQuestionSetDao();

    public abstract SelQuestionResponseNominationDao getSocialNominationQuestionResponseNominationDao();

    public abstract SelQuestionResponseDao getSocialNominationQuestionResponseDao();

    public abstract ScheduleDao getScheduleDao();

    public abstract UMCalendarDao getUMCalendarDao();

    public abstract HolidayDao getHolidayDao();

    public abstract ClazzActivityDao getClazzActivityDao();

    public abstract ClazzActivityChangeDao getClazzActivityChangeDao();

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

    public abstract SelQuestionOptionDao getSELQuestionOptionDao();

    public abstract ScheduledCheckDao getScheduledCheckDao();

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
        if(personUid == 0)
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
