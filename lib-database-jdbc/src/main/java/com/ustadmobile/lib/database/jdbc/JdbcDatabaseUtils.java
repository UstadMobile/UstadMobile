package com.ustadmobile.lib.database.jdbc;

import java.sql.Connection;
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
        ResultSet tableResult = null;
        List<String> tableNames = new ArrayList<>();
        try {
            tableResult = connection.getMetaData().getTables(null, null, "%", null);

            while(tableResult.next()){
                tableNames.add(tableResult.getString("TABLE_NAME"));
            }
        }catch(SQLException e) {
            throw e;
        }finally {
            if(tableResult != null)
                tableResult.close();
        }

        return tableNames;
    }

}
