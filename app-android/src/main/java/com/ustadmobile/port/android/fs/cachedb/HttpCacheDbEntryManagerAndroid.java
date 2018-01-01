package com.ustadmobile.port.android.fs.cachedb;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.port.jcommon.HttpCacheDbManagerOrmLite;
import com.ustadmobile.port.jcommon.fs.db.HttpCacheDbEntryEntity;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

/**
 * Created by mike on 12/30/17.
 */

public class HttpCacheDbEntryManagerAndroid extends HttpCacheDbManagerOrmLite {

    private WeakReference<HttpCacheDbHelper> dbHelperRef;

    @Override
    public Dao<HttpCacheDbEntryEntity, Integer> getDao(Object context) {
        Context ctx = ((Context)context).getApplicationContext();
        HttpCacheDbHelper dbHelper = dbHelperRef != null ? dbHelperRef.get() : null;
        if(dbHelper == null) {
            dbHelper = new HttpCacheDbHelper(ctx);
            dbHelperRef = new WeakReference<>(dbHelper);
        }

        try {
            return dbHelper.getDao(HttpCacheDbEntryEntity.class);
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
