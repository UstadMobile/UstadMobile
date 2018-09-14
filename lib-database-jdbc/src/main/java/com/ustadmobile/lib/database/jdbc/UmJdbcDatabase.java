package com.ustadmobile.lib.database.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public interface UmJdbcDatabase {

    Connection getConnection() throws SQLException;

    ExecutorService getExecutor();

}
