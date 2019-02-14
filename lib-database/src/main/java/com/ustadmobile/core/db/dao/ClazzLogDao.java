package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(
permissionJoin = "INNER JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid",
selectPermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2,
updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2,
insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2
)
@UmRepository
public abstract class ClazzLogDao implements SyncableDao<ClazzLog, ClazzLogDao> {

    public static class NumberOfDaysClazzesOpen{
        long date;
        int number;

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    /**
     * As the ClazzLog object is added using a timer, we need to ensure that the object created for
     * a specific time should come with the same primary key. For this purposes, we generate a
     * a hashcode using the clazzuid and startTime.
     *
     * @param clazzUid UID of the clazz
     * @param startTime scheduled start time of this instance of the clazz
     * @return a hashcode computed from the above
     */
    public static int generateClazzLogUid(long clazzUid, long startTime) {
        int hash = Long.valueOf(clazzUid).hashCode();
        hash = (31 * hash) + Long.valueOf(startTime).hashCode();
        return hash;
    }


    @UmInsert
    public abstract long insert(ClazzLog entity);

    @UmInsert
    public abstract void insertAsync(ClazzLog entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    public abstract ClazzLog findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogClazzUid = :clazzid AND logDate = :date")
    public abstract ClazzLog findByClazzIdAndDate(long clazzid, long date);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogClazzUid = :clazzid and logDate = :date")
    public abstract void findByClazzIdAndDateAsync(long clazzid, long date,
                                                   UmCallback<ClazzLog> resultObject);

    @UmQuery("SELECT * FROM ClazzLog")
    public abstract List<ClazzLog> findAll();

    @UmQuery("UPDATE ClazzLog SET done = 1 where clazzLogUid = :clazzLogUid ")
    public abstract void updateDoneForClazzLogAsync(long clazzLogUid, UmCallback<Integer> callback);

    @UmQuery("SELECT * FROM ClazzLog where clazzLogClazzUid = :clazzUid ORDER BY logDate DESC")
    public abstract UmProvider<ClazzLog> findByClazzUid(long clazzUid);

    @UmQuery("SELECT * FROM ClazzLog where clazzLogClazzUid = :clazzUid AND done = 1 ORDER BY logDate DESC")
    public abstract UmProvider<ClazzLog> findByClazzUidThatAreDone(long clazzUid);

    @UmQuery("UPDATE ClazzLog SET numPresent = :numPresent,  numAbsent = :numAbsent, " +
            "numPartial = :numPartial WHERE clazzLogUid = :clazzLogUid")
    public abstract void updateClazzAttendanceNumbersAsync(long clazzLogUid, int numPresent,
                                                           int numAbsent, int numPartial,
                                                           UmCallback<Void> callback);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateLocationClazzes(long fromDate, long toDate,
            UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND ClazzLog.clazzLogClazzUid in (:clazzes) " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateLocationClazzes(long fromDate, long toDate,
              List<Long> clazzes, UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    public void getNumberOfClassesOpenForDateLocationClazzes(long fromDate, long toDate,
         List<Long> clazzes, List<Long> locations,
         UmCallback<List<NumberOfDaysClazzesOpen>> resultList){
        if(clazzes.isEmpty()){
            getNumberOfClassesOpenForDateLocationClazzes(fromDate,toDate,resultList);
        }else{
            getNumberOfClassesOpenForDateLocationClazzes(fromDate,toDate,clazzes, resultList);
        }
    }

    public void createClazzLogForDate(long currentClazzUid, long currentLogDate,
                                      UmCallback<Long> callback){

        findByClazzIdAndDateAsync(currentClazzUid, currentLogDate, new UmCallback<ClazzLog>() {
            @Override
            public void onSuccess(ClazzLog result) {
                if(result != null){
                    callback.onSuccess(result.getClazzLogClazzUid());
                }else{
                    //Create one
                    ClazzLog newClazzLog = new ClazzLog();
                    newClazzLog.setLogDate(currentLogDate);
                    newClazzLog.setTimeRecorded(System.currentTimeMillis());
                    newClazzLog.setDone(false);
                    newClazzLog.setClazzLogClazzUid(currentClazzUid);
                    insertAsync(newClazzLog, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            newClazzLog.setClazzLogUid(result);
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

}
