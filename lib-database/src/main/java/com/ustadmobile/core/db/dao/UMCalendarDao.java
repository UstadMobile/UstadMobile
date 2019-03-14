package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.UMCalendar;
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class UMCalendarDao implements SyncableDao<UMCalendar, UMCalendarDao> {

    @UmInsert
    public abstract long insert(UMCalendar entity);

    @UmUpdate
    public abstract void update(UMCalendar entity);

    @UmInsert
    public abstract void insertAsync(UMCalendar entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarActive = 1 AND " +
            " umCalendarCategory = " + UMCalendar.CATEGORY_HOLIDAY )
    public abstract UmProvider<UMCalendar> findAllHolidays();

    @UmQuery("SELECT * , 0 AS numEntries FROM UMCalendar WHERE umCalendarActive = 1 AND " +
            " umCalendarCategory = " + UMCalendar.CATEGORY_HOLIDAY )
    public abstract UmProvider<UMCalendarWithNumEntries> findAllHolidaysWithEntriesCount();

    @UmQuery("SELECT * FROM UMCalendar")
    public abstract UmProvider<UMCalendar> findAllUMCalendars();

    @UmQuery("SELECT * FROM UMCalendar")
    public abstract UmLiveData<List<UMCalendar>> findAllUMCalendarsAsLiveDataList();

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarCategory = " + UMCalendar.CATEGORY_HOLIDAY )
    public abstract UmLiveData<List<UMCalendar>> findAllHolidaysLiveData();

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    public abstract UmLiveData<UMCalendar> findByUidLive(long uid);

    @UmUpdate
    public abstract void updateAsync(UMCalendar entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarName = :name")
    public abstract UMCalendar findByName(String name);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    public abstract UMCalendar findByUid(long uid);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<UMCalendar> resultObject);

}
