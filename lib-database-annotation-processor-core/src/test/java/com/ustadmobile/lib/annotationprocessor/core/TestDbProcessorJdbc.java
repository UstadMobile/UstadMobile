package com.ustadmobile.lib.annotationprocessor.core;


import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleEntity;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Tests the result of the JDBC generation
 */
public class TestDbProcessorJdbc {


    @BeforeClass
    public static void setupClass() throws NamingException{
        InitialContext ic = new InitialContext();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:test.sqlite");
        ic.bind("java:/comp/env/jdbc/ds", dataSource);
    }

    @Test
    public void testConnection() {
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");
        Assert.assertNotNull(db);
    }

    @Test
    public void givenDatabaseInstance_whenInitialized_thenTableShouldBeCreated() throws NamingException, SQLException{
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");

        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource)ic.lookup("java:/comp/env/jdbc/ds");

        try (
                Connection con = ds.getConnection();
        ){
            List<String> tableNames = JdbcDatabaseUtils.getTableNames(con);
            Assert.assertTrue("ExampleEntity table was created",
                    tableNames.contains("ExampleEntity"));
        }
    }

    @Test
    public void givenDatabaseInstance_whenDataInserted_thenShouldBeRetrievable() {
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");
        ExampleEntity entity = new ExampleEntity();
        entity.setName("Bob Jones");
        int newId = db.getExampleDao().insertGetId(entity);
        List<ExampleEntity> allEntities = db.getExampleDao().getAllEntities();
        ExampleEntity firstEntity = db.getExampleDao().findByUid(newId);
        Assert.assertNotNull("Retrieved first entity", firstEntity);
        Assert.assertTrue("All entities list is not empty", !allEntities.isEmpty());
    }



    @Test
    public void givenLiveDataObserving_whenDataUpdated_thenShouldCallOnChange() {
        ExampleDatabase db = ExampleDatabase.getInstance(null, "ds");
        ExampleEntity entityToUpdate = new ExampleEntity();
        entityToUpdate.setName("Name1");
        int uid = db.getExampleDao().insertGetId(entityToUpdate);

        CountDownLatch latch = new CountDownLatch(1);
        ExampleEntity[] entityVal = new ExampleEntity[1];
        UmLiveData<ExampleEntity> entityUmLiveData = db.getExampleDao().findByUidLive(uid);
        entityUmLiveData.observeForever((newValue) -> {
            entityVal[0] = newValue;
            if(newValue != null && newValue.getName().equals("Name2"))
                latch.countDown();
        });

        db.getExampleDao().updateNameByUid("Name2", uid);
        try {
            latch.await(5, TimeUnit.SECONDS);
        }catch(InterruptedException e) {}

        Assert.assertEquals("Updated value provided by live data onChange", "Name2",
                entityVal[0].getName());
    }

}
