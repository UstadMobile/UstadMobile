package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;

import java.util.List;

@UmDao
public abstract class ExampleDao {

    @UmInsert
    public abstract void insertE(ExampleEntity entity);

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

}
