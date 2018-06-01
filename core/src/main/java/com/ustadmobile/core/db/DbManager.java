package com.ustadmobile.core.db;

import com.ustadmobile.core.db.dao.*;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.db.impl.DbManagerFactory;


/**
 * Created by mike on 1/13/18.
 */

public abstract class DbManager {

    private static DbManager instance;


    public DbManager() {
    }

    public static DbManager getInstance(Object context){
        if(instance == null)
            instance = DbManagerFactory.makeDbManager(context);

        return instance;
    }

    public abstract OpdsAtomFeedRepository getOpdsAtomFeedRepository();

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

    public abstract Object getContext();

}
