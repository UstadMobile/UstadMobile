package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.CrawJoblItemDao;
import com.ustadmobile.core.db.dao.CrawlJobDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.HolidayDao;
import com.ustadmobile.core.db.dao.HttpCachedEntryDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheAncestorDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseNominationDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetResponseDao;
import com.ustadmobile.core.db.dao.UMCalendarDao;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Holiday;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;

import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponse;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponseNomination;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetRecognition;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;
import com.ustadmobile.lib.db.entities.UMCalendar;

import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;


@UmDatabase(version = 1, entities = {
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
        SocialNominationQuestion.class, SocialNominationQuestionResponse.class,
        SocialNominationQuestionResponseNomination.class, SocialNominationQuestionSet.class,
        SocialNominationQuestionSetRecognition.class, SocialNominationQuestionSetResponse.class,
        Schedule.class, Holiday.class, UMCalendar.class,
        ClazzActivity.class, ClazzActivityChange.class,
        ContentEntry.class, ContentEntryContentCategoryJoin.class,
        ContentEntryContentEntryFileJoin.class, ContentEntryFile.class,
        ContentEntryParentChildJoin.class, ContentEntryRelatedEntryJoin.class,
        SyncStatus.class, SyncablePrimaryKey.class, SyncDeviceBits.class, Location.class

})
public abstract class UmAppDatabase implements UmSyncableDatabase{

    private static volatile UmAppDatabase instance;

    private boolean master;

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

    public static synchronized UmAppDatabase getInstance(Object context) {
        if(instance == null){
            instance = com.ustadmobile.core.db.UmAppDatabase_Factory.makeUmAppDatabase(context);
        }

        return instance;
    }

    public static synchronized UmAppDatabase getInstance(Object context, String dbName) {
        return UmAppDatabase_Factory.makeUmAppDatabase(context, dbName);
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

    public abstract PersonCustomFieldDao getPersonCustomFieldDao();

    public abstract PersonCustomFieldValueDao getPersonCustomFieldValueDao();

    public abstract PersonDetailPresenterFieldDao getPersonDetailPresenterFieldDao();

    public abstract SocialNominationQuestionDao getSocialNominationQuestionDao();

    public abstract SocialNominationQuestionSetResponseDao getSocialNominationQuestionSetResponseDao();

    public abstract SocialNominationQuestionSetDao getSocialNominationQuestionSetDao();

    public abstract SocialNominationQuestionResponseNominationDao getSocialNominationQuestionResponseNominationDao();

    public abstract SocialNominationQuestionResponseDao getSocialNominationQuestionResponseDao();

    public abstract ScheduleDao getScheduleDao();

    public abstract UMCalendarDao getUMCalendarDao();

    public abstract HolidayDao getHolidayDao();

    public abstract ClazzActivityDao getClazzActivityDao();

    public abstract ClazzActivityChangeDao getClazzActivityChangeDao();

    public abstract ContentEntryDao getContentEntryDao();

    public abstract ContentEntryContentCategoryJoinDao getContentEntryContentCategoryJoinDao();

    public abstract ContentEntryContentEntryFileJoinDao getContentEntryContentEntryFileJoinDao();

    public abstract ContentEntryFileDao getContentEntryFileDao();

    public abstract ContentEntryParentChildJoinDao getContentEntryParentChildJoinDao();

    public abstract ContentEntryRelatedEntryJoinDao getContentEntryRelatedEntryJoinDao();

    public abstract SyncStatusDao getSyncStatusDao();

    public abstract LocationDao getLocationDao();

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
    public abstract void syncWith(UmAppDatabase otherDb, long accountUid);

}
