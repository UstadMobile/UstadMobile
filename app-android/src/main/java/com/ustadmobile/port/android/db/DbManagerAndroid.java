package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.core.db.dao.CrawJoblItemDao;
import com.ustadmobile.core.db.dao.CrawlJobDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.HttpCachedEntryDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheAncestorDao;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.fs.db.repository.OpdsAtomFeedRepositoryImpl;
import com.ustadmobile.port.android.db.dao.ContainerFileDaoAndroid;
import com.ustadmobile.port.android.db.dao.CrawlJobDaoAndroid;
import com.ustadmobile.port.android.db.dao.DownloadJobDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryParentToChildJoinDaoAndroid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 1/14/18.
 */

public class DbManagerAndroid extends DbManager {

    private Context context;

    private AppDatabase appDatabase;

    private ExecutorService executorService;

    private OpdsAtomFeedRepositoryImpl opdsAtomFeedRepository;

    private ContainerFileDaoAndroid containerFileDao;

    private OpdsEntryParentToChildJoinDaoAndroid opdsEntryParentToChildJoinDao;

    private CrawlJobDaoAndroid crawlJobDao;

    private DownloadJobDaoAndroid downloadJobDao;

    public DbManagerAndroid(Object context) {
        this.context = ((Context)context).getApplicationContext();
        appDatabase = Room.databaseBuilder(this.context, AppDatabase.class, "appdb65")
            .build();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public Object getContext() {
        return context;
    }


    @Override
    public OpdsAtomFeedRepository getOpdsAtomFeedRepository() {
        if(opdsAtomFeedRepository == null)
            opdsAtomFeedRepository = new OpdsAtomFeedRepositoryImpl(this, executorService);

        return opdsAtomFeedRepository;
    }

    @Override
    public OpdsEntryDao getOpdsEntryDao() {
        return appDatabase.getOpdsEntryDao();
    }

    @Override
    public OpdsEntryWithRelationsDao getOpdsEntryWithRelationsDao() {
        return appDatabase.getOpdsEntryWithRelationsDao();
    }

    @Override
    public OpdsLinkDao getOpdsLinkDao() {
        return appDatabase.getOpdsLinkDao();
    }

    @Override
    public OpdsEntryParentToChildJoinDao getOpdsEntryParentToChildJoinDao() {
        if(opdsEntryParentToChildJoinDao == null){
            opdsEntryParentToChildJoinDao = appDatabase.getOpdsEntryParentToChildJoinDao();
            opdsEntryParentToChildJoinDao.setExecutorService(executorService);
        }

        return opdsEntryParentToChildJoinDao;
    }

    @Override
    public ContainerFileDao getContainerFileDao() {
        if(containerFileDao == null) {
            containerFileDao = appDatabase.getContainerFileDao();
            containerFileDao.setExecutorService(executorService);
        }

        return containerFileDao;
    }

    @Override
    public ContainerFileEntryDao getContainerFileEntryDao() {
        return appDatabase.getContainerFileEntryDao();
    }

    @Override
    public NetworkNodeDao getNetworkNodeDao() {
        return appDatabase.getNetworkNodeDao();
    }

    @Override
    public EntryStatusResponseDao getEntryStatusResponseDao() {
        return appDatabase.getEntryStatusResponseDao();
    }

    @Override
    public DownloadSetDao getDownloadSetDao() {
        return appDatabase.getDownloadSetDao();
    }

    @Override
    public DownloadJobItemHistoryDao getDownloadJobItemHistoryDao() {
        return appDatabase.getDownloadJobItemHistoryDao();
    }

    @Override
    public DownloadSetItemDao getDownloadSetItemDao() {
        return appDatabase.getDownloadSetItemDao();
    }

    @Override
    public CrawJoblItemDao getDownloadJobCrawlItemDao() {
        return appDatabase.getDownloadJobCrawlItemDao();
    }

    @Override
    public CrawlJobDao getCrawlJobDao() {
        if(crawlJobDao == null){
            crawlJobDao = appDatabase.getCrawlJobDao();
            crawlJobDao.setExecutorService(executorService);
        }
        return appDatabase.getCrawlJobDao();
    }

    @Override
    public OpdsEntryStatusCacheDao getOpdsEntryStatusCacheDao() {
        return appDatabase.getOpdsEntryStatusCacheDao();
    }

    @Override
    public OpdsEntryStatusCacheAncestorDao getOpdsEntryStatusCacheAncestorDao() {
        return appDatabase.getOpdsEntryStatusCacheAncestorDao();
    }

    @Override
    public HttpCachedEntryDao getHttpCachedEntryDao() {
        return appDatabase.getHttpCachedEnrtyDao();
    }

    @Override
    public DownloadJobDao getDownloadJobDao() {
        if(downloadJobDao == null) {
            downloadJobDao = appDatabase.getDownloadJobDao();
            downloadJobDao.setExecutorService(executorService);
        }
        return downloadJobDao;
    }

    @Override
    public DownloadJobItemDao getDownloadJobItemDao() {
        return appDatabase.getDownloadJobItemDao();
    }
}
