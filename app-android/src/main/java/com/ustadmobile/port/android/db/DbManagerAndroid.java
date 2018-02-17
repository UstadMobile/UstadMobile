package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.fs.db.repository.OpdsEntryRepository;
import com.ustadmobile.port.android.db.dao.ContainerFileDaoAndroid;
import com.ustadmobile.port.android.db.dao.OpdsEntryParentToChildJoinDaoAndriod;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 1/14/18.
 */

public class DbManagerAndroid extends DbManager {

    private Context context;

    private AppDatabase appDatabase;

    private ExecutorService executorService;

    private OpdsEntryRepository opdsEntryRepository;

    private ContainerFileDaoAndroid containerFileDao;

    private OpdsEntryParentToChildJoinDaoAndriod opdsEntryParentToChildJoinDao;

    public DbManagerAndroid(Object context) {
        this.context = ((Context)context).getApplicationContext();
        appDatabase = Room.databaseBuilder(this.context, AppDatabase.class, "appdb57")
            .build();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public Object getContext() {
        return context;
    }


    @Override
    public OpdsEntryWithRelationsDao getOpdsEntryWithRelationsRepository() {
        if(opdsEntryRepository == null)
            opdsEntryRepository = new OpdsEntryRepository(this, executorService);

        return opdsEntryRepository;
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
    public DownloadJobDao getDownloadJobDao() {
        return appDatabase.getDownloadJobDao();
    }

    @Override
    public DownloadJobItemHistoryDao getDownloadJobItemHistoryDao() {
        return appDatabase.getDownloadJobItemHistoryDao();
    }

    @Override
    public DownloadJobItemDao getDownloadJobItemDao() {
        return appDatabase.getDownloadJobItemDao();
    }
}
