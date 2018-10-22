package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Schedule;

@UmDao
public abstract class ScheduleDao implements BaseDao<Schedule> {

    @UmInsert
    public abstract long insert(Schedule entity);

    @UmUpdate
    public abstract void update(Schedule entity);

    @UmInsert
    public abstract void insertAsync(Schedule entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Schedule")
    public abstract UmProvider<Schedule> findAllSchedules();

    @UmUpdate
    public abstract void updateAsync(Schedule entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    public abstract Schedule findByUid(long uid);


    @UmQuery("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid")
    public abstract UmProvider<Schedule> findAllSchedulesByClazzUid(long clazzUid);
}
