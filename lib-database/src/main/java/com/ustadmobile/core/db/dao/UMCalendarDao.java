package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.UMCalendar;

import java.util.List;

@UmDao
public abstract class UMCalendarDao implements BaseDao<UMCalendar>{

    @UmInsert
    public abstract long insert(UMCalendar entity);

    @UmUpdate
    public abstract void update(UMCalendar entity);

    @UmInsert
    public abstract void insertAsync(UMCalendar entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM UMCalendar")
    public abstract UmProvider<UMCalendar> findAllUMCalendars();

    @UmQuery("SELECT * FROM UMCalendar")
    public abstract UmLiveData<List<UMCalendar>> findAllUMCalendarsAsLiveDataList();

    @UmUpdate
    public abstract void updateAsync(UMCalendar entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarName = :name")
    public abstract UMCalendar findByName(String name);

    @UmQuery("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    public abstract UMCalendar findByUid(long uid);

}
