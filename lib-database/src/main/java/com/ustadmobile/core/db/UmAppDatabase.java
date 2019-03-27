package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.AccessTokenDao;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.ConnectivityStatusDao;
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
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.HolidayDao;
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
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.PersonLocationJoinDao;
import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.db.dao.ScheduledCheckDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
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
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
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
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
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
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.entities.SelQuestionOption;
import com.ustadmobile.lib.db.entities.SelQuestionResponse;
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
        ContainerFile.class, ContainerFileEntry.class, DownloadSet.class,
        DownloadSetItem.class, NetworkNode.class, EntryStatusResponse.class,
        DownloadJobItemHistory.class,
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
        SelQuestionOption.class, ScheduledCheck.class,
        PersonLocationJoin.class, PersonPicture.class, ScrapeQueueItem.class, ScrapeRun.class,
        ContentEntryStatus.class, ConnectivityStatus.class

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
     *
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

                        /*
                        First of all SyncStatus needs changed
                        masterChangeSeqNum removed (always 1)
                        localChangeSeqNum renamed to nextChangeSeqNum
                         */

                        //Add nextChangeSeqNum:
                        db.execSql("ALTER TABLE SyncStatus ADD COLUMN nextChangeSeqNum INTEGER NOT NULL DEFAULT 0"); // OK

                        /**
                         *To remove localChangeSeqNum and masterChangeSeqNum
                         *
                         */
                        db.execSql("BEGIN TRANSACTION");
                        db.execSql("ALTER TABLE SyncStatus RENAME TO temp_SyncStatus");
                        db.execSql("CREATE TABLE IF NOT EXISTS `SyncStatus` (`tableId` INTEGER NOT NULL, `nextChangeSeqNum` INTEGER NOT NULL, `syncedToMasterChangeNum` INTEGER NOT NULL, `syncedToLocalChangeSeqNum` INTEGER NOT NULL, PRIMARY KEY(`tableId`))");

                        db.execSql("INSERT INTO SyncStatus SELECT tableId, localChangeSeqNum, " +
                                "syncedToMasterChangeNum, syncedToLocalChangeSeqNum FROM temp_SyncStatus");
                        db.execSql("DROP TABLE temp_SyncStatus");
                        db.execSql("COMMIT");


                        /**
                         * SyncDeviceBits has an extra boolean
                         * private boolean master;*
                         */
                        db.execSql("ALTER TABLE SyncDeviceBits ADD COLUMN `master` INTEGER NOT NULL DEFAULT 0");


                        /*
                        SocialNominationQuestion - removed
                        SocialNominationQuestionResponse - removed
                        SocialNominationQuestionResponseNomination - removed
                        SocialNominationQuestionSet - removed
                        SocialNominationQuestionSetRecognition - removed
                        SocialNominationQuestionSetResponse - removed
                        */
                        //Check if we have to migrate first

                        db.execSql("DROP TABLE SocialNominationQuestion");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 22");
                        db.execSql("DROP TABLE SocialNominationQuestionResponse");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 23");
                        db.execSql("DROP TABLE SocialNominationQuestionResponseNomination");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 24");
                        db.execSql("DROP TABLE SocialNominationQuestionSet");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 25");
                        db.execSql("DROP TABLE SocialNominationQuestionSetRecognition");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 26");
                        db.execSql("DROP TABLE SocialNominationQuestionSetResponse");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 27");



                        /*ClazzLog : added two fields
                            private boolean canceled;
                            private long clazzLogScheduleUid;
                        */
                        db.execSql("ALTER TABLE ClazzLog ADD canceled INTEGER NOT NULL DEFAULT 0");
                        db.execSql("ALTER TABLE ClazzLog ADD clazzLogScheduleUid INTEGER NOT NULL DEFAULT 0");
                        db.execSql("DROP VIEW IF EXISTS ClazzLog_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzLog_spk_view AS SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, done, canceled, numPresent, numAbsent, numPartial, clazzLogScheduleUid, clazzLogChangeMasterChangeSeqNum, clazzLogChangeLocalChangeSeqNum, clazzLogLastChangedBy FROM ClazzLog");

                        /*
                        FeedEntry : added three fields
                            private long feedEntryClazzLogUid;
                            private long dateCreated;
                            private int feedEntryCheckType
                        */
                        db.execSql("ALTER TABLE FeedEntry ADD feedEntryClazzLogUid  INTEGER NOT NULL DEFAULT 0");
                        db.execSql("ALTER TABLE FeedEntry ADD dateCreated INTEGER NOT NULL DEFAULT 0");
                        db.execSql("ALTER TABLE FeedEntry ADD feedEntryCheckType INTEGER NOT NULL DEFAULT 0");
                        db.execSql("DROP VIEW IF EXISTS FeedEntry_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS FeedEntry_spk_view AS SELECT feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy FROM FeedEntry");


                        /*
                        Location: added one fields
                            private String timeZone;
                        */
                        db.execSql("ALTER TABLE Location ADD timeZone TEXT");
                        db.execSql("DROP VIEW IF EXISTS Location_spk_view");
                        db.execSql("CREATE VIEW IF NOT EXISTS Location_spk_view AS SELECT locationUid, title, locationDesc, lng, lat, parentLocationUid, timeZone, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy FROM Location");


                        /*
                        ScheduledCheck : New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `ScheduledCheck` (`scheduledCheckId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `checkTime` INTEGER NOT NULL, `checkType` INTEGER NOT NULL, `checkUuid` TEXT, `checkParameters` TEXT, `scClazzLogUid` INTEGER NOT NULL)");
                        db.execSql("CREATE  INDEX `clazzUid_type_index` ON `ScheduledCheck` (`scClazzLogUid`, `checkType`)");

                        /*
                        SelQuestion - New entity
                        */

                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestion` (`selQuestionUid` INTEGER NOT NULL, `questionText` TEXT, `selQuestionSelQuestionSetUid` INTEGER NOT NULL, `questionIndex` INTEGER NOT NULL, `assignToAllClasses` INTEGER NOT NULL, `multiNominations` INTEGER NOT NULL, `questionType` INTEGER NOT NULL, `questionActive` INTEGER NOT NULL, `selQuestionMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (22, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (22, 1)");
                        db.execSql("CREATE TRIGGER upd_22 AFTER update ON SelQuestion FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionMasterChangeSeqNum = 0 OR OLD.selQuestionMasterChangeSeqNum = NEW.selQuestionMasterChangeSeqNum) ELSE (NEW.selQuestionLocalChangeSeqNum = 0 OR OLD.selQuestionLocalChangeSeqNum = NEW.selQuestionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END),selQuestionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END) WHERE selQuestionUid = NEW.selQuestionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 22; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestion_spk_view AS SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion");
                        db.execSql("CREATE TRIGGER ins_22 INSTEAD OF INSERT ON SelQuestion_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 22)) ELSE NEW.selQuestionUid END; INSERT INTO SelQuestion(selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.questionText, NEW.selQuestionSelQuestionSetUid, NEW.questionIndex, NEW.assignToAllClasses, NEW.multiNominations, NEW.questionType, NEW.questionActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END), NEW.selQuestionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL) AND tableId = 22; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 22; END");

                        /*
                        SelQuestionOption - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionOption` (`selQuestionOptionUid` INTEGER NOT NULL, `optionText` TEXT, `selQuestionOptionQuestionUid` INTEGER NOT NULL, `selQuestionOptionMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionOptionLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionOptionLastChangedBy` INTEGER NOT NULL, `optionActive` INTEGER NOT NULL, PRIMARY KEY(`selQuestionOptionUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (52, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (52, 1)");
                        db.execSql("CREATE TRIGGER upd_52 AFTER update ON SelQuestionOption FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionOptionMasterChangeSeqNum = 0 OR OLD.selQuestionOptionMasterChangeSeqNum = NEW.selQuestionOptionMasterChangeSeqNum) ELSE (NEW.selQuestionOptionLocalChangeSeqNum = 0 OR OLD.selQuestionOptionLocalChangeSeqNum = NEW.selQuestionOptionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END),selQuestionOptionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END) WHERE selQuestionOptionUid = NEW.selQuestionOptionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 52; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionOption_spk_view AS SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption");
                        db.execSql("CREATE TRIGGER ins_52 INSTEAD OF INSERT ON SelQuestionOption_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 52)) ELSE NEW.selQuestionOptionUid END; INSERT INTO SelQuestionOption(selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.optionText, NEW.selQuestionOptionQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END), NEW.selQuestionOptionLastChangedBy, NEW.optionActive); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL) AND tableId = 52; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 52; END");

                        /*
                        SelQuestionResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionResponse` (`selQuestionResponseUid` INTEGER NOT NULL, `selQuestionResponseSelQuestionSetResponseUid` INTEGER NOT NULL, `selQuestionResponseSelQuestionUid` INTEGER NOT NULL, `selQuestionResponseMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionResponseUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (23, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (23, 1)");
                        db.execSql("CREATE TRIGGER upd_23 AFTER update ON SelQuestionResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseMasterChangeSeqNum = 0 OR OLD.selQuestionResponseMasterChangeSeqNum = NEW.selQuestionResponseMasterChangeSeqNum) ELSE (NEW.selQuestionResponseLocalChangeSeqNum = 0 OR OLD.selQuestionResponseLocalChangeSeqNum = NEW.selQuestionResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END),selQuestionResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END) WHERE selQuestionResponseUid = NEW.selQuestionResponseUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 23; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponse_spk_view AS SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse");
                        db.execSql("CREATE TRIGGER ins_23 INSTEAD OF INSERT ON SelQuestionResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 23)) ELSE NEW.selQuestionResponseUid END; INSERT INTO SelQuestionResponse(selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseSelQuestionSetResponseUid, NEW.selQuestionResponseSelQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END), NEW.selQuestionResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL) AND tableId = 23; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 23; END");

                        /*
                        SelQuestionResponseNomination - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionResponseNomination` (`selQuestionResponseNominationUid` INTEGER NOT NULL, `selQuestionResponseNominationClazzMemberUid` INTEGER NOT NULL, `selQuestionResponseNominationSelQuestionResponseUId` INTEGER NOT NULL, `nominationActive` INTEGER NOT NULL, `selQuestionResponseNominationMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseNominationLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseNominationLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionResponseNominationUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (24, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (24, 1)");
                        db.execSql("CREATE TRIGGER upd_24 AFTER update ON SelQuestionResponseNomination FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseNominationMasterChangeSeqNum = 0 OR OLD.selQuestionResponseNominationMasterChangeSeqNum = NEW.selQuestionResponseNominationMasterChangeSeqNum) ELSE (NEW.selQuestionResponseNominationLocalChangeSeqNum = 0 OR OLD.selQuestionResponseNominationLocalChangeSeqNum = NEW.selQuestionResponseNominationLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponseNomination SET selQuestionResponseNominationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END),selQuestionResponseNominationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END) WHERE selQuestionResponseNominationUid = NEW.selQuestionResponseNominationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 24; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponseNomination_spk_view AS SELECT selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy FROM SelQuestionResponseNomination");
                        db.execSql("CREATE TRIGGER ins_24 INSTEAD OF INSERT ON SelQuestionResponseNomination_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 24)) ELSE NEW.selQuestionResponseNominationUid END; INSERT INTO SelQuestionResponseNomination(selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseNominationClazzMemberUid, NEW.selQuestionResponseNominationSelQuestionResponseUId, NEW.nominationActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END), NEW.selQuestionResponseNominationLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL) AND tableId = 24; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 24; END");

                        /*
                        SelQuestionSet - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSet` (`selQuestionSetUid` INTEGER NOT NULL, `title` TEXT, `selQuestionSetMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (25, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (25, 1)");
                        db.execSql("CREATE TRIGGER upd_25 AFTER update ON SelQuestionSet FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetMasterChangeSeqNum = 0 OR OLD.selQuestionSetMasterChangeSeqNum = NEW.selQuestionSetMasterChangeSeqNum) ELSE (NEW.selQuestionSetLocalChangeSeqNum = 0 OR OLD.selQuestionSetLocalChangeSeqNum = NEW.selQuestionSetLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END),selQuestionSetMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END) WHERE selQuestionSetUid = NEW.selQuestionSetUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 25; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSet_spk_view AS SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet");
                        db.execSql("CREATE TRIGGER ins_25 INSTEAD OF INSERT ON SelQuestionSet_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 25)) ELSE NEW.selQuestionSetUid END; INSERT INTO SelQuestionSet(selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.title, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END), NEW.selQuestionSetLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL) AND tableId = 25; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 25; END");

                        /*
                        SelQuestionSetRecognition - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSetRecognition` (`selQuestionSetRecognitionUid` INTEGER NOT NULL, `selQuestionSetRecognitionSelQuestionSetResponseUid` INTEGER NOT NULL, `selQuestionSetRecognitionClazzMemberUid` INTEGER NOT NULL, `selQuestionSetRecognitionRecognized` INTEGER NOT NULL, `selQuestionSetRecognitionMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetRecognitionLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetRecognitionLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetRecognitionUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (26, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (26, 1)");
                        db.execSql("CREATE TRIGGER upd_26 AFTER update ON SelQuestionSetRecognition FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetRecognitionMasterChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionMasterChangeSeqNum = NEW.selQuestionSetRecognitionMasterChangeSeqNum) ELSE (NEW.selQuestionSetRecognitionLocalChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionLocalChangeSeqNum = NEW.selQuestionSetRecognitionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END),selQuestionSetRecognitionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END) WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 26; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetRecognition_spk_view AS SELECT selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition");
                        db.execSql("CREATE TRIGGER ins_26 INSTEAD OF INSERT ON SelQuestionSetRecognition_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 26)) ELSE NEW.selQuestionSetRecognitionUid END; INSERT INTO SelQuestionSetRecognition(selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetRecognitionSelQuestionSetResponseUid, NEW.selQuestionSetRecognitionClazzMemberUid, NEW.selQuestionSetRecognitionRecognized, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END), NEW.selQuestionSetRecognitionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL) AND tableId = 26; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 26; END");

                        /*
                        SelQuestionSetResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSetResponse` (`selQuestionSetResposeUid` INTEGER NOT NULL, `selQuestionSetResponseSelQuestionSetUid` INTEGER NOT NULL, `selQuestionSetResponseClazzMemberUid` INTEGER NOT NULL, `selQuestionSetResponseStartTime` INTEGER NOT NULL, `selQuestionSetResponseFinishTime` INTEGER NOT NULL, `selQuestionSetResponseRecognitionPercentage` REAL NOT NULL, `selQuestionSetResponseMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetResponseLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetResponseLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetResposeUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (27, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (27, 1)");
                        db.execSql("CREATE TRIGGER upd_27 AFTER update ON SelQuestionSetResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetResponseMasterChangeSeqNum = 0 OR OLD.selQuestionSetResponseMasterChangeSeqNum = NEW.selQuestionSetResponseMasterChangeSeqNum) ELSE (NEW.selQuestionSetResponseLocalChangeSeqNum = 0 OR OLD.selQuestionSetResponseLocalChangeSeqNum = NEW.selQuestionSetResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END),selQuestionSetResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END) WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 27; END");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetResponse_spk_view AS SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse");
                        db.execSql("CREATE TRIGGER ins_27 INSTEAD OF INSERT ON SelQuestionSetResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 27)) ELSE NEW.selQuestionSetResposeUid END; INSERT INTO SelQuestionSetResponse(selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetResponseSelQuestionSetUid, NEW.selQuestionSetResponseClazzMemberUid, NEW.selQuestionSetResponseStartTime, NEW.selQuestionSetResponseFinishTime, NEW.selQuestionSetResponseRecognitionPercentage, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END), NEW.selQuestionSetResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL) AND tableId = 27; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 27; END");


                        /**
                         * Download Set
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadSet");
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSet  ( dsUid  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL ,  destinationDir  TEXT,  meteredNetworkAllowed  INTEGER NOT NULL,  dsRootContentEntryUid  INTEGER NOT NULL)");
                        //END Create DownloadSet (SQLite)

                        //BEGIN Create DownloadSetItem (SQLite)
                        db.execSql("DROP TABLE  IF EXISTS DownloadSetItem");
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSetItem  ( dsiUid  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL ,  dsiDsUid  BIGINT NOT NULL,  dsiContentEntryUid  BIGINT NOT NULL)");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiContentEntryUid  ON  DownloadSetItem  ( dsiContentEntryUid  )");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiDsUid  ON  DownloadSetItem  ( dsiDsUid  )");

                        /**
                         * Network Node
                         * Removed wifiDirectLastUpdated column
                         */
                        db.execSql("DROP TABLE IF EXISTS NetworkNode");
                        //BEGIN Create NetworkNode (SQLite)
                        db.execSql("CREATE TABLE IF NOT EXISTS  NetworkNode  ( nodeId  INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL ,  bluetoothMacAddress  TEXT,  ipAddress  TEXT,  wifiDirectMacAddress  TEXT,  deviceWifiDirectName  TEXT,  lastUpdateTimeStamp  BIGINT NOT NULL,  networkServiceLastUpdated  BIGINT NOT NULL,  nsdServiceName  TEXT,  port  INTEGER NOT NULL,  wifiDirectDeviceStatus  INTEGER NOT NULL)");
                        //END Create NetworkNode (SQLite)

                        /** EntryStatusResponse updated several columns, etc
                         *
                         */
                        db.execSql("DROP TABLE IF EXISTS EntryStatusResponse");
                        db.execSql("CREATE TABLE IF NOT EXISTS `EntryStatusResponse` (`erId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `erContentEntryFileUid` INTEGER NOT NULL, `responseTime` INTEGER NOT NULL, `erNodeId` INTEGER NOT NULL, `available` INTEGER NOT NULL)");
                        db.execSql("CREATE UNIQUE INDEX `nodeId_fileUid_unique` ON `EntryStatusResponse` (`erContentEntryFileUid`, `erNodeId`)");
                        //END Create EntryStatusResponse (SQLite)

                        /** Download job
                         *
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadJob");
                        db.execSql("CREATE TABLE IF NOT EXISTS `DownloadJob` (`djUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `djDsUid` INTEGER NOT NULL, `timeCreated` INTEGER NOT NULL, `timeRequested` INTEGER NOT NULL, `timeCompleted` INTEGER NOT NULL, `totalBytesToDownload` INTEGER NOT NULL, `bytesDownloadedSoFar` INTEGER NOT NULL, `djStatus` INTEGER NOT NULL)");

                        /**
                         * DownloadJobItem
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadJobItem");
                        db.execSql("CREATE TABLE IF NOT EXISTS `DownloadJobItem` (`djiUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `djiDsiUid` INTEGER NOT NULL, `djiDjUid` INTEGER NOT NULL, `djiContentEntryFileUid` INTEGER NOT NULL, `downloadedSoFar` INTEGER NOT NULL, `downloadLength` INTEGER NOT NULL, `currentSpeed` INTEGER NOT NULL, `timeStarted` INTEGER NOT NULL, `timeFinished` INTEGER NOT NULL, `djiStatus` INTEGER NOT NULL, `destinationFile` TEXT, `numAttempts` INTEGER NOT NULL)");
                        db.execSql("CREATE  INDEX `index_DownloadJobItem_timeStarted` ON `DownloadJobItem` (`timeStarted`)");
                        db.execSql("CREATE  INDEX `index_DownloadJobItem_djiStatus` ON `DownloadJobItem` (`djiStatus`)");

                        /**
                         * Content Entry
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntry");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntry` (`contentEntryUid` INTEGER NOT NULL, `title` TEXT, `description` TEXT, `entryId` TEXT, `author` TEXT, `publisher` TEXT, `licenseType` INTEGER NOT NULL, `licenseName` TEXT, `licenseUrl` TEXT, `sourceUrl` TEXT, `thumbnailUrl` TEXT, `lastModified` INTEGER NOT NULL, `primaryLanguageUid` INTEGER NOT NULL, `languageVariantUid` INTEGER NOT NULL, `leaf` INTEGER NOT NULL, `publik` INTEGER NOT NULL, `contentTypeFlag` INTEGER NOT NULL, `contentEntryLocalChangeSeqNum` INTEGER NOT NULL, `contentEntryMasterChangeSeqNum` INTEGER NOT NULL, `contentEntryLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`contentEntryUid`))");
                        db.execSql("CREATE  INDEX `index_ContentEntry_primaryLanguageUid` ON `ContentEntry` (`primaryLanguageUid`)");

                        /**
                         * ContentEntryContentCategoryJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryContentCategoryJoin");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryContentCategoryJoin` (`ceccjUid` INTEGER NOT NULL, `ceccjContentEntryUid` INTEGER NOT NULL, `ceccjContentCategoryUid` INTEGER NOT NULL, `ceccjLocalChangeSeqNum` INTEGER NOT NULL, `ceccjMasterChangeSeqNum` INTEGER NOT NULL, `ceccjLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`ceccjUid`))");
                        db.execSql("CREATE  INDEX `index_ContentEntryContentCategoryJoin_ceccjContentEntryUid` ON `ContentEntryContentCategoryJoin` (`ceccjContentEntryUid`)");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryContentEntryFileJoin` (`cecefjUid` INTEGER NOT NULL, `cecefjContentEntryUid` INTEGER NOT NULL, `cecefjContentEntryFileUid` INTEGER NOT NULL, `cecefjLocalChangeSeqNum` INTEGER NOT NULL, `cecefjMasterChangeSeqNum` INTEGER NOT NULL, `cecefjLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`cecefjUid`))");
                        db.execSql("CREATE  INDEX `index_ContentEntryContentEntryFileJoin_cecefjContentEntryUid` ON `ContentEntryContentEntryFileJoin` (`cecefjContentEntryUid`)");
                        db.execSql("CREATE  INDEX `index_ContentEntryContentEntryFileJoin_cecefjContentEntryFileUid` ON `ContentEntryContentEntryFileJoin` (`cecefjContentEntryFileUid`)");

                        /**
                         * ContentEntryFile
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryFile");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryFile` (`contentEntryFileUid` INTEGER NOT NULL, `fileSize` INTEGER NOT NULL, `md5sum` TEXT, `lastModified` INTEGER NOT NULL, `mimeType` TEXT, `remarks` TEXT, `mobileOptimized` INTEGER NOT NULL, `contentEntryFileLocalChangeSeqNum` INTEGER NOT NULL, `contentEntryFileMasterChangeSeqNum` INTEGER NOT NULL, `contentEntryFileLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`contentEntryFileUid`))");
                        db.execSql("CREATE  INDEX `index_ContentEntryFile_lastModified` ON `ContentEntryFile` (`lastModified`)");
                        /**
                         * ContentEntryParentChildJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryParentChildJoin");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryParentChildJoin` (`cepcjUid` INTEGER NOT NULL, `cepcjChildContentEntryUid` INTEGER NOT NULL, `cepcjParentContentEntryUid` INTEGER NOT NULL, `childIndex` INTEGER NOT NULL, `cepcjLocalChangeSeqNum` INTEGER NOT NULL, `cepcjMasterChangeSeqNum` INTEGER NOT NULL, `cepcjLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`cepcjUid`))");
                        db.execSql("CREATE  INDEX `parent_child` ON `ContentEntryParentChildJoin` (`cepcjChildContentEntryUid`, `cepcjParentContentEntryUid`)");

                        /**
                         * ContentEntryRelatedEntryJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryRelatedEntryJoin");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryRelatedEntryJoin` (`cerejUid` INTEGER NOT NULL, `cerejContentEntryUid` INTEGER NOT NULL, `cerejRelatedEntryUid` INTEGER NOT NULL, `cerejLastChangedBy` INTEGER NOT NULL, `relType` INTEGER NOT NULL, `comment` TEXT, `cerejRelLanguageUid` INTEGER NOT NULL, `cerejLocalChangeSeqNum` INTEGER NOT NULL, `cerejMasterChangeSeqNum` INTEGER NOT NULL, PRIMARY KEY(`cerejUid`))");


                        /**
                         * ScrapeQueueItem
                         */
                        db.execSql("DROP TABLE IF EXISTS ScrapeQueueItem");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ScrapeQueueItem` (`sqiUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sqiContentEntryParentUid` INTEGER NOT NULL, `destDir` TEXT, `scrapeUrl` TEXT, `status` INTEGER NOT NULL, `runId` INTEGER NOT NULL, `itemType` INTEGER NOT NULL, `contentType` TEXT, `timeAdded` INTEGER NOT NULL, `timeStarted` INTEGER NOT NULL, `timeFinished` INTEGER NOT NULL)");

                        /**
                         * ScrapeRun
                         */
                        db.execSql("DROP TABLE IF EXISTS ScrapeRun");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ScrapeRun` (`scrapeRunUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scrapeType` TEXT, `status` INTEGER NOT NULL)");

                        /**
                         * ContentEntryStatus
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryStatus");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ContentEntryStatus` (`cesUid` INTEGER NOT NULL, `totalSize` INTEGER NOT NULL, `bytesDownloadSoFar` INTEGER NOT NULL, `downloadStatus` INTEGER NOT NULL, `localAvailability` INTEGER NOT NULL, `downloadSpeed` INTEGER NOT NULL, `invalidated` INTEGER NOT NULL, `cesLeaf` INTEGER NOT NULL, PRIMARY KEY(`cesUid`))");

                        /**
                         * ConnectivityStatus
                         */
                        db.execSql("DROP TABLE IF EXISTS ConnectivityStatus");
                        db.execSql("CREATE TABLE IF NOT EXISTS `ConnectivityStatus` (`csUid` INTEGER NOT NULL, `connectivityState` INTEGER NOT NULL, `wifiSsid` TEXT, `connectedOrConnecting` INTEGER NOT NULL, PRIMARY KEY(`csUid`))");

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

    public abstract ScheduledCheckDao getScheduledCheckDao();

    public abstract SelQuestionOptionDao getSELQuestionOptionDao();


    public abstract ScrapeRunDao getScrapeRunDao();

    public abstract ContentEntryStatusDao getContentEntryStatusDao();

    public abstract ConnectivityStatusDao getConnectivityStatusDao();


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
