package com.ustadmobile.lib.database.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface UmJdbcDatabase {

    Connection getConnection() throws SQLException;

    ExecutorService getExecutor();

//    void addDbChangeListener(JdbcDatabaseUtils.DbChangeListenerRequest listenerRequest);
//
//    void removeDbChangeListener(JdbcDatabaseUtils.DbChangeListenerRequest listenerRequest);

    /**
     * This method is to be called by DAOs when the table has b
     * @param changedTables
     */
    void handleTablesChanged(String... changedTables);

}
