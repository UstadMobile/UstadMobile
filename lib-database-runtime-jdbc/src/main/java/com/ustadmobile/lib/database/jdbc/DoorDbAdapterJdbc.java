package com.ustadmobile.lib.database.jdbc;

import com.ustadmobile.lib.db.DoorDbAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DoorDbAdapterJdbc implements DoorDbAdapter, AutoCloseable {

    private UmJdbcDatabase db;

    private Connection connection;

    private Statement statement;

    public DoorDbAdapterJdbc(UmJdbcDatabase db) {
        this.db = db;
        try {
            connection = db.getConnection();
            statement = connection.createStatement();
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execSql(String sql) {
        try {
            statement.execute(sql);
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getDbType() {
        return db.getDbType();
    }

    @Override
    public String selectSingleValue(String sql) {
        try (
            ResultSet result = statement.executeQuery(sql);
        ) {
            if(result.next())
                return result.getString(1);
        }catch(SQLException e) {
            throw new RuntimeException("Select single value query threw exception: ", e);
        }

        return null;
    }

    public void close() {
        JdbcDatabaseUtils.closeQuietly(statement);
        JdbcDatabaseUtils.closeQuietly(connection);
    }
}
