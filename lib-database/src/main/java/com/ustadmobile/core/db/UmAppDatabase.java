package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.AccessTokenDao;
import com.ustadmobile.core.db.dao.AuditLogDao;
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
import com.ustadmobile.core.db.dao.CustomFieldDao;
import com.ustadmobile.core.db.dao.CustomFieldValueDao;
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao;
import com.ustadmobile.core.db.dao.DateRangeDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
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
import com.ustadmobile.lib.db.DoorUtils;
import com.ustadmobile.lib.db.UmDbMigration;
import com.ustadmobile.lib.db.UmDbType;
import com.ustadmobile.lib.db.UmDbWithAttachmentsDir;
import com.ustadmobile.lib.db.UmDbWithAuthenticator;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.AuditLog;
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
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.CustomFieldValue;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.FeedEntry;
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


@UmDatabase(version = 5, entities = {
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
        Schedule.class, DateRange.class, UMCalendar.class,
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
        ContentEntryStatus.class, ConnectivityStatus.class,
        AuditLog.class, CustomField.class, CustomFieldValue.class, CustomFieldValueOption.class

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

                System.out.println("STARTING MIGRATIONS >>>");

                switch (db.getDbType()) {
                    case UmDbType.TYPE_SQLITE:

                        //Bumping SyncablePrimaryKey
                        db.execSql("UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1");


                        /**
                         First of all SyncStatus needs changed
                         localChangeSeqNum removed (always 1)
                         masterChangeSeqNum renamed to nextChangeSeqNum
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

                        db.execSql("INSERT INTO SyncStatus SELECT tableId, masterChangeSeqNum, " +
                                "syncedToMasterChangeNum, syncedToLocalChangeSeqNum FROM temp_SyncStatus");
                        db.execSql("DROP TABLE temp_SyncStatus");
                        db.execSql("COMMIT");

                        //Last syncable PK entity
                        db.execSql("CREATE TABLE IF NOT EXISTS _lastsyncablepk(id INTEGER PRIMARY KEY AUTOINCREMENT, lastpk INTEGER)");

                        // The triggers need updated as well.

                        /**
                         * DROP ALL Triggers
                         */
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_person");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_person");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazz");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazz");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazzmember");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazzmember");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazzlog");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazzlog");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazzlogattendancerecord");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazzlogattendancerecord");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_feedentry");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_feedentry");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_personfield");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_personfield");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_personcustomfieldvalue");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_personcustomfieldvalue");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_persondetailpresenterfield");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_persondetailpresenterfield");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestion");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestion");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestionresponse");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestionresponse");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestionresponsenomination");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestionresponsenomination");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestionset");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestionset");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestionsetrecognition");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestionsetrecognition");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_socialnominationquestionsetresponse");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_socialnominationquestionsetresponse");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_schedule");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_schedule");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_holiday");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_holiday");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_umcalendar");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_umcalendar");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazzactivity");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazzactivity");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_clazzactivitychange");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_clazzactivitychange");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententry");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententry");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententrycontentcategoryjoin");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententrycontentcategoryjoin");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententrycontententryfilejoin");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententrycontententryfilejoin");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententryfile");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententryfile");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententryparentchildjoin");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententryparentchildjoin");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contententryrelatedentryjoin");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contententryrelatedentryjoin");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_location");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_location");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contentcategoryschema");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contentcategoryschema");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_contentcategory");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_contentcategory");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_language");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_language");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_languagevariant");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_languagevariant");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_personauth");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_personauth");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_role");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_role");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_entityrole");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_entityrole");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_persongroup");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_persongroup");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_persongroupmember");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_persongroupmember");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_personlocationjoin");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_personlocationjoin");
                        db.execSql("DROP TRIGGER IF EXISTS update_csn_personpicture");
                        db.execSql("DROP TRIGGER IF EXISTS insert_csn_personpicture");



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

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestion_spk_view AS SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion");

                        /*
                        SelQuestionOption - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionOption` (`selQuestionOptionUid` INTEGER NOT NULL, `optionText` TEXT, `selQuestionOptionQuestionUid` INTEGER NOT NULL, `selQuestionOptionMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionOptionLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionOptionLastChangedBy` INTEGER NOT NULL, `optionActive` INTEGER NOT NULL, PRIMARY KEY(`selQuestionOptionUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (52, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (52, 1)");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionOption_spk_view AS SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption");


                        /*
                        SelQuestionResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionResponse` (`selQuestionResponseUid` INTEGER NOT NULL, `selQuestionResponseSelQuestionSetResponseUid` INTEGER NOT NULL, `selQuestionResponseSelQuestionUid` INTEGER NOT NULL, `selQuestionResponseMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionResponseUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (23, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (23, 1)");

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponse_spk_view AS SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse");

                        /*
                        SelQuestionResponseNomination - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionResponseNomination` (`selQuestionResponseNominationUid` INTEGER NOT NULL, `selQuestionResponseNominationClazzMemberUid` INTEGER NOT NULL, `selQuestionResponseNominationSelQuestionResponseUId` INTEGER NOT NULL, `nominationActive` INTEGER NOT NULL, `selQuestionResponseNominationMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseNominationLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionResponseNominationLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionResponseNominationUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (24, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (24, 1)");

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponseNomination_spk_view AS SELECT selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy FROM SelQuestionResponseNomination");


                        /*
                        SelQuestionSet - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSet` (`selQuestionSetUid` INTEGER NOT NULL, `title` TEXT, `selQuestionSetMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (25, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (25, 1)");

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSet_spk_view AS SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet");


                        /*
                        SelQuestionSetRecognition - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSetRecognition` (`selQuestionSetRecognitionUid` INTEGER NOT NULL, `selQuestionSetRecognitionSelQuestionSetResponseUid` INTEGER NOT NULL, `selQuestionSetRecognitionClazzMemberUid` INTEGER NOT NULL, `selQuestionSetRecognitionRecognized` INTEGER NOT NULL, `selQuestionSetRecognitionMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetRecognitionLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetRecognitionLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetRecognitionUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (26, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (26, 1)");

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetRecognition_spk_view AS SELECT selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition");


                        /*
                        SelQuestionSetResponse - New entity
                        */
                        db.execSql("CREATE TABLE IF NOT EXISTS `SelQuestionSetResponse` (`selQuestionSetResposeUid` INTEGER NOT NULL, `selQuestionSetResponseSelQuestionSetUid` INTEGER NOT NULL, `selQuestionSetResponseClazzMemberUid` INTEGER NOT NULL, `selQuestionSetResponseStartTime` INTEGER NOT NULL, `selQuestionSetResponseFinishTime` INTEGER NOT NULL, `selQuestionSetResponseRecognitionPercentage` REAL NOT NULL, `selQuestionSetResponseMasterChangeSeqNum` INTEGER NOT NULL, `selQuestionSetResponseLocalChangeSeqNum` INTEGER NOT NULL, `selQuestionSetResponseLastChangedBy` INTEGER NOT NULL, PRIMARY KEY(`selQuestionSetResposeUid`))");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (27, 1, 0, 0)");
                        db.execSql("INSERT INTO SyncablePrimaryKey (tableId, sequenceNumber) VALUES (27, 1)");

                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetResponse_spk_view AS SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse");



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


                        /**
                         * Create all Views
                         */

                        db.execSql("CREATE VIEW IF NOT EXISTS Person_spk_view AS SELECT personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, address FROM Person");
                        db.execSql("CREATE VIEW IF NOT EXISTS Clazz_spk_view AS SELECT clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, clazzActive, clazzLocationUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy FROM Clazz");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzMember_spk_view AS SELECT clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, dateJoined, dateLeft, role, attendancePercentage, clazzMemberActive, clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy FROM ClazzMember");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzLog_spk_view AS SELECT clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, done, canceled, numPresent, numAbsent, numPartial, clazzLogScheduleUid, clazzLogChangeMasterChangeSeqNum, clazzLogChangeLocalChangeSeqNum, clazzLogLastChangedBy FROM ClazzLog");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzLogAttendanceRecord_spk_view AS SELECT clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy FROM ClazzLogAttendanceRecord");
                        db.execSql("CREATE VIEW IF NOT EXISTS FeedEntry_spk_view AS SELECT feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy FROM FeedEntry");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonField_spk_view AS SELECT personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy FROM PersonField");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonCustomFieldValue_spk_view AS SELECT personCustomFieldValueUid, personCustomFieldValuePersonCustomFieldUid, personCustomFieldValuePersonUid, fieldValue, personCustomFieldValueMasterChangeSeqNum, personCustomFieldValueLocalChangeSeqNum, personCustomFieldValueLastChangedBy FROM PersonCustomFieldValue");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonDetailPresenterField_spk_view AS SELECT personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, readyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy FROM PersonDetailPresenterField");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestion_spk_view AS SELECT selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy FROM SelQuestion");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponse_spk_view AS SELECT selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy FROM SelQuestionResponse");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionResponseNomination_spk_view AS SELECT selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy FROM SelQuestionResponseNomination");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSet_spk_view AS SELECT selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy FROM SelQuestionSet");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetRecognition_spk_view AS SELECT selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy FROM SelQuestionSetRecognition");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionSetResponse_spk_view AS SELECT selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy FROM SelQuestionSetResponse");
                        db.execSql("CREATE VIEW IF NOT EXISTS Schedule_spk_view AS SELECT scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive FROM Schedule");
                        db.execSql("CREATE VIEW IF NOT EXISTS Holiday_spk_view AS SELECT holidayUid, holidayUMCalendarUid, holidayDate, holidayName, holidayLocalChangeSeqNum, holidayMasterChangeSeqNum, holidayLastChangedBy FROM Holiday");
                        db.execSql("CREATE VIEW IF NOT EXISTS UMCalendar_spk_view AS SELECT umCalendarUid, umCalendarName, personMasterChangeSeqNum, personLocalChangeSeqNum, umCalendarLastChangedBy FROM UMCalendar");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzActivity_spk_view AS SELECT clazzActivityUid, clazzActivityClazzActivityChangeUid, clazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivity");
                        db.execSql("CREATE VIEW IF NOT EXISTS ClazzActivityChange_spk_view AS SELECT clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, clazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy FROM ClazzActivityChange");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntry_spk_view AS SELECT contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, leaf, publik, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy FROM ContentEntry");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntryContentCategoryJoin_spk_view AS SELECT ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy FROM ContentEntryContentCategoryJoin");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntryContentEntryFileJoin_spk_view AS SELECT cecefjUid, cecefjContentEntryUid, cecefjContentEntryFileUid, cecefjLocalChangeSeqNum, cecefjMasterChangeSeqNum, cecefjLastChangedBy FROM ContentEntryContentEntryFileJoin");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntryFile_spk_view AS SELECT contentEntryFileUid, fileSize, md5sum, lastModified, mimeType, remarks, mobileOptimized, contentEntryFileLocalChangeSeqNum, contentEntryFileMasterChangeSeqNum, contentEntryFileLastChangedBy FROM ContentEntryFile");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntryParentChildJoin_spk_view AS SELECT cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy FROM ContentEntryParentChildJoin");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentEntryRelatedEntryJoin_spk_view AS SELECT cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum FROM ContentEntryRelatedEntryJoin");
                        db.execSql("CREATE VIEW IF NOT EXISTS Location_spk_view AS SELECT locationUid, title, locationDesc, lng, lat, parentLocationUid, timeZone, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy FROM Location");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentCategorySchema_spk_view AS SELECT contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy FROM ContentCategorySchema");
                        db.execSql("CREATE VIEW IF NOT EXISTS ContentCategory_spk_view AS SELECT contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy FROM ContentCategory");
                        db.execSql("CREATE VIEW IF NOT EXISTS Language_spk_view AS SELECT langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy FROM Language");
                        db.execSql("CREATE VIEW IF NOT EXISTS LanguageVariant_spk_view AS SELECT langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy FROM LanguageVariant");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonAuth_spk_view AS SELECT personAuthUid, passwordHash, personAuthLocalChangeSeqNum, personAuthMasterChangeSeqNum, lastChangedBy FROM PersonAuth");
                        db.execSql("CREATE VIEW IF NOT EXISTS Role_spk_view AS SELECT roleUid, roleName, roleMasterCsn, roleLocalCsn, roleLastChangedBy, rolePermissions FROM Role");
                        db.execSql("CREATE VIEW IF NOT EXISTS EntityRole_spk_view AS SELECT erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, erEntityUid, erGroupUid, erRoleUid FROM EntityRole");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonGroup_spk_view AS SELECT groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, groupPersonUid FROM PersonGroup");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonGroupMember_spk_view AS SELECT groupMemberUid, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy FROM PersonGroupMember");
                        db.execSql("CREATE VIEW IF NOT EXISTS SelQuestionOption_spk_view AS SELECT selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive FROM SelQuestionOption");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonLocationJoin_spk_view AS SELECT personLocationUid, personLocationPersonUid, personLocationLocationUid, plMasterCsn, plLocalCsn, plLastChangedBy FROM PersonLocationJoin");
                        db.execSql("CREATE VIEW IF NOT EXISTS PersonPicture_spk_view AS SELECT personPictureUid, personPicturePersonUid, personPictureMasterCsn, personPictureLocalCsn, personPictureLastChangedBy, fileSize, picTimestamp, mimeType FROM PersonPicture");


                        /**
                         * Create all triggers
                         */

                        db.execSql("CREATE TRIGGER upd_9 AFTER update ON Person FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personMasterChangeSeqNum = 0 OR OLD.personMasterChangeSeqNum = NEW.personMasterChangeSeqNum) ELSE (NEW.personLocalChangeSeqNum = 0 OR OLD.personLocalChangeSeqNum = NEW.personLocalChangeSeqNum) END) BEGIN UPDATE Person SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) ELSE NEW.personMasterChangeSeqNum END) WHERE personUid = NEW.personUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 9; END");
                        db.execSql("CREATE TRIGGER ins_9 INSTEAD OF INSERT ON Person_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personUid = 0 OR NEW.personUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 9)) ELSE NEW.personUid END; INSERT INTO Person(personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, address) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.username, NEW.firstNames, NEW.lastName, NEW.emailAddr, NEW.phoneNum, NEW.gender, NEW.active, NEW.admin, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) ELSE NEW.personMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) END), NEW.personLastChangedBy, NEW.fatherName, NEW.fatherNumber, NEW.motherName, NEW.motherNum, NEW.dateOfBirth, NEW.address); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personUid = 0 OR NEW.personUid IS NULL) AND tableId = 9; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 9; END");
                        db.execSql("CREATE TRIGGER upd_6 AFTER update ON Clazz FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzMasterChangeSeqNum = 0 OR OLD.clazzMasterChangeSeqNum = NEW.clazzMasterChangeSeqNum) ELSE (NEW.clazzLocalChangeSeqNum = 0 OR OLD.clazzLocalChangeSeqNum = NEW.clazzLocalChangeSeqNum) END) BEGIN UPDATE Clazz SET clazzLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) END),clazzMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) ELSE NEW.clazzMasterChangeSeqNum END) WHERE clazzUid = NEW.clazzUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 6; END");
                        db.execSql("CREATE TRIGGER ins_6 INSTEAD OF INSERT ON Clazz_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzUid = 0 OR NEW.clazzUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 6)) ELSE NEW.clazzUid END; INSERT INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, clazzActive, clazzLocationUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.clazzActive, NEW.clazzLocationUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) ELSE NEW.clazzMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) END), NEW.clazzLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzUid = 0 OR NEW.clazzUid IS NULL) AND tableId = 6; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 6; END");
                        db.execSql("CREATE TRIGGER upd_31 AFTER update ON ClazzMember FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzMemberMasterChangeSeqNum = 0 OR OLD.clazzMemberMasterChangeSeqNum = NEW.clazzMemberMasterChangeSeqNum) ELSE (NEW.clazzMemberLocalChangeSeqNum = 0 OR OLD.clazzMemberLocalChangeSeqNum = NEW.clazzMemberLocalChangeSeqNum) END) BEGIN UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzMemberLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) END),clazzMemberMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) ELSE NEW.clazzMemberMasterChangeSeqNum END) WHERE clazzMemberUid = NEW.clazzMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 31; END");
                        db.execSql("CREATE TRIGGER ins_31 INSTEAD OF INSERT ON ClazzMember_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzMemberUid = 0 OR NEW.clazzMemberUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 31)) ELSE NEW.clazzMemberUid END; INSERT INTO ClazzMember(clazzMemberUid, clazzMemberPersonUid, clazzMemberClazzUid, dateJoined, dateLeft, role, attendancePercentage, clazzMemberActive, clazzMemberLocalChangeSeqNum, clazzMemberMasterChangeSeqNum, clazzMemberLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzMemberPersonUid, NEW.clazzMemberClazzUid, NEW.dateJoined, NEW.dateLeft, NEW.role, NEW.attendancePercentage, NEW.clazzMemberActive, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzMemberLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) ELSE NEW.clazzMemberMasterChangeSeqNum END), NEW.clazzMemberLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzMemberUid = 0 OR NEW.clazzMemberUid IS NULL) AND tableId = 31; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 31; END");
                        db.execSql("CREATE TRIGGER upd_14 AFTER update ON ClazzLog FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzLogChangeMasterChangeSeqNum = 0 OR OLD.clazzLogChangeMasterChangeSeqNum = NEW.clazzLogChangeMasterChangeSeqNum) ELSE (NEW.clazzLogChangeLocalChangeSeqNum = 0 OR OLD.clazzLogChangeLocalChangeSeqNum = NEW.clazzLogChangeLocalChangeSeqNum) END) BEGIN UPDATE ClazzLog SET clazzLogChangeLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) END),clazzLogChangeMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) ELSE NEW.clazzLogChangeMasterChangeSeqNum END) WHERE clazzLogUid = NEW.clazzLogUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 14; END");
                        db.execSql("CREATE TRIGGER ins_14 INSTEAD OF INSERT ON ClazzLog_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzLogUid = 0 OR NEW.clazzLogUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 14)) ELSE NEW.clazzLogUid END; INSERT INTO ClazzLog(clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, done, canceled, numPresent, numAbsent, numPartial, clazzLogScheduleUid, clazzLogChangeMasterChangeSeqNum, clazzLogChangeLocalChangeSeqNum, clazzLogLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzLogClazzUid, NEW.logDate, NEW.timeRecorded, NEW.done, NEW.canceled, NEW.numPresent, NEW.numAbsent, NEW.numPartial, NEW.clazzLogScheduleUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) ELSE NEW.clazzLogChangeMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) END), NEW.clazzLogLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzLogUid = 0 OR NEW.clazzLogUid IS NULL) AND tableId = 14; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 14; END");
                        db.execSql("CREATE TRIGGER upd_15 AFTER update ON ClazzLogAttendanceRecord FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0 OR OLD.clazzLogAttendanceRecordMasterChangeSeqNum = NEW.clazzLogAttendanceRecordMasterChangeSeqNum) ELSE (NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0 OR OLD.clazzLogAttendanceRecordLocalChangeSeqNum = NEW.clazzLogAttendanceRecordLocalChangeSeqNum) END) BEGIN UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) END),clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END) WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 15; END");
                        db.execSql("CREATE TRIGGER ins_15 INSTEAD OF INSERT ON ClazzLogAttendanceRecord_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzLogAttendanceRecordUid = 0 OR NEW.clazzLogAttendanceRecordUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 15)) ELSE NEW.clazzLogAttendanceRecordUid END; INSERT INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordClazzMemberUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordClazzMemberUid, NEW.attendanceStatus, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) END), NEW.clazzLogAttendanceRecordLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzLogAttendanceRecordUid = 0 OR NEW.clazzLogAttendanceRecordUid IS NULL) AND tableId = 15; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 15; END");
                        db.execSql("CREATE TRIGGER upd_16 AFTER update ON FeedEntry FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.feedEntryMasterChangeSeqNum = 0 OR OLD.feedEntryMasterChangeSeqNum = NEW.feedEntryMasterChangeSeqNum) ELSE (NEW.feedEntryLocalChangeSeqNum = 0 OR OLD.feedEntryLocalChangeSeqNum = NEW.feedEntryLocalChangeSeqNum) END) BEGIN UPDATE FeedEntry SET feedEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.feedEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) END),feedEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) ELSE NEW.feedEntryMasterChangeSeqNum END) WHERE feedEntryUid = NEW.feedEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 16; END");
                        db.execSql("CREATE TRIGGER ins_16 INSTEAD OF INSERT ON FeedEntry_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.feedEntryUid = 0 OR NEW.feedEntryUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 16)) ELSE NEW.feedEntryUid END; INSERT INTO FeedEntry(feedEntryUid, feedEntryPersonUid, title, description, link, feedEntryClazzName, deadline, feedEntryHash, feedEntryDone, feedEntryClazzLogUid, dateCreated, feedEntryCheckType, feedEntryLocalChangeSeqNum, feedEntryMasterChangeSeqNum, feedEntryLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.feedEntryPersonUid, NEW.title, NEW.description, NEW.link, NEW.feedEntryClazzName, NEW.deadline, NEW.feedEntryHash, NEW.feedEntryDone, NEW.feedEntryClazzLogUid, NEW.dateCreated, NEW.feedEntryCheckType, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.feedEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) ELSE NEW.feedEntryMasterChangeSeqNum END), NEW.feedEntryLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.feedEntryUid = 0 OR NEW.feedEntryUid IS NULL) AND tableId = 16; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 16; END");
                        db.execSql("CREATE TRIGGER upd_20 AFTER update ON PersonField FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personFieldMasterChangeSeqNum = 0 OR OLD.personFieldMasterChangeSeqNum = NEW.personFieldMasterChangeSeqNum) ELSE (NEW.personFieldLocalChangeSeqNum = 0 OR OLD.personFieldLocalChangeSeqNum = NEW.personFieldLocalChangeSeqNum) END) BEGIN UPDATE PersonField SET personFieldLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) END),personFieldMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) ELSE NEW.personFieldMasterChangeSeqNum END) WHERE personCustomFieldUid = NEW.personCustomFieldUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 20; END");
                        db.execSql("CREATE TRIGGER ins_20 INSTEAD OF INSERT ON PersonField_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personCustomFieldUid = 0 OR NEW.personCustomFieldUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 20)) ELSE NEW.personCustomFieldUid END; INSERT INTO PersonField(personCustomFieldUid, fieldName, labelMessageId, fieldIcon, personFieldMasterChangeSeqNum, personFieldLocalChangeSeqNum, personFieldLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.fieldName, NEW.labelMessageId, NEW.fieldIcon, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) ELSE NEW.personFieldMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) END), NEW.personFieldLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personCustomFieldUid = 0 OR NEW.personCustomFieldUid IS NULL) AND tableId = 20; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 20; END");
                        db.execSql("CREATE TRIGGER upd_18 AFTER update ON PersonCustomFieldValue FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personCustomFieldValueMasterChangeSeqNum = 0 OR OLD.personCustomFieldValueMasterChangeSeqNum = NEW.personCustomFieldValueMasterChangeSeqNum) ELSE (NEW.personCustomFieldValueLocalChangeSeqNum = 0 OR OLD.personCustomFieldValueLocalChangeSeqNum = NEW.personCustomFieldValueLocalChangeSeqNum) END) BEGIN UPDATE PersonCustomFieldValue SET personCustomFieldValueLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personCustomFieldValueLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) END),personCustomFieldValueMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) ELSE NEW.personCustomFieldValueMasterChangeSeqNum END) WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 18; END");
                        db.execSql("CREATE TRIGGER ins_18 INSTEAD OF INSERT ON PersonCustomFieldValue_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personCustomFieldValueUid = 0 OR NEW.personCustomFieldValueUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 18)) ELSE NEW.personCustomFieldValueUid END; INSERT INTO PersonCustomFieldValue(personCustomFieldValueUid, personCustomFieldValuePersonCustomFieldUid, personCustomFieldValuePersonUid, fieldValue, personCustomFieldValueMasterChangeSeqNum, personCustomFieldValueLocalChangeSeqNum, personCustomFieldValueLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.personCustomFieldValuePersonCustomFieldUid, NEW.personCustomFieldValuePersonUid, NEW.fieldValue, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) ELSE NEW.personCustomFieldValueMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personCustomFieldValueLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) END), NEW.personCustomFieldValueLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personCustomFieldValueUid = 0 OR NEW.personCustomFieldValueUid IS NULL) AND tableId = 18; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 18; END");
                        db.execSql("CREATE TRIGGER upd_19 AFTER update ON PersonDetailPresenterField FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personDetailPresenterFieldMasterChangeSeqNum = 0 OR OLD.personDetailPresenterFieldMasterChangeSeqNum = NEW.personDetailPresenterFieldMasterChangeSeqNum) ELSE (NEW.personDetailPresenterFieldLocalChangeSeqNum = 0 OR OLD.personDetailPresenterFieldLocalChangeSeqNum = NEW.personDetailPresenterFieldLocalChangeSeqNum) END) BEGIN UPDATE PersonDetailPresenterField SET personDetailPresenterFieldLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) END),personDetailPresenterFieldMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END) WHERE personDetailPresenterFieldUid = NEW.personDetailPresenterFieldUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 19; END");
                        db.execSql("CREATE TRIGGER ins_19 INSTEAD OF INSERT ON PersonDetailPresenterField_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personDetailPresenterFieldUid = 0 OR NEW.personDetailPresenterFieldUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 19)) ELSE NEW.personDetailPresenterFieldUid END; INSERT INTO PersonDetailPresenterField(personDetailPresenterFieldUid, fieldUid, fieldType, fieldIndex, labelMessageId, fieldIcon, headerMessageId, viewModeVisible, editModeVisible, readyOnly, personDetailPresenterFieldMasterChangeSeqNum, personDetailPresenterFieldLocalChangeSeqNum, personDetailPresenterFieldLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.fieldUid, NEW.fieldType, NEW.fieldIndex, NEW.labelMessageId, NEW.fieldIcon, NEW.headerMessageId, NEW.viewModeVisible, NEW.editModeVisible, NEW.readyOnly, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) END), NEW.personDetailPresenterFieldLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personDetailPresenterFieldUid = 0 OR NEW.personDetailPresenterFieldUid IS NULL) AND tableId = 19; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 19; END");
                        db.execSql("CREATE TRIGGER upd_22 AFTER update ON SelQuestion FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionMasterChangeSeqNum = 0 OR OLD.selQuestionMasterChangeSeqNum = NEW.selQuestionMasterChangeSeqNum) ELSE (NEW.selQuestionLocalChangeSeqNum = 0 OR OLD.selQuestionLocalChangeSeqNum = NEW.selQuestionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END),selQuestionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END) WHERE selQuestionUid = NEW.selQuestionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 22; END");
                        db.execSql("CREATE TRIGGER ins_22 INSTEAD OF INSERT ON SelQuestion_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 22)) ELSE NEW.selQuestionUid END; INSERT INTO SelQuestion(selQuestionUid, questionText, selQuestionSelQuestionSetUid, questionIndex, assignToAllClasses, multiNominations, questionType, questionActive, selQuestionMasterChangeSeqNum, selQuestionLocalChangeSeqNum, selQuestionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.questionText, NEW.selQuestionSelQuestionSetUid, NEW.questionIndex, NEW.assignToAllClasses, NEW.multiNominations, NEW.questionType, NEW.questionActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END), NEW.selQuestionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionUid = 0 OR NEW.selQuestionUid IS NULL) AND tableId = 22; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 22; END");
                        db.execSql("CREATE TRIGGER upd_23 AFTER update ON SelQuestionResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseMasterChangeSeqNum = 0 OR OLD.selQuestionResponseMasterChangeSeqNum = NEW.selQuestionResponseMasterChangeSeqNum) ELSE (NEW.selQuestionResponseLocalChangeSeqNum = 0 OR OLD.selQuestionResponseLocalChangeSeqNum = NEW.selQuestionResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END),selQuestionResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END) WHERE selQuestionResponseUid = NEW.selQuestionResponseUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 23; END");
                        db.execSql("CREATE TRIGGER ins_23 INSTEAD OF INSERT ON SelQuestionResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 23)) ELSE NEW.selQuestionResponseUid END; INSERT INTO SelQuestionResponse(selQuestionResponseUid, selQuestionResponseSelQuestionSetResponseUid, selQuestionResponseSelQuestionUid, selQuestionResponseMasterChangeSeqNum, selQuestionResponseLocalChangeSeqNum, selQuestionResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseSelQuestionSetResponseUid, NEW.selQuestionResponseSelQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END), NEW.selQuestionResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseUid = 0 OR NEW.selQuestionResponseUid IS NULL) AND tableId = 23; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 23; END");
                        db.execSql("CREATE TRIGGER upd_24 AFTER update ON SelQuestionResponseNomination FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionResponseNominationMasterChangeSeqNum = 0 OR OLD.selQuestionResponseNominationMasterChangeSeqNum = NEW.selQuestionResponseNominationMasterChangeSeqNum) ELSE (NEW.selQuestionResponseNominationLocalChangeSeqNum = 0 OR OLD.selQuestionResponseNominationLocalChangeSeqNum = NEW.selQuestionResponseNominationLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionResponseNomination SET selQuestionResponseNominationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END),selQuestionResponseNominationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END) WHERE selQuestionResponseNominationUid = NEW.selQuestionResponseNominationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 24; END");
                        db.execSql("CREATE TRIGGER ins_24 INSTEAD OF INSERT ON SelQuestionResponseNomination_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 24)) ELSE NEW.selQuestionResponseNominationUid END; INSERT INTO SelQuestionResponseNomination(selQuestionResponseNominationUid, selQuestionResponseNominationClazzMemberUid, selQuestionResponseNominationSelQuestionResponseUId, nominationActive, selQuestionResponseNominationMasterChangeSeqNum, selQuestionResponseNominationLocalChangeSeqNum, selQuestionResponseNominationLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionResponseNominationClazzMemberUid, NEW.selQuestionResponseNominationSelQuestionResponseUId, NEW.nominationActive, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END), NEW.selQuestionResponseNominationLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionResponseNominationUid = 0 OR NEW.selQuestionResponseNominationUid IS NULL) AND tableId = 24; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 24; END");
                        db.execSql("CREATE TRIGGER upd_25 AFTER update ON SelQuestionSet FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetMasterChangeSeqNum = 0 OR OLD.selQuestionSetMasterChangeSeqNum = NEW.selQuestionSetMasterChangeSeqNum) ELSE (NEW.selQuestionSetLocalChangeSeqNum = 0 OR OLD.selQuestionSetLocalChangeSeqNum = NEW.selQuestionSetLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END),selQuestionSetMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END) WHERE selQuestionSetUid = NEW.selQuestionSetUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 25; END");
                        db.execSql("CREATE TRIGGER ins_25 INSTEAD OF INSERT ON SelQuestionSet_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 25)) ELSE NEW.selQuestionSetUid END; INSERT INTO SelQuestionSet(selQuestionSetUid, title, selQuestionSetMasterChangeSeqNum, selQuestionSetLocalChangeSeqNum, selQuestionSetLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.title, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END), NEW.selQuestionSetLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetUid = 0 OR NEW.selQuestionSetUid IS NULL) AND tableId = 25; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 25; END");
                        db.execSql("CREATE TRIGGER upd_26 AFTER update ON SelQuestionSetRecognition FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetRecognitionMasterChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionMasterChangeSeqNum = NEW.selQuestionSetRecognitionMasterChangeSeqNum) ELSE (NEW.selQuestionSetRecognitionLocalChangeSeqNum = 0 OR OLD.selQuestionSetRecognitionLocalChangeSeqNum = NEW.selQuestionSetRecognitionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END),selQuestionSetRecognitionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END) WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 26; END");
                        db.execSql("CREATE TRIGGER ins_26 INSTEAD OF INSERT ON SelQuestionSetRecognition_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 26)) ELSE NEW.selQuestionSetRecognitionUid END; INSERT INTO SelQuestionSetRecognition(selQuestionSetRecognitionUid, selQuestionSetRecognitionSelQuestionSetResponseUid, selQuestionSetRecognitionClazzMemberUid, selQuestionSetRecognitionRecognized, selQuestionSetRecognitionMasterChangeSeqNum, selQuestionSetRecognitionLocalChangeSeqNum, selQuestionSetRecognitionLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetRecognitionSelQuestionSetResponseUid, NEW.selQuestionSetRecognitionClazzMemberUid, NEW.selQuestionSetRecognitionRecognized, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END), NEW.selQuestionSetRecognitionLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetRecognitionUid = 0 OR NEW.selQuestionSetRecognitionUid IS NULL) AND tableId = 26; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 26; END");
                        db.execSql("CREATE TRIGGER upd_27 AFTER update ON SelQuestionSetResponse FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionSetResponseMasterChangeSeqNum = 0 OR OLD.selQuestionSetResponseMasterChangeSeqNum = NEW.selQuestionSetResponseMasterChangeSeqNum) ELSE (NEW.selQuestionSetResponseLocalChangeSeqNum = 0 OR OLD.selQuestionSetResponseLocalChangeSeqNum = NEW.selQuestionSetResponseLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END),selQuestionSetResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END) WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 27; END");
                        db.execSql("CREATE TRIGGER ins_27 INSTEAD OF INSERT ON SelQuestionSetResponse_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 27)) ELSE NEW.selQuestionSetResposeUid END; INSERT INTO SelQuestionSetResponse(selQuestionSetResposeUid, selQuestionSetResponseSelQuestionSetUid, selQuestionSetResponseClazzMemberUid, selQuestionSetResponseStartTime, selQuestionSetResponseFinishTime, selQuestionSetResponseRecognitionPercentage, selQuestionSetResponseMasterChangeSeqNum, selQuestionSetResponseLocalChangeSeqNum, selQuestionSetResponseLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.selQuestionSetResponseSelQuestionSetUid, NEW.selQuestionSetResponseClazzMemberUid, NEW.selQuestionSetResponseStartTime, NEW.selQuestionSetResponseFinishTime, NEW.selQuestionSetResponseRecognitionPercentage, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END), NEW.selQuestionSetResponseLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionSetResposeUid = 0 OR NEW.selQuestionSetResposeUid IS NULL) AND tableId = 27; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 27; END");
                        db.execSql("CREATE TRIGGER upd_21 AFTER update ON Schedule FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.scheduleMasterChangeSeqNum = 0 OR OLD.scheduleMasterChangeSeqNum = NEW.scheduleMasterChangeSeqNum) ELSE (NEW.scheduleLocalChangeSeqNum = 0 OR OLD.scheduleLocalChangeSeqNum = NEW.scheduleLocalChangeSeqNum) END) BEGIN UPDATE Schedule SET scheduleLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.scheduleLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) END),scheduleMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) ELSE NEW.scheduleMasterChangeSeqNum END) WHERE scheduleUid = NEW.scheduleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 21; END");
                        db.execSql("CREATE TRIGGER ins_21 INSTEAD OF INSERT ON Schedule_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.scheduleUid = 0 OR NEW.scheduleUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 21)) ELSE NEW.scheduleUid END; INSERT INTO Schedule(scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleActive) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.sceduleStartTime, NEW.scheduleEndTime, NEW.scheduleDay, NEW.scheduleMonth, NEW.scheduleFrequency, NEW.umCalendarUid, NEW.scheduleClazzUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) ELSE NEW.scheduleMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.scheduleLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) END), NEW.scheduleLastChangedBy, NEW.scheduleActive); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.scheduleUid = 0 OR NEW.scheduleUid IS NULL) AND tableId = 21; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 21; END");
                        db.execSql("CREATE TRIGGER upd_17 AFTER update ON Holiday FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.holidayMasterChangeSeqNum = 0 OR OLD.holidayMasterChangeSeqNum = NEW.holidayMasterChangeSeqNum) ELSE (NEW.holidayLocalChangeSeqNum = 0 OR OLD.holidayLocalChangeSeqNum = NEW.holidayLocalChangeSeqNum) END) BEGIN UPDATE Holiday SET holidayLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.holidayLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) END),holidayMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) ELSE NEW.holidayMasterChangeSeqNum END) WHERE holidayUid = NEW.holidayUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 17; END");
                        db.execSql("CREATE TRIGGER ins_17 INSTEAD OF INSERT ON Holiday_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.holidayUid = 0 OR NEW.holidayUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 17)) ELSE NEW.holidayUid END; INSERT INTO Holiday(holidayUid, holidayUMCalendarUid, holidayDate, holidayName, holidayLocalChangeSeqNum, holidayMasterChangeSeqNum, holidayLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.holidayUMCalendarUid, NEW.holidayDate, NEW.holidayName, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.holidayLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) ELSE NEW.holidayMasterChangeSeqNum END), NEW.holidayLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.holidayUid = 0 OR NEW.holidayUid IS NULL) AND tableId = 17; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 17; END");
                        db.execSql("CREATE TRIGGER upd_28 AFTER update ON UMCalendar FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personMasterChangeSeqNum = 0 OR OLD.personMasterChangeSeqNum = NEW.personMasterChangeSeqNum) ELSE (NEW.personLocalChangeSeqNum = 0 OR OLD.personLocalChangeSeqNum = NEW.personLocalChangeSeqNum) END) BEGIN UPDATE UMCalendar SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) ELSE NEW.personMasterChangeSeqNum END) WHERE umCalendarUid = NEW.umCalendarUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 28; END");
                        db.execSql("CREATE TRIGGER ins_28 INSTEAD OF INSERT ON UMCalendar_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.umCalendarUid = 0 OR NEW.umCalendarUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 28)) ELSE NEW.umCalendarUid END; INSERT INTO UMCalendar(umCalendarUid, umCalendarName, personMasterChangeSeqNum, personLocalChangeSeqNum, umCalendarLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.umCalendarName, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) ELSE NEW.personMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) END), NEW.umCalendarLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.umCalendarUid = 0 OR NEW.umCalendarUid IS NULL) AND tableId = 28; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 28; END");
                        db.execSql("CREATE TRIGGER upd_11 AFTER update ON ClazzActivity FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzActivityMasterChangeSeqNum = 0 OR OLD.clazzActivityMasterChangeSeqNum = NEW.clazzActivityMasterChangeSeqNum) ELSE (NEW.clazzActivityLocalChangeSeqNum = 0 OR OLD.clazzActivityLocalChangeSeqNum = NEW.clazzActivityLocalChangeSeqNum) END) BEGIN UPDATE ClazzActivity SET clazzActivityLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) END),clazzActivityMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) ELSE NEW.clazzActivityMasterChangeSeqNum END) WHERE clazzActivityUid = NEW.clazzActivityUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 11; END");
                        db.execSql("CREATE TRIGGER ins_11 INSTEAD OF INSERT ON ClazzActivity_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzActivityUid = 0 OR NEW.clazzActivityUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 11)) ELSE NEW.clazzActivityUid END; INSERT INTO ClazzActivity(clazzActivityUid, clazzActivityClazzActivityChangeUid, clazzActivityGoodFeedback, clazzActivityNotes, clazzActivityLogDate, clazzActivityClazzUid, clazzActivityDone, clazzActivityQuantity, clazzActivityMasterChangeSeqNum, clazzActivityLocalChangeSeqNum, clazzActivityLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzActivityClazzActivityChangeUid, NEW.clazzActivityGoodFeedback, NEW.clazzActivityNotes, NEW.clazzActivityLogDate, NEW.clazzActivityClazzUid, NEW.clazzActivityDone, NEW.clazzActivityQuantity, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) ELSE NEW.clazzActivityMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) END), NEW.clazzActivityLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzActivityUid = 0 OR NEW.clazzActivityUid IS NULL) AND tableId = 11; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 11; END");
                        db.execSql("CREATE TRIGGER upd_32 AFTER update ON ClazzActivityChange FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.clazzActivityChangeMasterChangeSeqNum = 0 OR OLD.clazzActivityChangeMasterChangeSeqNum = NEW.clazzActivityChangeMasterChangeSeqNum) ELSE (NEW.clazzActivityChangeLocalChangeSeqNum = 0 OR OLD.clazzActivityChangeLocalChangeSeqNum = NEW.clazzActivityChangeLocalChangeSeqNum) END) BEGIN UPDATE ClazzActivityChange SET clazzActivityChangeLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) END),clazzActivityChangeMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) ELSE NEW.clazzActivityChangeMasterChangeSeqNum END) WHERE clazzActivityChangeUid = NEW.clazzActivityChangeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 32; END");
                        db.execSql("CREATE TRIGGER ins_32 INSTEAD OF INSERT ON ClazzActivityChange_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.clazzActivityChangeUid = 0 OR NEW.clazzActivityChangeUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 32)) ELSE NEW.clazzActivityChangeUid END; INSERT INTO ClazzActivityChange(clazzActivityChangeUid, clazzActivityChangeTitle, clazzActivityDesc, clazzActivityUnitOfMeasure, clazzActivityChangeActive, clazzActivityChangeLastChangedBy, clazzActivityChangeMasterChangeSeqNum, clazzActivityChangeLocalChangeSeqNum, clazzActivityLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.clazzActivityChangeTitle, NEW.clazzActivityDesc, NEW.clazzActivityUnitOfMeasure, NEW.clazzActivityChangeActive, NEW.clazzActivityChangeLastChangedBy, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) ELSE NEW.clazzActivityChangeMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) END), NEW.clazzActivityLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.clazzActivityChangeUid = 0 OR NEW.clazzActivityChangeUid IS NULL) AND tableId = 32; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 32; END");
                        db.execSql("CREATE TRIGGER upd_42 AFTER update ON ContentEntry FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.contentEntryMasterChangeSeqNum = 0 OR OLD.contentEntryMasterChangeSeqNum = NEW.contentEntryMasterChangeSeqNum) ELSE (NEW.contentEntryLocalChangeSeqNum = 0 OR OLD.contentEntryLocalChangeSeqNum = NEW.contentEntryLocalChangeSeqNum) END) BEGIN UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) END),contentEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) ELSE NEW.contentEntryMasterChangeSeqNum END) WHERE contentEntryUid = NEW.contentEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 42; END");
                        db.execSql("CREATE TRIGGER ins_42 INSTEAD OF INSERT ON ContentEntry_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.contentEntryUid = 0 OR NEW.contentEntryUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 42)) ELSE NEW.contentEntryUid END; INSERT INTO ContentEntry(contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, leaf, publik, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.title, NEW.description, NEW.entryId, NEW.author, NEW.publisher, NEW.licenseType, NEW.licenseName, NEW.licenseUrl, NEW.sourceUrl, NEW.thumbnailUrl, NEW.lastModified, NEW.primaryLanguageUid, NEW.languageVariantUid, NEW.leaf, NEW.publik, NEW.contentTypeFlag, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) ELSE NEW.contentEntryMasterChangeSeqNum END), NEW.contentEntryLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.contentEntryUid = 0 OR NEW.contentEntryUid IS NULL) AND tableId = 42; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 42; END");
                        db.execSql("CREATE TRIGGER upd_3 AFTER update ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.ceccjMasterChangeSeqNum = 0 OR OLD.ceccjMasterChangeSeqNum = NEW.ceccjMasterChangeSeqNum) ELSE (NEW.ceccjLocalChangeSeqNum = 0 OR OLD.ceccjLocalChangeSeqNum = NEW.ceccjLocalChangeSeqNum) END) BEGIN UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.ceccjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) END),ceccjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) ELSE NEW.ceccjMasterChangeSeqNum END) WHERE ceccjUid = NEW.ceccjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 3; END");
                        db.execSql("CREATE TRIGGER ins_3 INSTEAD OF INSERT ON ContentEntryContentCategoryJoin_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.ceccjUid = 0 OR NEW.ceccjUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 3)) ELSE NEW.ceccjUid END; INSERT INTO ContentEntryContentCategoryJoin(ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.ceccjContentEntryUid, NEW.ceccjContentCategoryUid, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.ceccjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) ELSE NEW.ceccjMasterChangeSeqNum END), NEW.ceccjLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.ceccjUid = 0 OR NEW.ceccjUid IS NULL) AND tableId = 3; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 3; END");
                        db.execSql("CREATE TRIGGER upd_4 AFTER update ON ContentEntryContentEntryFileJoin FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.cecefjMasterChangeSeqNum = 0 OR OLD.cecefjMasterChangeSeqNum = NEW.cecefjMasterChangeSeqNum) ELSE (NEW.cecefjLocalChangeSeqNum = 0 OR OLD.cecefjLocalChangeSeqNum = NEW.cecefjLocalChangeSeqNum) END) BEGIN UPDATE ContentEntryContentEntryFileJoin SET cecefjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cecefjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) END),cecefjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) ELSE NEW.cecefjMasterChangeSeqNum END) WHERE cecefjUid = NEW.cecefjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 4; END");
                        db.execSql("CREATE TRIGGER ins_4 INSTEAD OF INSERT ON ContentEntryContentEntryFileJoin_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.cecefjUid = 0 OR NEW.cecefjUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 4)) ELSE NEW.cecefjUid END; INSERT INTO ContentEntryContentEntryFileJoin(cecefjUid, cecefjContentEntryUid, cecefjContentEntryFileUid, cecefjLocalChangeSeqNum, cecefjMasterChangeSeqNum, cecefjLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.cecefjContentEntryUid, NEW.cecefjContentEntryFileUid, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cecefjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) ELSE NEW.cecefjMasterChangeSeqNum END), NEW.cecefjLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.cecefjUid = 0 OR NEW.cecefjUid IS NULL) AND tableId = 4; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 4; END");
                        db.execSql("CREATE TRIGGER upd_5 AFTER update ON ContentEntryFile FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.contentEntryFileMasterChangeSeqNum = 0 OR OLD.contentEntryFileMasterChangeSeqNum = NEW.contentEntryFileMasterChangeSeqNum) ELSE (NEW.contentEntryFileLocalChangeSeqNum = 0 OR OLD.contentEntryFileLocalChangeSeqNum = NEW.contentEntryFileLocalChangeSeqNum) END) BEGIN UPDATE ContentEntryFile SET contentEntryFileLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryFileLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) END),contentEntryFileMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) ELSE NEW.contentEntryFileMasterChangeSeqNum END) WHERE contentEntryFileUid = NEW.contentEntryFileUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 5; END");
                        db.execSql("CREATE TRIGGER ins_5 INSTEAD OF INSERT ON ContentEntryFile_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.contentEntryFileUid = 0 OR NEW.contentEntryFileUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 5)) ELSE NEW.contentEntryFileUid END; INSERT INTO ContentEntryFile(contentEntryFileUid, fileSize, md5sum, lastModified, mimeType, remarks, mobileOptimized, contentEntryFileLocalChangeSeqNum, contentEntryFileMasterChangeSeqNum, contentEntryFileLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.fileSize, NEW.md5sum, NEW.lastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryFileLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) ELSE NEW.contentEntryFileMasterChangeSeqNum END), NEW.contentEntryFileLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.contentEntryFileUid = 0 OR NEW.contentEntryFileUid IS NULL) AND tableId = 5; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 5; END");
                        db.execSql("CREATE TRIGGER upd_7 AFTER update ON ContentEntryParentChildJoin FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.cepcjMasterChangeSeqNum = 0 OR OLD.cepcjMasterChangeSeqNum = NEW.cepcjMasterChangeSeqNum) ELSE (NEW.cepcjLocalChangeSeqNum = 0 OR OLD.cepcjLocalChangeSeqNum = NEW.cepcjLocalChangeSeqNum) END) BEGIN UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cepcjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) END),cepcjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) ELSE NEW.cepcjMasterChangeSeqNum END) WHERE cepcjUid = NEW.cepcjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 7; END");
                        db.execSql("CREATE TRIGGER ins_7 INSTEAD OF INSERT ON ContentEntryParentChildJoin_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.cepcjUid = 0 OR NEW.cepcjUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 7)) ELSE NEW.cepcjUid END; INSERT INTO ContentEntryParentChildJoin(cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.cepcjChildContentEntryUid, NEW.cepcjParentContentEntryUid, NEW.childIndex, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cepcjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) ELSE NEW.cepcjMasterChangeSeqNum END), NEW.cepcjLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.cepcjUid = 0 OR NEW.cepcjUid IS NULL) AND tableId = 7; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 7; END");
                        db.execSql("CREATE TRIGGER upd_8 AFTER update ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.cerejMasterChangeSeqNum = 0 OR OLD.cerejMasterChangeSeqNum = NEW.cerejMasterChangeSeqNum) ELSE (NEW.cerejLocalChangeSeqNum = 0 OR OLD.cerejLocalChangeSeqNum = NEW.cerejLocalChangeSeqNum) END) BEGIN UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cerejLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) END),cerejMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) ELSE NEW.cerejMasterChangeSeqNum END) WHERE cerejUid = NEW.cerejUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 8; END");
                        db.execSql("CREATE TRIGGER ins_8 INSTEAD OF INSERT ON ContentEntryRelatedEntryJoin_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.cerejUid = 0 OR NEW.cerejUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 8)) ELSE NEW.cerejUid END; INSERT INTO ContentEntryRelatedEntryJoin(cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.cerejContentEntryUid, NEW.cerejRelatedEntryUid, NEW.cerejLastChangedBy, NEW.relType, NEW.comment, NEW.cerejRelLanguageUid, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cerejLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) ELSE NEW.cerejMasterChangeSeqNum END)); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.cerejUid = 0 OR NEW.cerejUid IS NULL) AND tableId = 8; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 8; END");
                        db.execSql("CREATE TRIGGER upd_29 AFTER update ON Location FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.locationMasterChangeSeqNum = 0 OR OLD.locationMasterChangeSeqNum = NEW.locationMasterChangeSeqNum) ELSE (NEW.locationLocalChangeSeqNum = 0 OR OLD.locationLocalChangeSeqNum = NEW.locationLocalChangeSeqNum) END) BEGIN UPDATE Location SET locationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.locationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) END),locationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) ELSE NEW.locationMasterChangeSeqNum END) WHERE locationUid = NEW.locationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 29; END");
                        db.execSql("CREATE TRIGGER ins_29 INSTEAD OF INSERT ON Location_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.locationUid = 0 OR NEW.locationUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 29)) ELSE NEW.locationUid END; INSERT INTO Location(locationUid, title, locationDesc, lng, lat, parentLocationUid, timeZone, locationLocalChangeSeqNum, locationMasterChangeSeqNum, locationLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.title, NEW.locationDesc, NEW.lng, NEW.lat, NEW.parentLocationUid, NEW.timeZone, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.locationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) ELSE NEW.locationMasterChangeSeqNum END), NEW.locationLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.locationUid = 0 OR NEW.locationUid IS NULL) AND tableId = 29; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 29; END");
                        db.execSql("CREATE TRIGGER upd_2 AFTER update ON ContentCategorySchema FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.contentCategorySchemaMasterChangeSeqNum = 0 OR OLD.contentCategorySchemaMasterChangeSeqNum = NEW.contentCategorySchemaMasterChangeSeqNum) ELSE (NEW.contentCategorySchemaLocalChangeSeqNum = 0 OR OLD.contentCategorySchemaLocalChangeSeqNum = NEW.contentCategorySchemaLocalChangeSeqNum) END) BEGIN UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategorySchemaLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) END),contentCategorySchemaMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) ELSE NEW.contentCategorySchemaMasterChangeSeqNum END) WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 2; END");
                        db.execSql("CREATE TRIGGER ins_2 INSTEAD OF INSERT ON ContentCategorySchema_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.contentCategorySchemaUid = 0 OR NEW.contentCategorySchemaUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 2)) ELSE NEW.contentCategorySchemaUid END; INSERT INTO ContentCategorySchema(contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.schemaName, NEW.schemaUrl, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategorySchemaLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) ELSE NEW.contentCategorySchemaMasterChangeSeqNum END), NEW.contentCategorySchemaLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.contentCategorySchemaUid = 0 OR NEW.contentCategorySchemaUid IS NULL) AND tableId = 2; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 2; END");
                        db.execSql("CREATE TRIGGER upd_1 AFTER update ON ContentCategory FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.contentCategoryMasterChangeSeqNum = 0 OR OLD.contentCategoryMasterChangeSeqNum = NEW.contentCategoryMasterChangeSeqNum) ELSE (NEW.contentCategoryLocalChangeSeqNum = 0 OR OLD.contentCategoryLocalChangeSeqNum = NEW.contentCategoryLocalChangeSeqNum) END) BEGIN UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategoryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) END),contentCategoryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) ELSE NEW.contentCategoryMasterChangeSeqNum END) WHERE contentCategoryUid = NEW.contentCategoryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 1; END");
                        db.execSql("CREATE TRIGGER ins_1 INSTEAD OF INSERT ON ContentCategory_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.contentCategoryUid = 0 OR NEW.contentCategoryUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 1)) ELSE NEW.contentCategoryUid END; INSERT INTO ContentCategory(contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.ctnCatContentCategorySchemaUid, NEW.name, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategoryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) ELSE NEW.contentCategoryMasterChangeSeqNum END), NEW.contentCategoryLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.contentCategoryUid = 0 OR NEW.contentCategoryUid IS NULL) AND tableId = 1; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 1; END");
                        db.execSql("CREATE TRIGGER upd_13 AFTER update ON Language FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.langMasterChangeSeqNum = 0 OR OLD.langMasterChangeSeqNum = NEW.langMasterChangeSeqNum) ELSE (NEW.langLocalChangeSeqNum = 0 OR OLD.langLocalChangeSeqNum = NEW.langLocalChangeSeqNum) END) BEGIN UPDATE Language SET langLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) END),langMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) ELSE NEW.langMasterChangeSeqNum END) WHERE langUid = NEW.langUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 13; END");
                        db.execSql("CREATE TRIGGER ins_13 INSTEAD OF INSERT ON Language_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.langUid = 0 OR NEW.langUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 13)) ELSE NEW.langUid END; INSERT INTO Language(langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.name, NEW.iso_639_1_standard, NEW.iso_639_2_standard, NEW.iso_639_3_standard, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) ELSE NEW.langMasterChangeSeqNum END), NEW.langLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.langUid = 0 OR NEW.langUid IS NULL) AND tableId = 13; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 13; END");
                        db.execSql("CREATE TRIGGER upd_10 AFTER update ON LanguageVariant FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.langVariantMasterChangeSeqNum = 0 OR OLD.langVariantMasterChangeSeqNum = NEW.langVariantMasterChangeSeqNum) ELSE (NEW.langVariantLocalChangeSeqNum = 0 OR OLD.langVariantLocalChangeSeqNum = NEW.langVariantLocalChangeSeqNum) END) BEGIN UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langVariantLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) END),langVariantMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) ELSE NEW.langVariantMasterChangeSeqNum END) WHERE langVariantUid = NEW.langVariantUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 10; END");
                        db.execSql("CREATE TRIGGER ins_10 INSTEAD OF INSERT ON LanguageVariant_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.langVariantUid = 0 OR NEW.langVariantUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 10)) ELSE NEW.langVariantUid END; INSERT INTO LanguageVariant(langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.langUid, NEW.countryCode, NEW.name, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langVariantLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) ELSE NEW.langVariantMasterChangeSeqNum END), NEW.langVariantLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.langVariantUid = 0 OR NEW.langVariantUid IS NULL) AND tableId = 10; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 10; END");
                        db.execSql("CREATE TRIGGER upd_30 AFTER update ON PersonAuth FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personAuthMasterChangeSeqNum = 0 OR OLD.personAuthMasterChangeSeqNum = NEW.personAuthMasterChangeSeqNum) ELSE (NEW.personAuthLocalChangeSeqNum = 0 OR OLD.personAuthLocalChangeSeqNum = NEW.personAuthLocalChangeSeqNum) END) BEGIN UPDATE PersonAuth SET personAuthLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personAuthLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) END),personAuthMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) ELSE NEW.personAuthMasterChangeSeqNum END) WHERE personAuthUid = NEW.personAuthUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 30; END");
                        db.execSql("CREATE TRIGGER ins_30 INSTEAD OF INSERT ON PersonAuth_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personAuthUid = 0 OR NEW.personAuthUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 30)) ELSE NEW.personAuthUid END; INSERT INTO PersonAuth(personAuthUid, passwordHash, personAuthLocalChangeSeqNum, personAuthMasterChangeSeqNum, lastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.passwordHash, (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personAuthLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) END), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) ELSE NEW.personAuthMasterChangeSeqNum END), NEW.lastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personAuthUid = 0 OR NEW.personAuthUid IS NULL) AND tableId = 30; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 30; END");
                        db.execSql("CREATE TRIGGER upd_45 AFTER update ON Role FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.roleMasterCsn = 0 OR OLD.roleMasterCsn = NEW.roleMasterCsn) ELSE (NEW.roleLocalCsn = 0 OR OLD.roleLocalCsn = NEW.roleLocalCsn) END) BEGIN UPDATE Role SET roleLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.roleLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) END),roleMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) ELSE NEW.roleMasterCsn END) WHERE roleUid = NEW.roleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 45; END");
                        db.execSql("CREATE TRIGGER ins_45 INSTEAD OF INSERT ON Role_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.roleUid = 0 OR NEW.roleUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 45)) ELSE NEW.roleUid END; INSERT INTO Role(roleUid, roleName, roleMasterCsn, roleLocalCsn, roleLastChangedBy, rolePermissions) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.roleName, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) ELSE NEW.roleMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.roleLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) END), NEW.roleLastChangedBy, NEW.rolePermissions); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.roleUid = 0 OR NEW.roleUid IS NULL) AND tableId = 45; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 45; END");
                        db.execSql("CREATE TRIGGER upd_47 AFTER update ON EntityRole FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.erMasterCsn = 0 OR OLD.erMasterCsn = NEW.erMasterCsn) ELSE (NEW.erLocalCsn = 0 OR OLD.erLocalCsn = NEW.erLocalCsn) END) BEGIN UPDATE EntityRole SET erLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.erLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) END),erMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) ELSE NEW.erMasterCsn END) WHERE erUid = NEW.erUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 47; END");
                        db.execSql("CREATE TRIGGER ins_47 INSTEAD OF INSERT ON EntityRole_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.erUid = 0 OR NEW.erUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 47)) ELSE NEW.erUid END; INSERT INTO EntityRole(erUid, erMasterCsn, erLocalCsn, erLastChangedBy, erTableId, erEntityUid, erGroupUid, erRoleUid) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) ELSE NEW.erMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.erLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) END), NEW.erLastChangedBy, NEW.erTableId, NEW.erEntityUid, NEW.erGroupUid, NEW.erRoleUid); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.erUid = 0 OR NEW.erUid IS NULL) AND tableId = 47; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 47; END");
                        db.execSql("CREATE TRIGGER upd_43 AFTER update ON PersonGroup FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.groupMasterCsn = 0 OR OLD.groupMasterCsn = NEW.groupMasterCsn) ELSE (NEW.groupLocalCsn = 0 OR OLD.groupLocalCsn = NEW.groupLocalCsn) END) BEGIN UPDATE PersonGroup SET groupLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) END),groupMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) ELSE NEW.groupMasterCsn END) WHERE groupUid = NEW.groupUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 43; END");
                        db.execSql("CREATE TRIGGER ins_43 INSTEAD OF INSERT ON PersonGroup_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.groupUid = 0 OR NEW.groupUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 43)) ELSE NEW.groupUid END; INSERT INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupName, groupPersonUid) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) ELSE NEW.groupMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) END), NEW.groupLastChangedBy, NEW.groupName, NEW.groupPersonUid); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.groupUid = 0 OR NEW.groupUid IS NULL) AND tableId = 43; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 43; END");
                        db.execSql("CREATE TRIGGER upd_44 AFTER update ON PersonGroupMember FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.groupMemberMasterCsn = 0 OR OLD.groupMemberMasterCsn = NEW.groupMemberMasterCsn) ELSE (NEW.groupMemberLocalCsn = 0 OR OLD.groupMemberLocalCsn = NEW.groupMemberLocalCsn) END) BEGIN UPDATE PersonGroupMember SET groupMemberLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupMemberLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) END),groupMemberMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) ELSE NEW.groupMemberMasterCsn END) WHERE groupMemberUid = NEW.groupMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 44; END");
                        db.execSql("CREATE TRIGGER ins_44 INSTEAD OF INSERT ON PersonGroupMember_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.groupMemberUid = 0 OR NEW.groupMemberUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 44)) ELSE NEW.groupMemberUid END; INSERT INTO PersonGroupMember(groupMemberUid, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) ELSE NEW.groupMemberMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupMemberLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) END), NEW.groupMemberLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.groupMemberUid = 0 OR NEW.groupMemberUid IS NULL) AND tableId = 44; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 44; END");
                        db.execSql("CREATE TRIGGER upd_52 AFTER update ON SelQuestionOption FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.selQuestionOptionMasterChangeSeqNum = 0 OR OLD.selQuestionOptionMasterChangeSeqNum = NEW.selQuestionOptionMasterChangeSeqNum) ELSE (NEW.selQuestionOptionLocalChangeSeqNum = 0 OR OLD.selQuestionOptionLocalChangeSeqNum = NEW.selQuestionOptionLocalChangeSeqNum) END) BEGIN UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END),selQuestionOptionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END) WHERE selQuestionOptionUid = NEW.selQuestionOptionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 52; END");
                        db.execSql("CREATE TRIGGER ins_52 INSTEAD OF INSERT ON SelQuestionOption_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 52)) ELSE NEW.selQuestionOptionUid END; INSERT INTO SelQuestionOption(selQuestionOptionUid, optionText, selQuestionOptionQuestionUid, selQuestionOptionMasterChangeSeqNum, selQuestionOptionLocalChangeSeqNum, selQuestionOptionLastChangedBy, optionActive) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.optionText, NEW.selQuestionOptionQuestionUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END), NEW.selQuestionOptionLastChangedBy, NEW.optionActive); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.selQuestionOptionUid = 0 OR NEW.selQuestionOptionUid IS NULL) AND tableId = 52; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 52; END");
                        db.execSql("CREATE TRIGGER upd_48 AFTER update ON PersonLocationJoin FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.plMasterCsn = 0 OR OLD.plMasterCsn = NEW.plMasterCsn) ELSE (NEW.plLocalCsn = 0 OR OLD.plLocalCsn = NEW.plLocalCsn) END) BEGIN UPDATE PersonLocationJoin SET plLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.plLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) END),plMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) ELSE NEW.plMasterCsn END) WHERE personLocationUid = NEW.personLocationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 48; END");
                        db.execSql("CREATE TRIGGER ins_48 INSTEAD OF INSERT ON PersonLocationJoin_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personLocationUid = 0 OR NEW.personLocationUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 48)) ELSE NEW.personLocationUid END; INSERT INTO PersonLocationJoin(personLocationUid, personLocationPersonUid, personLocationLocationUid, plMasterCsn, plLocalCsn, plLastChangedBy) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.personLocationPersonUid, NEW.personLocationLocationUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) ELSE NEW.plMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.plLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) END), NEW.plLastChangedBy); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personLocationUid = 0 OR NEW.personLocationUid IS NULL) AND tableId = 48; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 48; END");
                        db.execSql("CREATE TRIGGER upd_50 AFTER update ON PersonPicture FOR EACH ROW WHEN (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (NEW.personPictureMasterCsn = 0 OR OLD.personPictureMasterCsn = NEW.personPictureMasterCsn) ELSE (NEW.personPictureLocalCsn = 0 OR OLD.personPictureLocalCsn = NEW.personPictureLocalCsn) END) BEGIN UPDATE PersonPicture SET personPictureLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personPictureLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) END),personPictureMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) ELSE NEW.personPictureMasterCsn END) WHERE personPictureUid = NEW.personPictureUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1 WHERE tableId = 50; END");
                        db.execSql("CREATE TRIGGER ins_50 INSTEAD OF INSERT ON PersonPicture_spk_view BEGIN INSERT INTO _lastsyncablepk (lastpk) SELECT CASE WHEN NEW.personPictureUid = 0 OR NEW.personPictureUid IS NULL THEN (SELECT (SELECT deviceBits << 32 FROM SyncDeviceBits)  | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = 50)) ELSE NEW.personPictureUid END; INSERT INTO PersonPicture(personPictureUid, personPicturePersonUid, personPictureMasterCsn, personPictureLocalCsn, personPictureLastChangedBy, fileSize, picTimestamp, mimeType) VALUES ((SELECT lastPk FROM _lastsyncablepk ORDER BY id DESC LIMIT 1), NEW.personPicturePersonUid, (SELECT CASE WHEN  (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) ELSE NEW.personPictureMasterCsn END), (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personPictureLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) END), NEW.personPictureLastChangedBy, NEW.fileSize, NEW.picTimestamp, NEW.mimeType); UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE (NEW.personPictureUid = 0 OR NEW.personPictureUid IS NULL) AND tableId = 50; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 50; END");


                        /**
                         * There are situations where the SyncablePrimaryKey isn't created for
                         * an entity.
                         */
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 1, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 1)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 2, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 2)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 3, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 3)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 4, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 4)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 5, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 5)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 6, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 6)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 7, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 7)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 8, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 8)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 9, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 9)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 10, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 10)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 11, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 11)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 13, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 13)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 14, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 14)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 15, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 15)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 16, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 16)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 17, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 17)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 18, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 18)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 19, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 19)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 20, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 20)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 21, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 21)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 22, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 22)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 23, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 23)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 24, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 24)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 25, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 25)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 26, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 26)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 27, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 27)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 28, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 28)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 29, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 29)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 30, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 30)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 31, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 31)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 32, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 32)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 42, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 42)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 43, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 43)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 44, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 44)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 45, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 45)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 47, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 47)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 48, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 48)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 50, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 50)");
                        db.execSql("INSERT INTO SyncablePrimaryKey(tableId, sequenceNumber) SELECT 52, 1 WHERE NOT EXISTS(SELECT 1 FROM SyncablePrimaryKey WHERE tableId = 52)");

                        break;
                    case UmDbType.TYPE_POSTGRES:

                        System.out.println("RUNNING MIGRATIONS >>>");

                        //For all entites: Drop all triggers and functions and re create them.

                        //New Entities: Create sequence, table and upate syncstatus

                        //Existing entities update: Create Seq, update def of pk to link with Sequence.

                        //Other bits: Update SyncDeviceBits with new bits value


                        /**
                         * DROP All triggers and functions
                         */
                        //Functions
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_9_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_6_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_31_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_14_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_15_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_16_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_20_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_18_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_19_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_22_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_23_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_24_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_25_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_26_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_27_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_21_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_17_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_28_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_11_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_32_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_42_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_3_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_4_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_5_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_7_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_8_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_29_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_2_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_1_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_13_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_10_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_30_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_45_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_47_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_43_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_44_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_52_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_48_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS inc_csn_50_fn()");

                        //triggers
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_9_trig ON Person");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_6_trig ON Clazz");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_31_trig ON ClazzMember");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_14_trig ON ClazzLog");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_15_trig ON ClazzLogAttendanceRecord");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_16_trig ON FeedEntry");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_20_trig ON PersonField");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_18_trig ON PersonCustomFieldValue");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_19_trig ON PersonDetailPresenterField");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_22_trig ON SelQuestion");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_23_trig ON SelQuestionResponse");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_24_trig ON SelQuestionResponseNomination");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_25_trig ON SelQuestionSet");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_26_trig ON SelQuestionSetRecognition");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_27_trig ON SelQuestionSetResponse");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_21_trig ON Schedule");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_17_trig ON Holiday");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_28_trig ON UMCalendar");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_11_trig ON ClazzActivity");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_32_trig ON ClazzActivityChange");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_42_trig ON ContentEntry");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_3_trig ON ContentEntryContentCategoryJoin");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_4_trig ON ContentEntryContentEntryFileJoin");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_5_trig ON ContentEntryFile");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_7_trig ON ContentEntryParentChildJoin");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_8_trig ON ContentEntryRelatedEntryJoin");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_29_trig ON Location");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_2_trig ON ContentCategorySchema");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_1_trig ON ContentCategory");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_13_trig ON Language");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_10_trig ON LanguageVariant");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_30_trig ON PersonAuth");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_45_trig ON Role");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_47_trig ON EntityRole");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_43_trig ON PersonGroup");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_44_trig ON PersonGroupMember");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_52_trig ON SelQuestionOption");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_48_trig ON PersonLocationJoin");
                        db.execSql("DROP TRIGGER IF EXISTS inc_csn_50_trig ON PersonPicture");


                        //Drop beta trigger and functions :

                        //Beta trigger if they exist:
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_person_trigger ON Person");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazz_trigger ON Clazz");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazzmember_trigger ON ClazzMember");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazzlog_trigger ON ClazzLog");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazzlogattendancerecord_trigger ON ClazzLogAttendanceRecord");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_feedentry_trigger ON FeedEntry");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personfield_trigger ON PersonField");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personcustomfieldvalue_trigger ON PersonCustomFieldValue");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_persondetailpresenterfield_trigger ON PersonDetailPresenterField");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestion_trigger ON SocialNominationQuestion");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestionresponse_trigger ON SocialNominationQuestionResponse");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestionresponsenomination_trigger ON SocialNominationQuestionResponseNomination");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestionset_trigger ON SocialNominationQuestionSet");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestionsetrecognition_trigger ON SocialNominationQuestionSetRecognition");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_socialnominationquestionsetresponse_trigger ON SocialNominationQuestionSetResponse");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_schedule_trigger ON Schedule");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_holiday_trigger ON Holiday");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_umcalendar_trigger ON UMCalendar");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazzactivity_trigger ON ClazzActivity");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_clazzactivitychange_trigger ON ClazzActivityChange");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententry_trigger ON ContentEntry");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententrycontentcategoryjoin_trigger ON ContentEntryContentCategoryJoin");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententrycontententryfilejoin_trigger ON ContentEntryContentEntryFileJoin");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryfile_trigger ON ContentEntryFile");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryparentchildjoin_trigger ON ContentEntryParentChildJoin");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contententryrelatedentryjoin_trigger ON ContentEntryRelatedEntryJoin");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_location_trigger ON Location");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contentcategoryschema_trigger ON ContentCategorySchema");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_contentcategory_trigger ON ContentCategory");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_language_trigger ON Language");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_languagevariant_trigger ON LanguageVariant");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personauth_trigger ON PersonAuth");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_role_trigger ON Role");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_entityrole_trigger ON EntityRole");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroup_trigger ON PersonGroup");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_persongroupmember_trigger ON PersonGroupMember");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personlocationjoin_trigger ON PersonLocationJoin");
                        db.execSql("DROP TRIGGER IF EXISTS increment_csn_personpicture_trigger ON PersonPicture");

                        //Beta functions if they exist:
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_person_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazz_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzmember_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzlog_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzlogattendancerecord_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_feedentry_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personfield_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personcustomfieldvalue_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_persondetailpresenterfield_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestion_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestionresponse_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestionresponsenomination_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestionset_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestionsetrecognition_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_socialnominationquestionsetresponse_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_schedule_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_holiday_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_umcalendar_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzactivity_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_clazzactivitychange_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententry_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontentcategoryjoin_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententrycontententryfilejoin_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryfile_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryparentchildjoin_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contententryrelatedentryjoin_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_location_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategoryschema_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_contentcategory_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_language_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_languagevariant_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personauth_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_role_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_entityrole_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroup_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_persongroupmember_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personlocationjoin_fn()");
                        db.execSql("DROP FUNCTION IF EXISTS increment_csn_personpicture_fn()");



                        /**
                         * New device bits
                         */
                        int deviceBits = new Random().nextInt();

                        //Sync helper entities:

                        /**
                         * SyncDeviceBits has an extra boolean
                         * private boolean master;*
                         */
                        db.execSql("ALTER TABLE SyncDeviceBits ADD COLUMN master BOOL");

                        /**
                         * Reset SyncDeviceBits
                         */
                        db.execSql("UPDATE SyncDeviceBits SET deviceBits = " + deviceBits +
                                ", master = TRUE");



                        /**
                        First of all SyncStatus needs changed
                         localChangeSeqNum removed (always 1)
                         masterChangeSeqNum renamed to nextChangeSeqNum
                         */
                        //db.execSql("ALTER TABLE SyncStatus ADD COLUMN nextChangeSeqNum BIGINT");
                        db.execSql("ALTER TABLE SyncStatus RENAME COLUMN masterchangeseqnum TO nextChangeSeqNum");
                        db.execSql("ALTER TABLE SyncStatus DROP COLUMN localchangeseqnum ");
                        //db.execSql("DELETE FROM SyncStatus");

                        /*
                        SyncablePrimaryKey
                         */
                        db.execSql("CREATE TABLE IF NOT EXISTS  SyncablePrimaryKey  " +
                                "( tableId  INTEGER PRIMARY KEY  NOT NULL ,  " +
                                "sequenceNumber  INTEGER)");



                        //Sync entities:


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
                        db.execSql("ALTER TABLE ClazzLog ADD COLUMN canceled BOOL");
                        db.execSql("ALTER TABLE ClazzLog ADD COLUMN clazzLogScheduleUid BIGINT");
                        //Create sequence
                        db.execSql("CREATE SEQUENCE spk_seq_14 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ClazzLog ALTER COLUMN clazzLogUid SET DEFAULT NEXTVAL('spk_seq_14')");


                        /*
                        FeedEntry : added three fields
                            private long feedEntryClazzLogUid;
                            private long dateCreated;
                            private int feedEntryCheckType
                        */
                        db.execSql("ALTER TABLE FeedEntry ADD COLUMN feedEntryClazzLogUid BIGINT");
                        db.execSql("ALTER TABLE FeedEntry ADD COLUMN dateCreated BIGINT");
                        db.execSql("ALTER TABLE FeedEntry ADD COLUMN feedEntryCheckType INTEGER");
                        //Create sequence
                        db.execSql("CREATE SEQUENCE spk_seq_16 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE FeedEntry ALTER COLUMN feedEntryUid SET DEFAULT NEXTVAL('spk_seq_16') ");

                        /*
                        Location: added one fields
                            private String timeZone;
                        */
                        db.execSql("ALTER TABLE Location ADD COLUMN timeZone TEXT");
                        //Sequence
                        db.execSql("CREATE SEQUENCE spk_seq_29 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));;
                        //PK <-> Sequence
                        db.execSql("ALTER TABLE Location ALTER COLUMN locationUid SET DEFAULT NEXTVAL ('spk_seq_29') ");

                        /*
                        ScheduledCheck : New entity
                        */
                        //BEGIN Create ScheduledCheck (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ScheduledCheck  ( scheduledCheckId  SERIAL PRIMARY KEY  NOT NULL ,  checkTime  BIGINT,  checkType  INTEGER,  checkUuid  TEXT,  checkParameters  TEXT,  scClazzLogUid  BIGINT)");
                        //END Create ScheduledCheck (PostgreSQL)
                        //TODO: Check: do we need to create Sequence, etc for ScheduledCheck ?

                        /*
                        SelQuestion - New entity
                        */
                        //BEGIN Create SelQuestion (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_22 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestion  ( selQuestionUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_22') ,  questionText  TEXT,  selQuestionSelQuestionSetUid  BIGINT,  questionIndex  INTEGER,  assignToAllClasses  BOOL,  multiNominations  BOOL,  questionType  INTEGER,  questionActive  BOOL,  selQuestionMasterChangeSeqNum  BIGINT,  selQuestionLocalChangeSeqNum  BIGINT,  selQuestionLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 22");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (22, 1, 0, 0)");
                        //END Create SelQuestion (PostgreSQL)

                        /*
                        SelQuestionOption - New entity
                        */
                        //BEGIN Create SelQuestionOption (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_52 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionOption  ( selQuestionOptionUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_52') ,  optionText  TEXT,  selQuestionOptionQuestionUid  BIGINT,  selQuestionOptionMasterChangeSeqNum  BIGINT,  selQuestionOptionLocalChangeSeqNum  BIGINT,  selQuestionOptionLastChangedBy  INTEGER,  optionActive  BOOL)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 52");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (52, 1, 0, 0)");
                        //END Create SelQuestionOption (PostgreSQL)

                        /*
                        SelQuestionResponse - New entity
                        */
                        //BEGIN Create SelQuestionResponse (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_23 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionResponse  ( selQuestionResponseUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_23') ,  selQuestionResponseSelQuestionSetResponseUid  BIGINT,  selQuestionResponseSelQuestionUid  BIGINT,  selQuestionResponseMasterChangeSeqNum  BIGINT,  selQuestionResponseLocalChangeSeqNum  BIGINT,  selQuestionResponseLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 23");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (23, 1, 0, 0)");
                        //END Create SelQuestionResponse (PostgreSQL)

                        /*
                        SelQuestionResponseNomination - New entity
                        */
                        //BEGIN Create SelQuestionResponseNomination (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_24 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionResponseNomination  ( selQuestionResponseNominationUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_24') ,  selQuestionResponseNominationClazzMemberUid  BIGINT,  selQuestionResponseNominationSelQuestionResponseUId  BIGINT,  nominationActive  BOOL,  selQuestionResponseNominationMasterChangeSeqNum  BIGINT,  selQuestionResponseNominationLocalChangeSeqNum  BIGINT,  selQuestionResponseNominationLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 24");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (24, 1, 0, 0)");
                        //END Create SelQuestionResponseNomination (PostgreSQL)

                        /*
                        SelQuestionSet - New entity
                        */
                        //BEGIN Create SelQuestionSet (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_25 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSet  ( selQuestionSetUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_25') ,  title  TEXT,  selQuestionSetMasterChangeSeqNum  BIGINT,  selQuestionSetLocalChangeSeqNum  BIGINT,  selQuestionSetLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 25");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (25, 1, 0, 0)");
                        //END Create SelQuestionSet (PostgreSQL)

                        /*
                        SelQuestionSetRecognition - New entity
                        */
                        //BEGIN Create SelQuestionSetRecognition (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_26 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSetRecognition  ( selQuestionSetRecognitionUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_26') ,  selQuestionSetRecognitionSelQuestionSetResponseUid  BIGINT,  selQuestionSetRecognitionClazzMemberUid  BIGINT,  selQuestionSetRecognitionRecognized  BOOL,  selQuestionSetRecognitionMasterChangeSeqNum  BIGINT,  selQuestionSetRecognitionLocalChangeSeqNum  BIGINT,  selQuestionSetRecognitionLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 26");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (26, 1, 0, 0)");
                        //END Create SelQuestionSetRecognition (PostgreSQL)

                        /*
                        SelQuestionSetResponse - New entity
                        */
                        //BEGIN Create SelQuestionSetResponse (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_27 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  SelQuestionSetResponse  ( selQuestionSetResposeUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_27') ,  selQuestionSetResponseSelQuestionSetUid  BIGINT,  selQuestionSetResponseClazzMemberUid  BIGINT,  selQuestionSetResponseStartTime  BIGINT,  selQuestionSetResponseFinishTime  BIGINT,  selQuestionSetResponseRecognitionPercentage  FLOAT,  selQuestionSetResponseMasterChangeSeqNum  BIGINT,  selQuestionSetResponseLocalChangeSeqNum  BIGINT,  selQuestionSetResponseLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 27");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (27, 1, 0, 0)");
                        //END Create SelQuestionSetResponse (PostgreSQL)


                        /*
                         * Download Set
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadSet");
                        //BEGIN Create DownloadSet (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSet  ( dsUid  SERIAL PRIMARY KEY  NOT NULL ,  destinationDir  TEXT,  meteredNetworkAllowed  BOOL,  dsRootContentEntryUid  BIGINT)");
                        //END Create DownloadSet (PostgreSQL)

                        //BEGIN Create DownloadSetItem (PostgreSQL)
                        db.execSql("DROP TABLE IF EXISTS DownloadSetItem");
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSetItem  ( dsiUid  SERIAL PRIMARY KEY  NOT NULL ,  dsiDsUid  BIGINT,  dsiContentEntryUid  BIGINT)");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiContentEntryUid  ON  DownloadSetItem  ( dsiContentEntryUid  )");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiDsUid  ON  DownloadSetItem  ( dsiDsUid  )");
                        //END Create DownloadSetItem (PostgreSQL)

                        /*
                         * DownloadSetItem
                         */
                        db.execSql("DROP TABLE  IF EXISTS DownloadSetItem");
                        //BEGIN Create DownloadSetItem (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadSetItem  ( dsiUid  SERIAL PRIMARY KEY  NOT NULL ,  dsiDsUid  BIGINT,  dsiContentEntryUid  BIGINT)");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiContentEntryUid  ON  DownloadSetItem  ( dsiContentEntryUid  )");
                        db.execSql("CREATE INDEX  index_DownloadSetItem_dsiDsUid  ON  DownloadSetItem  ( dsiDsUid  )");
                        //END Create DownloadSetItem (PostgreSQL)

                        /*
                         * Network Node
                         * Removed wifiDirectLastUpdated column
                         */
                        db.execSql("DROP TABLE IF EXISTS NetworkNode");
                        //BEGIN Create NetworkNode (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  NetworkNode  ( nodeId  SERIAL PRIMARY KEY  NOT NULL ,  bluetoothMacAddress  TEXT,  ipAddress  TEXT,  wifiDirectMacAddress  TEXT,  deviceWifiDirectName  TEXT,  lastUpdateTimeStamp  BIGINT,  networkServiceLastUpdated  BIGINT,  nsdServiceName  TEXT,  port  INTEGER,  wifiDirectDeviceStatus  INTEGER)");
                        //END Create NetworkNode (PostgreSQL)

                        /*
                         * EntryStatusResponse updated several columns, etc
                         *
                         */
                        db.execSql("DROP TABLE IF EXISTS EntryStatusResponse");
                        //BEGIN Create EntryStatusResponse (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  EntryStatusResponse  ( erId  SERIAL PRIMARY KEY  NOT NULL ,  erContentEntryFileUid  BIGINT,  responseTime  BIGINT,  erNodeId  BIGINT,  available  BOOL)");
                        //END Create EntryStatusResponse (PostgreSQL)

                        /*
                         * Download job
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadJob");
                        //BEGIN Create DownloadJob (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJob  ( djUid  SERIAL PRIMARY KEY  NOT NULL ,  djDsUid  BIGINT,  timeCreated  BIGINT,  timeRequested  BIGINT,  timeCompleted  BIGINT,  totalBytesToDownload  BIGINT,  bytesDownloadedSoFar  BIGINT,  djStatus  INTEGER)");
                        //END Create DownloadJob (PostgreSQL)

                        /*
                         * DownloadJobItem
                         */
                        db.execSql("DROP TABLE IF EXISTS DownloadJobItem");
                        //BEGIN Create DownloadJobItem (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  DownloadJobItem  ( djiUid  SERIAL PRIMARY KEY  NOT NULL ,  djiDsiUid  BIGINT,  djiDjUid  BIGINT,  djiContentEntryFileUid  BIGINT,  downloadedSoFar  BIGINT,  downloadLength  BIGINT,  currentSpeed  BIGINT,  timeStarted  BIGINT,  timeFinished  BIGINT,  djiStatus  INTEGER,  destinationFile  TEXT,  numAttempts  INTEGER)");
                        db.execSql("CREATE INDEX  index_DownloadJobItem_timeStarted  ON  DownloadJobItem  ( timeStarted  )");
                        db.execSql("CREATE INDEX  index_DownloadJobItem_djiStatus  ON  DownloadJobItem  ( djiStatus  )");
                        //END Create DownloadJobItem (PostgreSQL)

                        /*
                         * Content Entry
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntry");
                        //BEGIN Create ContentEntry (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_42 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntry  ( contentEntryUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_42') ,  title  TEXT,  description  TEXT,  entryId  TEXT,  author  TEXT,  publisher  TEXT,  licenseType  INTEGER,  licenseName  TEXT,  licenseUrl  TEXT,  sourceUrl  TEXT,  thumbnailUrl  TEXT,  lastModified  BIGINT,  primaryLanguageUid  BIGINT,  languageVariantUid  BIGINT,  leaf  BOOL,  publik  BOOL,  contentTypeFlag  INTEGER,  contentEntryLocalChangeSeqNum  BIGINT,  contentEntryMasterChangeSeqNum  BIGINT,  contentEntryLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 42");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (42, 1, 0, 0)");
                        db.execSql("CREATE INDEX  index_ContentEntry_primaryLanguageUid  ON  ContentEntry  ( primaryLanguageUid  )");
                        //END Create ContentEntry (PostgreSQL)

                        /*
                         * ContentEntryContentCategoryJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryContentCategoryJoin");
                        //BEGIN Create ContentEntryContentCategoryJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_3 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryContentCategoryJoin  ( ceccjUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_3') ,  ceccjContentEntryUid  BIGINT,  ceccjContentCategoryUid  BIGINT,  ceccjLocalChangeSeqNum  BIGINT,  ceccjMasterChangeSeqNum  BIGINT,  ceccjLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 3");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (3, 1, 0, 0)");
                        //END Create ContentEntryContentCategoryJoin (PostgreSQL)


                        /*
                         * ContentEntryFile
                         */

                        //BEGIN Create ContentEntryFile (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_5 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryFile  ( contentEntryFileUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_5') ,  fileSize  BIGINT,  md5sum  TEXT,  lastModified  BIGINT,  mimeType  TEXT,  remarks  TEXT,  mobileOptimized  BOOL,  contentEntryFileLocalChangeSeqNum  BIGINT,  contentEntryFileMasterChangeSeqNum  BIGINT,  contentEntryFileLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 5");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (5, 1, 0, 0)");
                        db.execSql("CREATE INDEX  index_ContentEntryFile_lastModified  ON  ContentEntryFile  ( lastModified  )");
                        //END Create ContentEntryFile (PostgreSQL)

                        /*
                         * ContentEntryContentEntryFileJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryContentEntryFileJoin");
                        //BEGIN Create ContentEntryContentEntryFileJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_4 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryContentEntryFileJoin  ( cecefjUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_4') ,  cecefjContentEntryUid  BIGINT,  cecefjContentEntryFileUid  BIGINT,  cecefjLocalChangeSeqNum  BIGINT,  cecefjMasterChangeSeqNum  BIGINT,  cecefjLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 4");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (4, 1, 0, 0)");
                        db.execSql("CREATE INDEX  index_ContentEntryContentEntryFileJoin_cecefjContentEntryFileUid  ON  ContentEntryContentEntryFileJoin  ( cecefjContentEntryFileUid  )");
                        db.execSql("CREATE INDEX  index_ContentEntryContentEntryFileJoin_cecefjContentEntryUid  ON  ContentEntryContentEntryFileJoin  ( cecefjContentEntryUid  )");
                        //END Create ContentEntryContentEntryFileJoin (PostgreSQL)

                        /*
                         * ContentEntryParentChildJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryParentChildJoin");
                        //BEGIN Create ContentEntryParentChildJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_7 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryParentChildJoin  ( cepcjUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_7') ,  cepcjChildContentEntryUid  BIGINT,  cepcjParentContentEntryUid  BIGINT,  childIndex  INTEGER,  cepcjLocalChangeSeqNum  BIGINT,  cepcjMasterChangeSeqNum  BIGINT,  cepcjLastChangedBy  INTEGER)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 7");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (7, 1, 0, 0)");

                        //END Create ContentEntryParentChildJoin (PostgreSQL)

                        /*
                         * ContentEntryRelatedEntryJoin
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryRelatedEntryJoin");
                        //BEGIN Create ContentEntryRelatedEntryJoin (PostgreSQL)
                        db.execSql("CREATE SEQUENCE spk_seq_8 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryRelatedEntryJoin  ( cerejUid  BIGINT PRIMARY KEY  DEFAULT NEXTVAL('spk_seq_8') ,  cerejContentEntryUid  BIGINT,  cerejRelatedEntryUid  BIGINT,  cerejLastChangedBy  INTEGER,  relType  INTEGER,  comment  TEXT,  cerejRelLanguageUid  BIGINT,  cerejLocalChangeSeqNum  BIGINT,  cerejMasterChangeSeqNum  BIGINT)");
                        db.execSql("DELETE FROM SyncStatus WHERE tableId = 8");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) VALUES (8, 1, 0, 0)");
                        //END Create ContentEntryRelatedEntryJoin (PostgreSQL)

                        /*
                         * ScrapeQueueItem
                         */
                        db.execSql("DROP TABLE IF EXISTS ScrapeQueueItem");
                        //BEGIN Create ScrapeQueueItem (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ScrapeQueueItem  ( sqiUid  SERIAL PRIMARY KEY  NOT NULL ,  sqiContentEntryParentUid  BIGINT,  destDir  TEXT,  scrapeUrl  TEXT,  status  INTEGER,  runId  INTEGER,  itemType  INTEGER,  contentType  TEXT,  timeAdded  BIGINT,  timeStarted  BIGINT,  timeFinished  BIGINT)");
                        //END Create ScrapeQueueItem (PostgreSQL)

                        /*
                         * ScrapeRun
                         */
                        db.execSql("DROP TABLE IF EXISTS ScrapeRun");
                        //BEGIN Create ScrapeRun (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ScrapeRun  ( scrapeRunUid  SERIAL PRIMARY KEY  NOT NULL ,  scrapeType  TEXT,  status  INTEGER)");
                        //END Create ScrapeRun (PostgreSQL)

                        /*
                         * ContentEntryStatus
                         */
                        db.execSql("DROP TABLE IF EXISTS ContentEntryStatus");
                        //BEGIN Create ContentEntryStatus (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ContentEntryStatus  ( cesUid  BIGINT PRIMARY KEY  NOT NULL ,  totalSize  BIGINT,  bytesDownloadSoFar  BIGINT,  downloadStatus  INTEGER,  localAvailability  INTEGER,  downloadSpeed  INTEGER,  invalidated  BOOL,  cesLeaf  BOOL)");
                        //END Create ContentEntryStatus (PostgreSQL)

                        /*
                         * ConnectivityStatus
                         */
                        db.execSql("DROP TABLE IF EXISTS ConnectivityStatus");
                        //BEGIN Create ConnectivityStatus (PostgreSQL)
                        db.execSql("CREATE TABLE IF NOT EXISTS  ConnectivityStatus  ( csUid  INTEGER PRIMARY KEY  NOT NULL ,  connectivityState  INTEGER,  wifiSsid  TEXT,  connectedOrConnecting  BOOL)");
                        //END Create ConnectivityStatus (PostgreSQL)



                        /**
                         * Create triggers and functions for all
                         */
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_9_fn() RETURNS trigger AS $$ BEGIN UPDATE Person SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 9) ELSE NEW.personMasterChangeSeqNum END) WHERE personUid = NEW.personUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 9; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_9_trig AFTER UPDATE OR INSERT ON Person FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_9_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_6_fn() RETURNS trigger AS $$ BEGIN UPDATE Clazz SET clazzLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) END),clazzMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 6) ELSE NEW.clazzMasterChangeSeqNum END) WHERE clazzUid = NEW.clazzUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 6; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_6_trig AFTER UPDATE OR INSERT ON Clazz FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_6_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_31_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzMemberLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) END),clazzMemberMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 31) ELSE NEW.clazzMemberMasterChangeSeqNum END) WHERE clazzMemberUid = NEW.clazzMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 31; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_31_trig AFTER UPDATE OR INSERT ON ClazzMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_31_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_14_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzLog SET clazzLogChangeLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) END),clazzLogChangeMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 14) ELSE NEW.clazzLogChangeMasterChangeSeqNum END) WHERE clazzLogUid = NEW.clazzLogUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 14; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_14_trig AFTER UPDATE OR INSERT ON ClazzLog FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_14_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_15_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) END),clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 15) ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END) WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 15; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_15_trig AFTER UPDATE OR INSERT ON ClazzLogAttendanceRecord FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_15_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_16_fn() RETURNS trigger AS $$ BEGIN UPDATE FeedEntry SET feedEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.feedEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) END),feedEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 16) ELSE NEW.feedEntryMasterChangeSeqNum END) WHERE feedEntryUid = NEW.feedEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 16; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_16_trig AFTER UPDATE OR INSERT ON FeedEntry FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_16_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_20_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonField SET personFieldLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) END),personFieldMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 20) ELSE NEW.personFieldMasterChangeSeqNum END) WHERE personCustomFieldUid = NEW.personCustomFieldUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 20; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_20_trig AFTER UPDATE OR INSERT ON PersonField FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_20_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_18_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonCustomFieldValue SET personCustomFieldValueLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personCustomFieldValueLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) END),personCustomFieldValueMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 18) ELSE NEW.personCustomFieldValueMasterChangeSeqNum END) WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 18; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_18_trig AFTER UPDATE OR INSERT ON PersonCustomFieldValue FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_18_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_19_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonDetailPresenterField SET personDetailPresenterFieldLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personDetailPresenterFieldLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) END),personDetailPresenterFieldMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 19) ELSE NEW.personDetailPresenterFieldMasterChangeSeqNum END) WHERE personDetailPresenterFieldUid = NEW.personDetailPresenterFieldUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 19; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_19_trig AFTER UPDATE OR INSERT ON PersonDetailPresenterField FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_19_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_22_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestion SET selQuestionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) END),selQuestionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 22) ELSE NEW.selQuestionMasterChangeSeqNum END) WHERE selQuestionUid = NEW.selQuestionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 22; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_22_trig AFTER UPDATE OR INSERT ON SelQuestion FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_22_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_23_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionResponse SET selQuestionResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) END),selQuestionResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 23) ELSE NEW.selQuestionResponseMasterChangeSeqNum END) WHERE selQuestionResponseUid = NEW.selQuestionResponseUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 23; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_23_trig AFTER UPDATE OR INSERT ON SelQuestionResponse FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_23_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_24_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionResponseNomination SET selQuestionResponseNominationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionResponseNominationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) END),selQuestionResponseNominationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 24) ELSE NEW.selQuestionResponseNominationMasterChangeSeqNum END) WHERE selQuestionResponseNominationUid = NEW.selQuestionResponseNominationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 24; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_24_trig AFTER UPDATE OR INSERT ON SelQuestionResponseNomination FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_24_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_25_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionSet SET selQuestionSetLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) END),selQuestionSetMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 25) ELSE NEW.selQuestionSetMasterChangeSeqNum END) WHERE selQuestionSetUid = NEW.selQuestionSetUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 25; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_25_trig AFTER UPDATE OR INSERT ON SelQuestionSet FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_25_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_26_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionSetRecognition SET selQuestionSetRecognitionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetRecognitionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) END),selQuestionSetRecognitionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 26) ELSE NEW.selQuestionSetRecognitionMasterChangeSeqNum END) WHERE selQuestionSetRecognitionUid = NEW.selQuestionSetRecognitionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 26; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_26_trig AFTER UPDATE OR INSERT ON SelQuestionSetRecognition FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_26_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_27_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionSetResponse SET selQuestionSetResponseLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionSetResponseLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) END),selQuestionSetResponseMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 27) ELSE NEW.selQuestionSetResponseMasterChangeSeqNum END) WHERE selQuestionSetResposeUid = NEW.selQuestionSetResposeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 27; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_27_trig AFTER UPDATE OR INSERT ON SelQuestionSetResponse FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_27_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_21_fn() RETURNS trigger AS $$ BEGIN UPDATE Schedule SET scheduleLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.scheduleLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) END),scheduleMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 21) ELSE NEW.scheduleMasterChangeSeqNum END) WHERE scheduleUid = NEW.scheduleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 21; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_21_trig AFTER UPDATE OR INSERT ON Schedule FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_21_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_17_fn() RETURNS trigger AS $$ BEGIN UPDATE Holiday SET holidayLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.holidayLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) END),holidayMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 17) ELSE NEW.holidayMasterChangeSeqNum END) WHERE holidayUid = NEW.holidayUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 17; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_17_trig AFTER UPDATE OR INSERT ON Holiday FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_17_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_28_fn() RETURNS trigger AS $$ BEGIN UPDATE UMCalendar SET personLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) END),personMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 28) ELSE NEW.personMasterChangeSeqNum END) WHERE umCalendarUid = NEW.umCalendarUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 28; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_28_trig AFTER UPDATE OR INSERT ON UMCalendar FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_28_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_11_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzActivity SET clazzActivityLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) END),clazzActivityMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 11) ELSE NEW.clazzActivityMasterChangeSeqNum END) WHERE clazzActivityUid = NEW.clazzActivityUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 11; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_11_trig AFTER UPDATE OR INSERT ON ClazzActivity FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_11_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_32_fn() RETURNS trigger AS $$ BEGIN UPDATE ClazzActivityChange SET clazzActivityChangeLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.clazzActivityChangeLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) END),clazzActivityChangeMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 32) ELSE NEW.clazzActivityChangeMasterChangeSeqNum END) WHERE clazzActivityChangeUid = NEW.clazzActivityChangeUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 32; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_32_trig AFTER UPDATE OR INSERT ON ClazzActivityChange FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_32_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_42_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntry SET contentEntryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) END),contentEntryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 42) ELSE NEW.contentEntryMasterChangeSeqNum END) WHERE contentEntryUid = NEW.contentEntryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 42; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_42_trig AFTER UPDATE OR INSERT ON ContentEntry FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_42_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_3_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.ceccjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) END),ceccjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 3) ELSE NEW.ceccjMasterChangeSeqNum END) WHERE ceccjUid = NEW.ceccjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 3; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_3_trig AFTER UPDATE OR INSERT ON ContentEntryContentCategoryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_3_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_4_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryContentEntryFileJoin SET cecefjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cecefjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) END),cecefjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 4) ELSE NEW.cecefjMasterChangeSeqNum END) WHERE cecefjUid = NEW.cecefjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 4; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_4_trig AFTER UPDATE OR INSERT ON ContentEntryContentEntryFileJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_4_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_5_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryFile SET contentEntryFileLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentEntryFileLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) END),contentEntryFileMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 5) ELSE NEW.contentEntryFileMasterChangeSeqNum END) WHERE contentEntryFileUid = NEW.contentEntryFileUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 5; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_5_trig AFTER UPDATE OR INSERT ON ContentEntryFile FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_5_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_7_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cepcjLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) END),cepcjMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 7) ELSE NEW.cepcjMasterChangeSeqNum END) WHERE cepcjUid = NEW.cepcjUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 7; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_7_trig AFTER UPDATE OR INSERT ON ContentEntryParentChildJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_7_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_8_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.cerejLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) END),cerejMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 8) ELSE NEW.cerejMasterChangeSeqNum END) WHERE cerejUid = NEW.cerejUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 8; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_8_trig AFTER UPDATE OR INSERT ON ContentEntryRelatedEntryJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_8_fn()");
                        //Location trigger and function
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_29_fn() RETURNS trigger AS $$ BEGIN UPDATE Location SET locationLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.locationLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) END),locationMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 29) ELSE NEW.locationMasterChangeSeqNum END) WHERE locationUid = NEW.locationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 29; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_29_trig AFTER UPDATE OR INSERT ON Location FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_29_fn()");

                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_2_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategorySchemaLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) END),contentCategorySchemaMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 2) ELSE NEW.contentCategorySchemaMasterChangeSeqNum END) WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 2; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_2_trig AFTER UPDATE OR INSERT ON ContentCategorySchema FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_2_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_1_fn() RETURNS trigger AS $$ BEGIN UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.contentCategoryLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) END),contentCategoryMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 1) ELSE NEW.contentCategoryMasterChangeSeqNum END) WHERE contentCategoryUid = NEW.contentCategoryUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 1; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_1_trig AFTER UPDATE OR INSERT ON ContentCategory FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_1_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_13_fn() RETURNS trigger AS $$ BEGIN UPDATE Language SET langLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) END),langMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 13) ELSE NEW.langMasterChangeSeqNum END) WHERE langUid = NEW.langUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 13; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_13_trig AFTER UPDATE OR INSERT ON Language FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_13_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_10_fn() RETURNS trigger AS $$ BEGIN UPDATE LanguageVariant SET langVariantLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.langVariantLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) END),langVariantMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 10) ELSE NEW.langVariantMasterChangeSeqNum END) WHERE langVariantUid = NEW.langVariantUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 10; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_10_trig AFTER UPDATE OR INSERT ON LanguageVariant FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_10_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_30_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonAuth SET personAuthLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personAuthLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) END),personAuthMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 30) ELSE NEW.personAuthMasterChangeSeqNum END) WHERE personAuthUid = NEW.personAuthUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 30; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_30_trig AFTER UPDATE OR INSERT ON PersonAuth FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_30_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_45_fn() RETURNS trigger AS $$ BEGIN UPDATE Role SET roleLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.roleLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) END),roleMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 45) ELSE NEW.roleMasterCsn END) WHERE roleUid = NEW.roleUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 45; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_45_trig AFTER UPDATE OR INSERT ON Role FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_45_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_47_fn() RETURNS trigger AS $$ BEGIN UPDATE EntityRole SET erLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.erLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) END),erMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 47) ELSE NEW.erMasterCsn END) WHERE erUid = NEW.erUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 47; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_47_trig AFTER UPDATE OR INSERT ON EntityRole FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_47_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_43_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroup SET groupLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) END),groupMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 43) ELSE NEW.groupMasterCsn END) WHERE groupUid = NEW.groupUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 43; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_43_trig AFTER UPDATE OR INSERT ON PersonGroup FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_43_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_44_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonGroupMember SET groupMemberLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.groupMemberLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) END),groupMemberMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 44) ELSE NEW.groupMemberMasterCsn END) WHERE groupMemberUid = NEW.groupMemberUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 44; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_44_trig AFTER UPDATE OR INSERT ON PersonGroupMember FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_44_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_52_fn() RETURNS trigger AS $$ BEGIN UPDATE SelQuestionOption SET selQuestionOptionLocalChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.selQuestionOptionLocalChangeSeqNum ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) END),selQuestionOptionMasterChangeSeqNum = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 52) ELSE NEW.selQuestionOptionMasterChangeSeqNum END) WHERE selQuestionOptionUid = NEW.selQuestionOptionUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 52; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_52_trig AFTER UPDATE OR INSERT ON SelQuestionOption FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_52_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_48_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonLocationJoin SET plLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.plLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) END),plMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 48) ELSE NEW.plMasterCsn END) WHERE personLocationUid = NEW.personLocationUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 48; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_48_trig AFTER UPDATE OR INSERT ON PersonLocationJoin FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_48_fn()");
                        db.execSql("CREATE OR REPLACE FUNCTION inc_csn_50_fn() RETURNS trigger AS $$ BEGIN UPDATE PersonPicture SET personPictureLocalCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN NEW.personPictureLocalCsn ELSE (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) END),personPictureMasterCsn = (SELECT CASE WHEN (SELECT master FROM SyncDeviceBits) THEN (SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = 50) ELSE NEW.personPictureMasterCsn END) WHERE personPictureUid = NEW.personPictureUid; UPDATE SyncStatus SET nextChangeSeqNum = nextChangeSeqNum + 1  WHERE tableId = 50; RETURN null; END $$LANGUAGE plpgsql");
                        db.execSql("CREATE TRIGGER inc_csn_50_trig AFTER UPDATE OR INSERT ON PersonPicture FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE inc_csn_50_fn()");


                        /**
                         * Create Sequences and assign them to Primary keys for all not dealt with (updates and new ones above)
                         * Also create SyncStatus if they do not exist.
                         */
                        db.execSql("CREATE SEQUENCE spk_seq_9 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Person ALTER COLUMN personUid SET DEFAULT NEXTVAL('spk_seq_9')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "9" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "9"
                                +")");


                        db.execSql("CREATE SEQUENCE spk_seq_6 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Clazz ALTER COLUMN clazzUid SET DEFAULT NEXTVAL('spk_seq_6')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "6" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "6"
                                +")");


                        db.execSql("CREATE SEQUENCE spk_seq_31 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ClazzMember ALTER COLUMN clazzMemberUid SET DEFAULT NEXTVAL('spk_seq_31')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "31" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "31"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_14 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "14" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "14"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_15 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ClazzLogAttendanceRecord ALTER COLUMN clazzLogAttendanceRecordUid SET DEFAULT NEXTVAL('spk_seq_15')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "15" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "15"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_16 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "16" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "16"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_20 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonField ALTER COLUMN personCustomFieldUid SET DEFAULT NEXTVAL('spk_seq_20')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "20" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "20"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_18 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonCustomFieldValue ALTER COLUMN personCustomFieldValueUid SET DEFAULT NEXTVAL('spk_seq_18')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "18" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "18"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_19 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonDetailPresenterField ALTER COLUMN personDetailPresenterFieldUid SET DEFAULT NEXTVAL('spk_seq_19')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "19" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "19"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_22 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "22" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "22"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_23 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "23" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "23"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_24 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "24" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "24"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_25 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "25" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "25"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_26 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "26" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "26"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_27 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "27" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "27"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_21 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Schedule ALTER COLUMN scheduleUid SET DEFAULT NEXTVAL('spk_seq_21')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "21" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "21"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_17 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Holiday ALTER COLUMN holidayUid SET DEFAULT NEXTVAL('spk_seq_17')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "17" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "17"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_28 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE UMCalendar ALTER COLUMN umCalendarUid SET DEFAULT NEXTVAL('spk_seq_28')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "28" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "28"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_11 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ClazzActivity ALTER COLUMN clazzActivityUid SET DEFAULT NEXTVAL('spk_seq_11')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "11" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "11"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_32 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ClazzActivityChange ALTER COLUMN clazzActivityChangeUid SET DEFAULT NEXTVAL('spk_seq_32')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "32" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "32"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_42 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "42" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "42"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_3 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "3" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "3"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_4 " +  DoorUtilsadmin .generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "4" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "4"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_5 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "5" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "5"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_7 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "7" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "7"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_8 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "8" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "8"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_29 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "29" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "29"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_2 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ContentCategorySchema ALTER COLUMN contentCategorySchemaUid SET DEFAULT NEXTVAL('spk_seq_2')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "2" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "2"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_1 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE ContentCategory ALTER COLUMN contentCategoryUid SET DEFAULT NEXTVAL('spk_seq_1')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "1" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "1"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_13 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Language ALTER COLUMN langUid SET DEFAULT NEXTVAL('spk_seq_13')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "13" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "13"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_10 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE LanguageVariant ALTER COLUMN langVariantUid SET DEFAULT NEXTVAL('spk_seq_10')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "10" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "10"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_30 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonAuth ALTER COLUMN personAuthUid SET DEFAULT NEXTVAL('spk_seq_30')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "30" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "30"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_45 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE Role ALTER COLUMN roleUid SET DEFAULT NEXTVAL('spk_seq_45')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "45" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "45"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_47 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE EntityRole ALTER COLUMN erUid SET DEFAULT NEXTVAL('spk_seq_47')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "47" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "47"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_43 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonGroup ALTER COLUMN groupUid SET DEFAULT NEXTVAL('spk_seq_43')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "43" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "43"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_44 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonGroupMember ALTER COLUMN groupMemberUid SET DEFAULT NEXTVAL('spk_seq_44')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "44" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "44"
                                +")");

                        //db.execSql("CREATE SEQUENCE spk_seq_52 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "52" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "52"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_48 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonLocationJoin ALTER COLUMN personLocationUid SET DEFAULT NEXTVAL('spk_seq_48')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "48" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "48"
                                +")");

                        db.execSql("CREATE SEQUENCE spk_seq_50 " +  DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(deviceBits));
                        //Update def of PK to link with Sequence
                        db.execSql("ALTER TABLE PersonPicture ALTER COLUMN personPictureUid SET DEFAULT NEXTVAL('spk_seq_50')");
                        db.execSql("INSERT INTO SyncStatus(tableId, nextChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                                "SELECT " +
                                "50" +
                                ", 1, 0, 0 WHERE NOT EXISTS ( SELECT tableId " +
                                " FROM SyncStatus WHERE tableId = " +
                                "50"
                                +")");



                        break;


                }
            }
        });

        /**
         * Sprint 5(v3) - Sproint 6(v5) Migration notes:
         *
         */
        builder.addMigration(new UmDbMigration(3, 5) {

            @Override
            public void migrate(DoorDbAdapter db) {

                System.out.println("STARTING Sprint 5 to Sprint 6 MIGRATIONS >>>");
                switch (db.getDbType()){
                    case UmDbType.TYPE_SQLITE:
                        //TODO

                        break;
                    case UmDbType.TYPE_POSTGRES :
                        //TODO:

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

    public abstract DateRangeDao getDateRangeDao();

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


    public abstract AuditLogDao getAuditLogDao();

    public abstract CustomFieldDao getCustomFieldDao();

    public abstract CustomFieldValueDao getCustomFieldValueDao();

    public abstract CustomFieldValueOptionDao getCustomFieldValueOptionDao();

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
