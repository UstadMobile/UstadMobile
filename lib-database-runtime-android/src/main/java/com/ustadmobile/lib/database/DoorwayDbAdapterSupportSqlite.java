package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;

import com.ustadmobile.lib.db.DoorwayDbAdapter;
import com.ustadmobile.lib.db.UmDbType;

public class DoorwayDbAdapterSupportSqlite implements DoorwayDbAdapter {

    private SupportSQLiteDatabase db;

    public DoorwayDbAdapterSupportSqlite(SupportSQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void execSql(String sql) {
        db.execSQL(sql);
    }

    @Override
    public int getDbType() {
        return UmDbType.TYPE_SQLITE;
    }
}
