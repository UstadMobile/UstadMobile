package com.ustadmobile.port.android.fs.cachedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.jcommon.fs.db.HttpCacheDbEntryEntity;

import java.sql.SQLException;

/**
 * Created by mike on 12/30/17.
 */

public class HttpCacheDbHelper extends OrmLiteSqliteOpenHelper {

    public static final String LOGTAG = "UstadMobile/HttpCacheDbHelper";

    public static final String DATABASE_NAME = "httpcache.db";

    public HttpCacheDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final int DATABASE_VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, HttpCacheDbEntryEntity.class);
        }catch(SQLException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 8, null, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
