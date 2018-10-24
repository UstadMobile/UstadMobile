package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Holiday;

@UmDao
public abstract class HolidayDao implements BaseDao<Holiday> {

    @UmInsert
    public abstract long insert(Holiday entity);

    @UmUpdate
    public abstract void update(Holiday entity);

    @UmInsert
    public abstract void insertAsync(Holiday entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Holiday")
    public abstract UmProvider<Holiday> findAllHolidays();

    @UmQuery("SELECT * FROM Holiday WHERE holidayUid = :uid")
    public abstract Holiday findByUid(long uid);

    @UmUpdate
    public abstract void updateAsync(Holiday entity, UmCallback<Integer> result);

}
