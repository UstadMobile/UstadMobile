package com.ustadmobile.lib.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DbCallback;
import com.ustadmobile.lib.db.UmDbMigration;

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
                Class roomDbClass = Class.forName(dbClass.getName() + "_RoomDb");

                RoomDatabase.Builder roomBuilder = Room.databaseBuilder((Context)context, roomDbClass,
                        dbName);
                for(DbCallback dbCallback : callbackList) {
                    roomBuilder.addCallback(new UmDbCallbackAdapter(dbCallback));
                }

                for(UmDbMigration migration : migrationList) {
                    final UmDbMigration umMigration = migration;
                    roomBuilder.addMigrations(new Migration(migration.getFromVersion(),
                            migration.getToVersion()) {
                        @Override
                        public void migrate(@NonNull SupportSQLiteDatabase database) {
                            umMigration.migrate(new DoorDbAdapterSupportSqlite(database));
                        }
                    });
                }

                @SuppressWarnings("unchecked")
                Constructor constructor = roomDbManagerClass.getConstructor(Object.class, String.class,
                        RoomDatabase.Builder.class);

                @SuppressWarnings("unchecked")
                T dbResult = (T)constructor.newInstance(context, dbName, roomBuilder);

                return dbResult;
            }catch(Exception e) {
                System.err.print("Exception creating database: " + e);
                throw new RuntimeException(e);
            }
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