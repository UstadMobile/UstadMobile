package com.ustadmobile.port.javase.fs.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.ustadmobile.port.javase.impl.UmContextSe;
import com.ustadmobile.port.jcommon.HttpCacheDbManagerOrmLite;
import com.ustadmobile.port.jcommon.fs.db.HttpCacheDbEntryEntity;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

/**
 * Created by mike on 12/31/17.
 */

public class HttpCacheDbManagerJdbc extends HttpCacheDbManagerOrmLite {

    private WeakReference<Dao<HttpCacheDbEntryEntity, Integer>> daoRef;


    @Override
    public Dao<HttpCacheDbEntryEntity, Integer> getDao(Object context) {
        Dao<HttpCacheDbEntryEntity, Integer> dao = daoRef != null ? daoRef.get() : null;
        if(dao == null) {
            try {
                UmContextSe contextSe = (UmContextSe)context;
                dao = DaoManager.createDao(contextSe.getCacheDbConnectionSource(), HttpCacheDbEntryEntity.class);
                daoRef = new WeakReference<>(dao);
            }catch(SQLException e) {

            }

        }

        return dao;
    }
}
