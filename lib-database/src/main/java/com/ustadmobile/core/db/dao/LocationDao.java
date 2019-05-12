package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class LocationDao implements SyncableDao<Location, LocationDao> {


    @UmInsert
    public abstract long insert(Location entity);

    @UmInsert
    public abstract void insertAsync(Location entity, UmCallback<Long> resultObject);

    @UmUpdate
    public abstract void update(Location entity);

    @UmUpdate
    public abstract void updateAsync(Location entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM Location WHERE locationUid = :uid")
    public abstract Location findByUid(long uid);

    @UmQuery("SELECT * FROM Location WHERE locationUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Location> resultObject);

    @UmQuery("SELECT * FROM Location WHERE parentLocationUid = 0")
    public abstract void findTopLocationsAsync(UmCallback<List<Location>> resultList);

    @UmQuery("SELECT * FROM Location WHERE parentLocationUid = :uid")
    public abstract void findAllChildLocationsForUidAsync(long uid, UmCallback<List<Location>> resultList);

    @UmQuery("SELECT * FROM Location WHERE title = :name")
    public abstract void findByTitleAsync(String name, UmCallback<List<Location>> resultList);

}
