package com.ustadmobile.lib.database.jdbc;

import com.ustadmobile.lib.db.DoorwayDbAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DoorwayDbAdapterJdbc implements DoorwayDbAdapter, AutoCloseable {

    private UmJdbcDatabase db;

    private Connection connection;

    private Statement statement;

    public DoorwayDbAdapterJdbc(UmJdbcDatabase db) {
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

    public void close() {
        JdbcDatabaseUtils.closeQuietly(statement);
        JdbcDatabaseUtils.closeQuietly(connection);
    }
}
