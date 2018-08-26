package com.ustadmobile.lib.database.jdbc;


import com.utadmobile.lib.database.jdbc.db.ExampleDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class TestDatabaseJdbc {


    @BeforeClass
    public static void setupClass() throws NamingException{
        InitialContext ic = new InitialContext();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:memory:");
        ic.bind("java:/comp/env/jdbc/ds", dataSource);
    }

    @Test
    public void testConnection() {
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");
        Assert.assertNotNull(db);
    }

    @Test
    public void testTableCreated() throws NamingException, SQLException{
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");

        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource)ic.lookup("java:/comp/env/jdbc/ds");

        Connection con = ds.getConnection();
        List<String> tableNames = JdbcDatabaseUtils.getTableNames(con);

        Assert.assertTrue("ExampleEntity table was created", tableNames.contains("ExampleEntity"));
    }




}
