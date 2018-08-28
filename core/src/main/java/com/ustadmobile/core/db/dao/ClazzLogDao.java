package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ClazzLog;

import java.util.List;

@UmDao
public abstract class ClazzLogDao implements BaseDao<ClazzLog>{

    @UmInsert
    public abstract long insert(ClazzLog entity);

    @UmInsert
    public abstract void insertAsync(ClazzLog entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    public abstract ClazzLog findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzClazzUid = :clazzid AND logDate = :date")
    public abstract ClazzLog findByClazzIdAndDate(long clazzid, long date);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzClazzUid = :clazzid and logDate = :date")
    public abstract void findByClazzIdAndDateAsync(long clazzid, long date, UmCallback<ClazzLog> result);

    @UmQuery("SELECT * FROM ClazzLog")
    public abstract List<ClazzLog> findAll();

    public void createClazzLogForDate(long currentClazzUid, long currentLogDate, UmCallback<Long> callback){

        findByClazzIdAndDateAsync(currentClazzUid, currentLogDate, new UmCallback<ClazzLog>() {
            @Override
            public void onSuccess(ClazzLog result) {
                if(result != null){
                    callback.onSuccess(result.getClazzClazzUid());
                }else{
                    System.out.println("Sucess but null");
                    //Create one
                    ClazzLog newClazzLog = new ClazzLog();
                    newClazzLog.setLogDate(currentLogDate);
                    newClazzLog.setTimeRecorded(System.currentTimeMillis());
                    newClazzLog.setDone(false);
                    newClazzLog.setClazzClazzUid(currentClazzUid);
                    insertAsync(newClazzLog, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            newClazzLog.setClazzLogUid(result);
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            System.out.println(exception);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println("Fail: " + exception);
            }
        });
    }

}
