package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.database.annotation.UmSyncType;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(syncType = UmSyncType.SYNC_PROACTIVE, readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ExampleSyncableDao implements SyncableDao<ExampleSyncableEntity, ExampleSyncableDao>  {

    @UmSyncFindUpdateable
    @UmQuery("SELECT ExampleSyncableEntity.exampleSyncableUid AS primaryKey, 1 as userCanUpdate " +
            "FROM ExampleSyncableEntity " +
            "WHERE ExampleSyncableEntity.exampleSyncableUid IN (:primaryKeys)")
    public abstract List<UmSyncExistingEntity> syncFindExistingEntities(List<Long> primaryKeys, long accountPersonUid);

    @UmQuery("SELECT localChangeSeqNum FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid")
    @UmRestAccessible
    public abstract int getLocalChangeByUid(long uid);

    @UmQuery("SELECT title FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid")
    @UmRestAccessible
    public abstract String getTitleByUid(long uid);

    @UmQuery("SELECT title FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid")
    @UmRestAccessible
    public abstract void getTitleByUidAsync(long uid, UmCallback<String> callback);

    @UmQuery("UPDATE ExampleSyncableEntity SET title = :title WHERE uid = :uid")
    @UmRestAccessible
    public abstract void updateTitle(long uid, String title);

    @UmQuery("UPDATE ExampleSyncableEntity SET title = :title WHERE uid = :uid")
    @UmRestAccessible
    public abstract void updateTitleAsync(long uid, String title, UmCallback<Void> callback);

    @UmRestAccessible
    @UmUpdate
    public abstract void updateEntity(ExampleSyncableEntity entity);

    @UmQuery("SELECT title FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid")
    @UmRestAccessible
    public abstract UmLiveData<String> findTitleLive(long uid);

    @UmQuery("SELECT * FROM ExampleSyncableEntity")
    @UmRestAccessible
    public abstract UmLiveData<List<ExampleSyncableEntity>> findAllLive();

    @UmQuery("SELECT * FROM ExampleSyncableEntity")
    public abstract List<ExampleSyncableEntity> findAll();

    @UmInsert
    @UmRestAccessible
    public abstract void insertRest(ExampleSyncableEntity entity, UmCallback<Long> callback);

    @UmInsert
    @UmRestAccessible
    public abstract void insertRestList(List<ExampleSyncableEntity> entityList, UmCallback<Void> callback);

    @UmInsert
    @UmRestAccessible
    public abstract void insertRestListAndReturnIds(List<ExampleSyncableEntity> entityList, UmCallback<List<Long>> callback);


    @UmQuery("SELECT * FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid")
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    @UmRestAccessible
    public abstract void findByUidAsyncRepo(long uid, UmCallback<ExampleSyncableEntity> callback);

    @UmInsert
    public abstract void insertListAsyncArr(List<ExampleSyncableEntity> entityList,
                                            UmCallback<Long[]> insertedKeys);


    @UmQuery("SELECT title FROM ExampleSyncableEntity WHERE exampleSyncableUid = :uid AND (:personUid = :personUid)")
    @UmRestAccessible
    public abstract String getTitleByUidAuthenticated(long uid, @UmRestAuthorizedUidParam long personUid);


}
