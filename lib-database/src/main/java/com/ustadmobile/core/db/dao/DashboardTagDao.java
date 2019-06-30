package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.DashboardTag;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class DashboardTagDao
        implements SyncableDao<DashboardTag,DashboardTagDao> {

    @UmQuery("SELECT * FROM DashboardTag WHERE " +
            " dashboardTagActive = 1")
    public abstract UmProvider<DashboardTag> findAllActiveProvider();


    @UmQuery("SELECT * FROM DashboardTag WHERE " +
            " dashboardTagActive = 1")
    public abstract UmLiveData<List<DashboardTag>> findAllActiveLive();


}
