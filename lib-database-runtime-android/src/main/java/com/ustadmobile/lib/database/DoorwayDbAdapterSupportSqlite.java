package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;

import com.ustadmobile.lib.db.DoorwayDbAdapter;

public class DoorwayDbAdapterSupportSqlite implements DoorwayDbAdapter {

    private SupportSQLiteDatabase db;

    public DoorwayDbAdapterSupportSqlite(SupportSQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void execSql(String sql) {
        db.execSQL(sql);
    }
}
