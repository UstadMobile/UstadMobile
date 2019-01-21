package com.ustadmobile.lib.database;

import com.ustadmobile.lib.database.jdbc.DoorwayDbAdapterJdbc;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;
import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DbCallback;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.List;

public class UmDbBuilder {

    public static final String DBINFO_TABLENAME = "_doorwayinfo";

    public static class JdbcDbBuilder<T> extends AbstractDoorwayDbBuilder<T> {

        private Object context;

        private String name;

        public JdbcDbBuilder(Class<T> dbClass, Object context, String name) {
            super(dbClass);
            this.context = context;
            this.name = name;
        }


        @Override
        public T build() {
            DoorwayDbAdapterJdbc dbAdapter = null;
            Connection dbConnection = null;
            try {
                Class jdbcClass = Class.forName(dbClass.getName() + "_Jdbc");

                @SuppressWarnings("unchecked")
                Constructor constructor = jdbcClass.getConstructor(Object.class, String.class);

                @SuppressWarnings("unchecked")
                T dbResult = (T)constructor.newInstance(context, name);


                UmJdbcDatabase db = (UmJdbcDatabase)dbResult;
                dbConnection = db.getConnection();
                dbAdapter = new DoorwayDbAdapterJdbc(db);

                List<String> existingTableNames = JdbcDatabaseUtils.getTableNames(dbConnection);
                if(JdbcDatabaseUtils.listContainsStringIgnoreCase(existingTableNames,
                        DBINFO_TABLENAME)) {
                    //TODO: run migrations
                }else {
                    //create the table
                    dbAdapter.execSql("CREATE TABLE " + DBINFO_TABLENAME +
                            " (dbVersion int primary key, dbHash varchar(255))");

                    for(DbCallback callback : callbackList){
                        callback.onCreate(dbAdapter);
                    }
                }

                for(DbCallback callback : callbackList){
                    callback.onOpen(dbAdapter);
                }

                return dbResult;
            }catch(Exception e) {
                System.err.print("Exception creating database: " + e);
                throw new RuntimeException(e);
            }finally {
                JdbcDatabaseUtils.closeQuietly(dbAdapter);
                JdbcDatabaseUtils.closeQuietly(dbConnection);
            }
        }
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context) {
        return new JdbcDbBuilder<>(dbClass, context, dbClass.getSimpleName());
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context, String dbName) {
        return new JdbcDbBuilder<>(dbClass, context, dbName);
    }
}
