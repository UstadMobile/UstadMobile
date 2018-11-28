package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.UmDbBuilder;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.Hashtable;


@UmDatabase(version = 1, entities = {ExampleEntity.class, ExampleLocation.class,
        ExampleSyncableEntity.class, SyncStatus.class, SyncablePrimaryKey.class,
        SyncDeviceBits.class})
public abstract class ExampleDatabase implements UmSyncableDatabase {

    private static volatile ExampleDatabase instance;

    private boolean master;

    private static volatile Hashtable<String, ExampleDatabase> namedInstances = new Hashtable<>();

    public static synchronized ExampleDatabase getInstance(Object context) {
        if(instance == null){
            instance = UmDbBuilder.makeDatabase(ExampleDatabase.class, context);
        }

        return instance;
    }

    public static synchronized ExampleDatabase getInstance(Object context, String dbName) {
        ExampleDatabase db = namedInstances.get(dbName);
        if(db == null) {
            db = UmDbBuilder.makeDatabase(ExampleDatabase.class, context, dbName);
            namedInstances.put(dbName, db);
        }

        return db;
    }


    public abstract ExampleDao getExampleDao();

    public abstract ExampleSyncableDao getExampleSyncableDao();

    @UmClearAll
    public abstract void clearAll();

    @Override
    public abstract SyncStatusDao getSyncStatusDao();

    @Override
    public abstract SyncablePrimaryKeyDao getSyncablePrimaryKeyDao();

    @Override
    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    @UmRepository
    public abstract ExampleDatabase getRepository(String baseUrl, String auth);

}
