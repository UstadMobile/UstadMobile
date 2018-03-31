package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.lib.db.entities.*;
import com.ustadmobile.port.android.db.dao.ContainerFileDaoAndroid;
import com.ustadmobile.port.android.db.dao.ContainerFileEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.CrawJoblItemDaoAndroid;
import com.ustadmobile.port.android.db.dao.CrawlJobDaoAndroid;
import com.ustadmobile.port.android.db.dao.DownloadJobDaoAndroid;
import com.ustadmobile.port.android.db.dao.DownloadJobItemDaoAndroid;
import com.ustadmobile.port.android.db.dao.DownloadJobItemHistoryDaoAndroid;
import com.ustadmobile.port.android.db.dao.EntryStatusResponseDaoAndroid;
import com.ustadmobile.port.android.db.dao.NetworkNodeDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryParentToChildJoinDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryStatusCacheAncestorDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryStatusCacheDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryWithRelationsDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsLinkDaoAndroid;


/**
 * Created by mike on 1/14/18.
 */
@Database(version = 1, entities =  {
        OpdsEntry.class, OpdsLink.class, OpdsEntryParentToChildJoin.class,
        ContainerFile.class, ContainerFileEntry.class, DownloadJob.class,
        DownloadJobItem.class, NetworkNode.class, EntryStatusResponse.class,
        DownloadJobItemHistory.class, CrawlJob.class, CrawlJobItem.class,
        OpdsEntryStatusCache.class, OpdsEntryStatusCacheAncestor.class
})
public abstract class AppDatabase extends RoomDatabase {

    public abstract OpdsEntryDaoAndroid getOpdsEntryDao();

    public abstract OpdsEntryWithRelationsDaoAndroid getOpdsEntryWithRelationsDao();

    public abstract OpdsLinkDaoAndroid getOpdsLinkDao();

    public abstract OpdsEntryParentToChildJoinDaoAndroid getOpdsEntryParentToChildJoinDao();

    public abstract ContainerFileEntryDaoAndroid getContainerFileEntryDao();

    public abstract ContainerFileDaoAndroid getContainerFileDao();

    public abstract NetworkNodeDaoAndroid getNetworkNodeDao();

    public abstract EntryStatusResponseDaoAndroid getEntryStatusResponseDao();

    public abstract DownloadJobDaoAndroid getDownloadJobDao();

    public abstract DownloadJobItemDaoAndroid getDownloadJobItemDao();

    public abstract DownloadJobItemHistoryDaoAndroid getDownloadJobItemHistoryDao();

    public abstract CrawlJobDaoAndroid getCrawlJobDao();

    public abstract CrawJoblItemDaoAndroid getDownloadJobCrawlItemDao();

    public abstract OpdsEntryStatusCacheDaoAndroid getOpdsEntryStatusCacheDao();

    public abstract OpdsEntryStatusCacheAncestorDaoAndroid getOpdsEntryStatusCacheAncestorDao();

}
