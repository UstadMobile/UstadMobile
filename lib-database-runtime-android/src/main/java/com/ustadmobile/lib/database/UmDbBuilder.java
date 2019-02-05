package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;

import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DbCallback;

import java.lang.reflect.Constructor;

public class UmDbBuilder {

    public static class RoomDbBuilder<T> extends AbstractDoorwayDbBuilder<T> {

        private Object context;

        private String dbName;

        public RoomDbBuilder(Class<T> dbClass, Object context, String dbName) {
            super(dbClass);
            this.context = context;
            this.dbName = dbName;
        }

        @Override
        public T build() {
            try {
                Class roomDbManagerClass = Class.forName(dbClass.getName() + "_RoomDbManager");

                @SuppressWarnings("unchecked")
                Constructor constructor = roomDbManagerClass.getConstructor(Object.class, String.class);

                @SuppressWarnings("unchecked")
                T dbResult = (T)constructor.newInstance(context, dbName);

                return dbResult;
            }catch(Exception e) {
                System.err.print("Exception creating database: " + e);
                throw new RuntimeException(e);
            }
        }

        public <D extends RoomDatabase> RoomDatabase.Builder<D> applyToRoomBuilder(
                RoomDatabase.Builder<D> roomBuilder) {
            for(DbCallback dbCallback : callbackList) {
                roomBuilder.addCallback(new UmDbCallbackAdapter(dbCallback));
            }

            return roomBuilder;
        }

    }

    private static class UmDbCallbackAdapter extends RoomDatabase.Callback{

        private DbCallback callback;

        private UmDbCallbackAdapter(DbCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            callback.onCreate(new DoorDbAdapterSupportSqlite(db));
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            callback.onCreate(new DoorDbAdapterSupportSqlite(db));
        }
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context) {
        return new RoomDbBuilder<>(dbClass, context, dbClass.getSimpleName());
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context, String dbName) {
        return new RoomDbBuilder<>(dbClass, context, dbName);
    }
}