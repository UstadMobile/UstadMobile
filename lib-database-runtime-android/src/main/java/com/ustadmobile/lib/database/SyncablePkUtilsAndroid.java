package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteProgram;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.RoomDatabase;
import android.database.Cursor;


import java.util.ArrayList;
import java.util.List;

/**
 * This class helps us implement syncable primary key support on Android. It's methods support
 * the retrieval of the generated primary keys.
 */
public abstract class SyncablePkUtilsAndroid {

    /**
     * Implementation of SupportSQLiteQuery to query for the last generated syncable primary keys.
     */
    public static class GetLastSyncablePkQuery implements SupportSQLiteQuery {
        @Override
        public String getSql() {
            return "SELECT lastpk FROM _lastsyncablepk ORDER BY id ASC";
        }

        @Override
        public void bindTo(SupportSQLiteProgram statement) {

        }

        @Override
        public int getArgCount() {
            return 0;
        }
    }

    /**
     * Find the last generated syncable primary keys and return them as a list
     *
     * @param db RoomDatabase being used
     *
     * @param roomDbManager manager object which has prepare statements and queries
     *
     * @return List of Long objects representing the generated primary keys (empty if there were none)
     */
    public static List<Long> getGeneratedKeysAsList(RoomDatabase db,
                                              UmRoomDbManagerWithSyncablePk roomDbManager) {

        SupportSQLiteQuery query = roomDbManager.getLastPksQuery();
        Cursor result = db.query(query);
        List<Long> resultList = null;
        try {
            result = db.query(roomDbManager.getLastPksQuery());
            resultList = new ArrayList<>();
            final int colIndex = result.getColumnIndexOrThrow("lastpk");
            if(result.moveToFirst()) {
                do {
                    resultList.add(result.getLong(colIndex));
                }while(result.moveToNext());
            }

            roomDbManager.getDeleteLastPksStatement().execute();
        }finally {
            if(result != null)
                result.close();
        }

        return resultList;

    }

    /**
     * Delete rows from the _lastsyncablepk, and do not return their value. Use this when running
     * inserts with no return value. The trigger will insert generated primary keys into the table,
     * so failing to delete them would result in an incorrect return value the next time.
     *
     * @param roomDbManager database manager
     */
    public static void deleteLastGeneratedPks(UmRoomDbManagerWithSyncablePk roomDbManager) {
        roomDbManager.getDeleteLastPksStatement().execute();
    }

    /**
     * As per getGeneratedKeysAsList - then converted to an array of primitive longs
     *
     * @param db RoomDatabase being used
     * @param roomDbManager manager object which has prepare statements and queries
     * @return Array of long primitives representing the generated primary keys (zero length if there were none)
     */
    public static long[] getGeneratedKeysAsArray(RoomDatabase db,
                                                 UmRoomDbManagerWithSyncablePk roomDbManager) {
        List<Long> resultList = getGeneratedKeysAsList(db, roomDbManager);
        long[] resultArr = new long[resultList.size()];
        for(int i = 0; i < resultList.size(); i++) {
            resultArr[i] = resultList.get(i);
        }

        return resultArr;
    }

    /**
     * As per getGeneratedKeysAsList - then converted to an array of boxed longs
     *
     * @param db RoomDatabase being used
     * @param roomDbManager manager object which has prepare statements and queries
     * @return Array of boxed longs representing the generated primary keys (zero length if there were none)
     */
    public static Long[] getGeneratedKeysAsBoxedArray(RoomDatabase db,
                                                      UmRoomDbManagerWithSyncablePk roomDbManager) {
        List<Long> resultList = getGeneratedKeysAsList(db, roomDbManager);
        return resultList.toArray(new Long[0]);
    }

    /**
     * Used when only one value was inserted.
     *
     * @param db RoomDatabase being used
     * @param roomDbManager manager object which has prepare statements and queries
     *
     * @return the value of the last syncable primary key generated, or 0 if no such key was generated
     */
    public static long getGeneratedKey(RoomDatabase db, UmRoomDbManagerWithSyncablePk roomDbManager) {
        List<Long> resultList = getGeneratedKeysAsList(db, roomDbManager);
        return !resultList.isEmpty() ? resultList.get(0) : 0L;
    }

}
