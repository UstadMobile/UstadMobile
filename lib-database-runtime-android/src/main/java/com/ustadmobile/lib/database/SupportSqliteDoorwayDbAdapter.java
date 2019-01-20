package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;

import com.ustadmobile.lib.db.DoorwayDbHelper;

public class SupportSqliteDoorwayDbAdapter implements DoorwayDbHelper {

    private SupportSQLiteDatabase db;

    public SupportSqliteDoorwayDbAdapter(SupportSQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void execSql(String sql) {
        db.execSQL(sql);
    }
}
