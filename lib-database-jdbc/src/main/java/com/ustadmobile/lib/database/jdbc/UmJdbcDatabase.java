package com.ustadmobile.lib.database.jdbc;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;

public interface UmJdbcDatabase {

    Connection getConnection();

    ExecutorService getExecutor();

}
