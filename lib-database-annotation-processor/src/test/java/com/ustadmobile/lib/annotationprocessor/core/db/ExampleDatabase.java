package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.UmDbBuilder;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.UmDbWithAttachmentsDir;
import com.ustadmobile.lib.db.UmDbWithAuthenticator;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


@UmDatabase(version = 1, entities = {ExampleEntity.class, ExampleLocation.class,
        ExampleSyncableEntity.class, ExampleSyncableEntityWithAttachment.class,
        SyncStatus.class, SyncablePrimaryKey.class,
        SyncDeviceBits.class})
public abstract class ExampleDatabase implements UmSyncableDatabase, UmDbWithAuthenticator,
        UmDbWithAttachmentsDir {

    private static volatile ExampleDatabase instance;

    private boolean master;

    private String attachmentsDir;

    private static volatile Hashtable<String, ExampleDatabase> namedInstances = new Hashtable<>();

    private Map<Long, String> validAuthTokens = new HashMap<>();

    public static final String VALID_AUTH_TOKEN = "fefe1010fe";

    public static final long VALID_AUTH_TOKEN_USER_UID = 1L;

    public ExampleDatabase() {
        validAuthTokens.put(1L, VALID_AUTH_TOKEN);
    }

    public static synchronized ExampleDatabase getInstance(Object context) {
        if(instance == null){
            instance = UmDbBuilder.builder(ExampleDatabase.class, context).build();
        }

        return instance;
    }

    public static synchronized ExampleDatabase getInstance(Object context, String dbName) {
        ExampleDatabase db = namedInstances.get(dbName);
        if(db == null) {
            db = UmDbBuilder.builder(ExampleDatabase.class, context, dbName).build();
            namedInstances.put(dbName, db);
        }

        return db;
    }


    public abstract ExampleDao getExampleDao();

    public abstract ExampleSyncableDao getExampleSyncableDao();

    public abstract ExampleSyncableEntityWithAttachmentDao getExampleSyncableEntityWithAttachmentDao();

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

    @Override
    public boolean validateAuth(long userUid, String auth) {
        if(!validAuthTokens.containsKey(userUid))
            return false;
        else
            return validAuthTokens.get(userUid).equals(auth);
    }

    @UmSyncOutgoing
    public abstract void syncWith(ExampleDatabase otherDb, long personUid, int sendLimit,int receiveLimit);

    @Override
    public int getDeviceBits() {
        return getSyncablePrimaryKeyDao().getDeviceBits();
    }

    @Override
    public void invalidateDeviceBits() {
        getSyncablePrimaryKeyDao().invalidateDeviceBits();
    }

    @Override
    public String getAttachmentsDir() {
        return attachmentsDir;
    }

    public void setAttachmentsDir(String attachmentsDir) {
        this.attachmentsDir = attachmentsDir;
    }

    @UmSyncCountLocalPendingChanges
    public abstract int countPendingChanges(long personAccountUid, int deiceId);
}
