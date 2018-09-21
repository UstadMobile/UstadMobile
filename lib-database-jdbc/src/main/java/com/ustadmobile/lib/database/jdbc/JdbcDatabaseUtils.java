package com.ustadmobile.lib.database.jdbc;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class JdbcDatabaseUtils {

    public static class DbChangeListenerRequest {

        private List<String> tableNames;

        private DbChangeListener listener;

        public DbChangeListenerRequest(List<String> tableNames, DbChangeListener listener) {
            this.listener = listener;
            this.tableNames = tableNames;
        }

        public List<String> getTableNames() {
            return tableNames;
        }

        public void setTableNames(List<String> tableNames) {
            this.tableNames = tableNames;
        }

        public DbChangeListener getListener() {
            return listener;
        }

        public void setListener(DbChangeListener listener) {
            this.listener = listener;
        }
    }

    /**
     * List the tables available from a given JDBC connection
     *
     * @param connection JDBC connection to list tables from
     * @return A string list of table names
     * @throws SQLException If an SQLException occurs
     */
    public static List<String> getTableNames(Connection connection) throws SQLException{
        List<String> tableNames = new ArrayList<>();

        try(
            ResultSet tableResult = connection.getMetaData().getTables(null, null, "%", null);
        ) {
            while(tableResult.next()){
                tableNames.add(tableResult.getString("TABLE_NAME"));
            }
        }catch(SQLException e) {
            throw e;
        }

        return tableNames;
    }

    public static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param stmt
     */
    public static void closeStatement(PreparedStatement stmt) {
        if(stmt != null) {
            try {
                stmt.close();
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addDbChangeListener(DbChangeListenerRequest listenerRequest,
                                           Map<DbChangeListener, DbChangeListenerRequest> listeners) {
        listeners.put(listenerRequest.getListener(), listenerRequest);
    }

    public static void removeDbChangeListener(DbChangeListenerRequest listenerRequest,
                                      Map<DbChangeListener, DbChangeListenerRequest> listeners) {
        listeners.remove(listenerRequest);
    }

    public static void handleTablesChanged(Map<DbChangeListener, DbChangeListenerRequest> listeners,
                                           String... tablesChanged) {
        List<String> tablesChangedList = Arrays.asList(tablesChanged);

        for(DbChangeListenerRequest request : listeners.values()) {
            if(!Collections.disjoint(tablesChangedList, request.getTableNames())){
                request.getListener().onTablesChanged(tablesChangedList);
            }
        }
    }



}
