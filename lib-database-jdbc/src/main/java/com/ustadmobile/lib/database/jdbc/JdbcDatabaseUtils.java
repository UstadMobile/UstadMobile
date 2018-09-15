package com.ustadmobile.lib.database.jdbc;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcDatabaseUtils {

    public static Connection checkConnection(DataSource dataSource, Connection connection) {
        try {
            if(!connection.isClosed())
                return connection;

            return dataSource.getConnection();
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
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

}
