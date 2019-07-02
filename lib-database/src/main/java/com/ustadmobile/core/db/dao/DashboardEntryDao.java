package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class DashboardEntryDao
        implements SyncableDao<DashboardEntry, DashboardEntryDao> {


    @UmQuery("UPDATE DashboardEntry SET dashboardEntryTitle = :title " +
            " WHERE dashboardEntryUid = :uid")
    public abstract void updateTitle(long uid, String title, UmCallback<Integer> resultCallback);

    @UmQuery("SELECT * FROM DashboardEntry WHERE " +
            "dashboardEntryPersonUid = :uid AND dashboardEntryActive = 1 ORDER BY dashboardEntryIndex ASC")
    public abstract UmProvider<DashboardEntry> findByPersonAndActiveProvider(long uid);


    @UmQuery("UPDATE DashboardEntry SET dashboardEntryIndex = -1 WHERE  dashboardEntryUid = :uid")
    public abstract void pinEntry(long uid, UmCallback<Integer> resultCallback);

    @UmQuery("UPDATE DashboardEntry SET dashboardEntryIndex = " +
            "(SELECT (SELECT MAX(dashboardEntryIndex) FROM DashboardEntry) +1) " +
            "WHERE dashboardEntryUid = :uid")
    public abstract void unpinEntry(long uid, UmCallback<Integer> resultCallback);

    @UmQuery("UPDATE DashboardEntry SET dashboardEntryActive = 0 WHERE dashboardEntryUid = :uid")
    public abstract void deleteEntry(long uid, UmCallback<Integer> resultCallback);

    @UmQuery("SELECT * FROM DashboardEntry WHERE dashboardEntryUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<DashboardEntry> resultCallback);

    @UmUpdate
    public abstract void updateAsync(DashboardEntry entity, UmCallback<Integer> resultCallback);

}
