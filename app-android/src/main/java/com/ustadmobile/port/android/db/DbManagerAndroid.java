package com.ustadmobile.port.android.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsFeedDao;
import com.ustadmobile.core.db.dao.OpdsFeedWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.core.fs.db.repository.OpdsFeedRepository;
import com.ustadmobile.port.android.db.dao.OpdsFeedDaoAndroid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 1/14/18.
 */

public class DbManagerAndroid extends DbManager {

    private Context context;

    private AppDatabase appDatabase;

    private ExecutorService executorService;

    private OpdsFeedDaoAndroid opdsFeedDaoAndroid;

    private OpdsFeedWithRelationsDao opdsFeedWithRelationsDao;

    public DbManagerAndroid(Object context) {
        this.context = ((Context)context).getApplicationContext();
        appDatabase = Room.databaseBuilder(this.context, AppDatabase.class, "appdb7")
            .build();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public OpdsFeedDao getOpdsFeedDao() {
        if(opdsFeedDaoAndroid == null)
            opdsFeedDaoAndroid = appDatabase.getOpdsFeedDao();

        return opdsFeedDaoAndroid;
    }

    @Override
    public OpdsFeedWithRelationsDao getOpdsFeedRepository() {
        return null;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public OpdsFeedWithRelationsDao getOpdsFeedWithRelationsDao() {
        if(opdsFeedWithRelationsDao == null)
            opdsFeedWithRelationsDao = appDatabase.getOpdsFeedWithRelationsDao();

        return opdsFeedWithRelationsDao;
    }

    @Override
    public OpdsFeedWithRelationsDao getOpdsFeedWithRelationsRepository() {
        return new OpdsFeedRepository(this, executorService);
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
}
