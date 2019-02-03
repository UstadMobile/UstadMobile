package com.ustadmobile.lib.database;

import com.ustadmobile.lib.database.jdbc.DoorDbAdapterJdbc;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;
import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;
import com.ustadmobile.lib.db.DbCallback;
import com.ustadmobile.lib.db.UmDbMigration;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            DoorDbAdapterJdbc dbAdapter = null;
            Connection dbConnection = null;
            try {
                Class jdbcClass = Class.forName(dbClass.getName() + "_Jdbc");

                @SuppressWarnings("unchecked")
                Constructor constructor = jdbcClass.getConstructor(Object.class, String.class);

                @SuppressWarnings("unchecked")
                T dbResult = (T)constructor.newInstance(context, name);


                UmJdbcDatabase db = (UmJdbcDatabase)dbResult;
                dbConnection = db.getConnection();
                dbAdapter = new DoorDbAdapterJdbc(db);

                List<String> existingTableNames = JdbcDatabaseUtils.getTableNames(dbConnection);
                if(JdbcDatabaseUtils.listContainsStringIgnoreCase(existingTableNames,
                        DBINFO_TABLENAME)) {
                    try (
                        Connection dbCon = db.getConnection();
                        Statement stmt = dbCon.createStatement();
                        ResultSet versionResult = stmt.executeQuery(
                                "SELECT dbVersion from _doorwayinfo");
                    ) {
                        if(!versionResult.next()) {
                            throw new RuntimeException("Almost impossible: there is a _doorwayinfo table but no dbVersion column");
                        }

                        int dbVersion = versionResult.getInt(1);
                        runMigrations(dbVersion, db.getVersion(), dbAdapter);
                    }catch(SQLException e) {
                        throw new RuntimeException("UmDb: exception running migration ", e);
                    }
                }else {
                    //create the table
                    dbAdapter.execSql("CREATE TABLE " + DBINFO_TABLENAME +
                            " (dbVersion int primary key, dbHash varchar(255))");
                    dbAdapter.execSql("INSERT INTO _doorwayinfo (dbVersion, dbHash) VALUES (" +
                            db.getVersion() +", '')");

                    db.createAllTables();

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

        protected void runMigrations(int currentVersion, int targetVersion,
                                     DoorDbAdapterJdbc dbAdapter) {
            while(currentVersion < targetVersion) {
                UmDbMigration migration = JdbcDatabaseUtils.pickNextMigration(currentVersion,
                        migrationList);

                if(migration == null){
                    throw new RuntimeException("No migration found to upgrade from: "
                            + currentVersion);
                }

                migration.migrate(dbAdapter);
                currentVersion = migration.getToVersion();
                dbAdapter.execSql("UPDATE _doorwayinfo SET dbVersion = " +
                        currentVersion);
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
