package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteStatement;

public interface UmRoomDbManagerWithSyncablePk {

    /**
     * Return a query that will get a list of the last syncable primary keys generated from
     * the _lastsyncablepk table
     *
     * @return SupportSQLiteQuery that can be used query the database for the most recently
     * created syncable primary keys
     */
    SupportSQLiteQuery getLastPksQuery();

    /**
     * Return a statement that will delete all entries from the _lastsyncablepk table
     *
     * @return SupportSQLiteStatement that will delete all rows from the _lastsyncablepk table
     */
    SupportSQLiteStatement getDeleteLastPksStatement();

}
