package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.database.UmDbBuilder;
import com.ustadmobile.lib.db.DbCallback;
import com.ustadmobile.lib.db.DoorwayDbAdapter;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestDbBuilder {

    @Test
    public void givenDbWithCallbacks_whenCreated_thenShouldRunCreateAndOpenCallback() {
        File sqliteDbFile = new File("build/tmp/testbuilderoncreate.sqlite");
        if(sqliteDbFile.exists())
            sqliteDbFile.delete();

        DbCallback createExampleEntityCallback = new DbCallback() {
            @Override
            public void onCreate(DoorwayDbAdapter dbHelper) {
                dbHelper.execSql("INSERT INTO ExampleEntity (uid, name, locationPk) VALUES (42, 'onCreate', 0)");
            }

            @Override
            public void onOpen(DoorwayDbAdapter dbHelper) {
                dbHelper.execSql("INSERT INTO ExampleEntity (uid, name, locationPk) VALUES (43, 'onOpen', 0)");
            }
        };

        DbCallback testCallback = spy(createExampleEntityCallback);
        ExampleDatabase exampleDb = UmDbBuilder
                .builder(ExampleDatabase.class, null, "testbuilderoncreate")
                .addCallback(testCallback).build();

        verify(testCallback, times(1)).onCreate(any());
        verify(testCallback, times(1)).onOpen(any());

        Assert.assertEquals("Entity inserted onCreate name matches",
                "onCreate", exampleDb.getExampleDao().findByUid(42).getName());
        Assert.assertEquals("Entity inserted onOpen name matches",
                "onOpen", exampleDb.getExampleDao().findByUid(43).getName());
    }

    @Test
    public void givenDbWithCallbacks_whenOpened_thenShouldRunOpenCallbackOnly() {
        File sqliteDbFile = new File("build/tmp/testbuilderonopen.sqlite");
        if(sqliteDbFile.exists())
            sqliteDbFile.delete();


        UmDbBuilder.builder(ExampleDatabase.class, null, "testbuilderonopen")
                .build();

        System.gc();

        DbCallback openExampleEntityCallback = new DbCallback() {
            @Override
            public void onOpen(DoorwayDbAdapter dbHelper) {
                dbHelper.execSql("INSERT INTO ExampleEntity (uid, name, locationPk) VALUES (43, 'onOpen', 0)");
            }
        };
        openExampleEntityCallback = spy(openExampleEntityCallback);

        ExampleDatabase openedDb = UmDbBuilder
                .builder(ExampleDatabase.class, null, "testbuilderonopen")
                .addCallback(openExampleEntityCallback).build();

        verify(openExampleEntityCallback, times(0)).onCreate(any());
        verify(openExampleEntityCallback, times(1)).onOpen(any());

        Assert.assertEquals("Entity inserted onOpen name matches",
                "onOpen", openedDb.getExampleDao().findByUid(43).getName());
    }


}
