package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class DateRangeDao implements SyncableDao<DateRange, DateRangeDao> {

    @UmInsert
    public abstract long insert(DateRange entity);

    @UmUpdate
    public abstract void update(DateRange entity);

    @UmInsert
    public abstract void insertAsync(DateRange entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM DateRange")
    public abstract UmProvider<DateRange> findAllDateRanges();

    @UmQuery("SELECT * FROM DateRange WHERE dateRangeUMCalendarUid = :calendarUid")
    public abstract UmProvider<DateRange> findAllDatesInCalendar(long calendarUid);

    @UmQuery("SELECT * FROM DateRange WHERE dateRangeUid = :uid")
    public abstract DateRange findByUid(long uid);

    @UmQuery("SELECT * FROM DateRange WHERE dateRangeUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<DateRange> resultObject);

    @UmQuery("UPDATE DateRange SET dateRangeActive = 0 WHERE dateRangeUid = :uid")
    public abstract void inactivateRange(long uid);

    @UmUpdate
    public abstract void updateAsync(DateRange entity, UmCallback<Integer> result);


}
