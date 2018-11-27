package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;

import java.util.List;

@UmDao
public abstract class ExampleDao {

    @UmInsert
    public abstract void insertE(ExampleEntity entity);

    @UmInsert
    public abstract int insertGetId(ExampleEntity entity);

    @UmInsert
    public abstract void insertAndGetIdAsync(ExampleEntity entity, UmCallback<Integer> callback);

    @UmInsert
    public abstract void insertAsync(ExampleEntity entity, UmCallback<Void> callback);

    @UmInsert
    public abstract void insertList(List<ExampleEntity> entities);

    @UmInsert
    public abstract void insertArray(ExampleEntity[] entities);

    @UmQuery("SELECT * FROM ExampleEntity")
    public abstract List<ExampleEntity> getAllEntities();

    @UmQuery("SELECT * FROM ExampleEntity")
    public abstract ExampleEntity[] getAllEntitiesAsArray();

    @UmQuery("SELECT * FROM ExampleEntity WHERE uid = :uid ")
    public abstract ExampleEntity findByUid(int uid);

    @UmQuery("SELECT COUNT(*) FROM ExampleEntity")
    public abstract int countNumElements();

    @UmQuery("SELECT * FROM ExampleEntity " +
            "LEFT JOIN ExampleLocation ON ExampleEntity.locationPk = ExampleLocation.locationUid")
    public abstract List<ExampleEntityWithLocation> getAllEntitiesWithLocation();

    @UmQuery("SELECT * FROM ExampleEntity WHERE uid = :uid ")
    public abstract void findExampleEntityByUidAsync(int uid, UmCallback<ExampleEntity> callback);

    @UmQuery("SELECT * FROM ExampleEntity")
    public abstract void findAllExampleEntitiesAsync(UmCallback<List<ExampleEntity>> callback);

    @UmQuery("UPDATE ExampleEntity SET name = :name WHERE uid = :uid")
    public abstract int updateNameByUid(String name, int uid);

    @UmQuery("UPDATE ExampleEntity SET name = :name WHERE uid = :uid")
    public abstract void updateNameByUidVoid(String name, int uid);

    @UmQuery("UPDATE ExampleEntity SET name = :name WHERE uid = :uid")
    public abstract void updateNameByUidAsync(String name, int uid, UmCallback<Integer> callback);

    @UmQuery("UPDATE ExampleEntity SET name = :name WHERE uid = :uid")
    public abstract void updateNameByUidVoidAsync(String name, int uid, UmCallback<Void> callback);

    @UmQuery("SELECT * FROM ExampleEntity WHERE uid = :uid")
    public abstract UmLiveData<ExampleEntity> findByUidLive(int uid);

    @UmUpdate
    public abstract void update(ExampleEntity entity);

    @UmUpdate
    public abstract void update(List<ExampleEntity> entityList);

    @UmUpdate
    public abstract void updateAsync(ExampleEntity entity, UmCallback<Void> callback);

    @UmUpdate
    public abstract int updateAndGetCount(ExampleEntity entity);

    @UmDelete
    public abstract void delete(ExampleEntity entity);

    @UmDelete
    public abstract void deleteAsync(ExampleEntity entity, UmCallback<Void> callback);

    @UmDelete
    public abstract void deleteList(List<ExampleEntity> entities);

    @UmDelete
    public abstract int deleteAndGetCount(ExampleEntity entity);

    @UmQuery("SELECT * FROM ExampleEntity")
    public abstract UmProvider<ExampleEntity> getAllAsProvider();

    @UmQuery("SELECT * FROM ExampleEntity WHERE name IN (:nameArr)")
    public abstract List<ExampleEntity> findByTitleArrValues(List<String> nameArr);

    @UmQuery("SELECT * FROM ExampleEntity WHERE uid IN (:uidArr)")
    public abstract List<ExampleEntity> findByUidArrValues(List<Long> uidArr);


}
