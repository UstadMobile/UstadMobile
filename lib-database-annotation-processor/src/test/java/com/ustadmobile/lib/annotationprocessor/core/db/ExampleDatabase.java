package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;


@UmDatabase(version = 1, entities = {ExampleEntity.class, ExampleLocation.class,
        ExampleSyncableEntity.class, SyncStatus.class})
public abstract class ExampleDatabase {

    private static volatile ExampleDatabase instance;

    public static synchronized ExampleDatabase getInstance(Object context) {
        if(instance == null){
            instance = ExampleDatabase_Factory.makeExampleDatabase(context);
        }

        return instance;
    }

    public static synchronized ExampleDatabase getInstance(Object context, String dbName) {
        return ExampleDatabase_Factory.makeExampleDatabase(context, dbName);
    }


    public abstract ExampleDao getExampleDao();

    public abstract ExampleSyncableDao getExampleSyncableDao();

    @UmClearAll
    public abstract void clearAll();

    public abstract SyncStatusDao getSyncStatusDao();

}
