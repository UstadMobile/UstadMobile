package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.DashboardEntryTag;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class DashboardEntryTagDao
        implements SyncableDao<DashboardEntryTag, DashboardEntryTagDao> {

    @UmQuery("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1")
    public abstract UmProvider<DashboardEntryTag> findAllActiveProvider();

    @UmQuery("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1 " +
            " AND dashboardEntryTagDashboardEntryUid = :uid ")
    public abstract UmProvider<DashboardEntryTag> findByEntryProvider(long uid);

    @UmQuery("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1 " +
            " AND dashboardEntryTagDashboardTagUid = :uid ")
    public abstract UmProvider<DashboardEntryTag> findByTagProvider(long uid);


}
