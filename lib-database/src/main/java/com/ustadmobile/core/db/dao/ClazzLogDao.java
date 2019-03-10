package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes;
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
     * Small POJO used by the attendance screen to get a list of valid dates for the class (to show
     * in a list) and their UID so they can be looked up.
     */
    public static class ClazzLogUidAndDate {

        private long clazzLogUid;

        private long logDate;

        public ClazzLogUidAndDate() {

        }

        public ClazzLogUidAndDate(ClazzLog clazzLog) {
            this.clazzLogUid = clazzLog.getClazzLogUid();
            this.logDate = clazzLog.getLogDate();
        }

        public long getClazzLogUid() {
            return clazzLogUid;
        }

        public void setClazzLogUid(long clazzLogUid) {
            this.clazzLogUid = clazzLogUid;
        }

        public long getLogDate() {
            return logDate;
        }

        public void setLogDate(long logDate) {
            this.logDate = logDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClazzLogUidAndDate that = (ClazzLogUidAndDate) o;

            if (clazzLogUid != that.clazzLogUid) return false;
            return logDate == that.logDate;
        }

        @Override
        public int hashCode() {
            int result = (int) (clazzLogUid ^ (clazzLogUid >>> 32));
            result = 31 * result + (int) (logDate ^ (logDate >>> 32));
            return result;
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

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long replace(ClazzLog entity);

    @UmInsert
    public abstract void insertAsync(ClazzLog entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    public abstract ClazzLog findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<ClazzLog> callback);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid ORDER BY logDate DESC LIMIT 1")
    public abstract void findMostRecentByClazzUid(long clazzUid, UmCallback<ClazzLog> callback);

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

    @UmQuery("SELECT * FROM ClazzLog where clazzLogClazzUid = :clazzUid ORDER BY logDate DESC")
    public abstract List<ClazzLog> findByClazzUidAsList(long clazzUid);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid AND NOT canceled")
    public abstract UmProvider<ClazzLog> findByClazzUidNotCanceled(long clazzUid);

    @UmQuery("SELECT ClazzLog.*, Schedule.sceduleStartTime, Schedule.scheduleEndTime FROM ClazzLog " +
            "LEFT JOIN Schedule ON Schedule.scheduleUid = ClazzLog.clazzLogScheduleUid " +
            "WHERE clazzLogClazzUid = :clazzUid AND NOT canceled")
    public abstract UmProvider<ClazzLogWithScheduleStartEndTimes>
                                            findByClazzUidNotCancelledWithSchedule(long clazzUid);

    @UmQuery("UPDATE ClazzLog SET numPresent = :numPresent,  numAbsent = :numAbsent, " +
            "numPartial = :numPartial WHERE clazzLogUid = :clazzLogUid")
    public abstract void updateClazzAttendanceNumbersAsync(long clazzLogUid, int numPresent,
                                                           int numAbsent, int numPartial,
                                                           UmCallback<Void> callback);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDate(long fromDate, long toDate,
                                                              UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND ClazzLog.clazzLogClazzUid in (:clazzes) " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateClazzes(long fromDate, long toDate,
                          List<Long> clazzes, UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND Clazz.clazzLocationUid in (:locations) " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateLocations(long fromDate, long toDate,
                                                              List<Long> locations, UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND ClazzLog.clazzLogClazzUid in (:clazzes) " +
            "       AND Clazz.clazzLocationUid in (:locations) " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateClazzesLocation(long fromDate, long toDate,
                                    List<Long> clazzes, List<Long> locations,
                                    UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    public void getNumberOfClassesOpenForDateClazzes(long fromDate, long toDate,
                                                     List<Long> clazzes, List<Long> locations,
                                                     UmCallback<List<NumberOfDaysClazzesOpen>> resultList){
        if(locations.isEmpty()){
            if(clazzes.isEmpty()){
                getNumberOfClassesOpenForDate(fromDate, toDate, resultList);
            }else{
                getNumberOfClassesOpenForDateClazzes(fromDate, toDate, clazzes, resultList);
            }
        }else{
            if(clazzes.isEmpty()){
                getNumberOfClassesOpenForDateLocations(fromDate, toDate, locations, resultList);
            }else{
                getNumberOfClassesOpenForDateClazzesLocation(fromDate, toDate, clazzes, locations, resultList);
            }
        }
        
    }

    @UmQuery("UPDATE ClazzLog SET canceled = :canceled WHERE clazzLogScheduleUid = :scheduleUid AND logDate >= :after ")
    public abstract void cancelFutureInstances(long scheduleUid, long after, boolean canceled);

    @UmQuery("SELECT ClazzLog.clazzLogUid, ClazzLog.logDate FROM ClazzLog " +
            " WHERE clazzLogClazzUid = :clazzUid ORDER BY logDate ASC")
    public abstract void getListOfClazzLogUidsAndDatesForClazz(long clazzUid,
                                                               UmCallback<List<ClazzLogUidAndDate>> callback);

}
