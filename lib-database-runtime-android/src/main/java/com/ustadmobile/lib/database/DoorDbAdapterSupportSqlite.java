package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;

import com.ustadmobile.lib.db.DoorDbAdapter;
import com.ustadmobile.lib.db.UmDbType;

public class DoorDbAdapterSupportSqlite implements DoorDbAdapter {

    private SupportSQLiteDatabase db;

    public DoorDbAdapterSupportSqlite(SupportSQLiteDatabase db) {
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

    @Override
    public String selectSingleValue(String sql) {
        //TODO: implement this
        return null;
    }
}
