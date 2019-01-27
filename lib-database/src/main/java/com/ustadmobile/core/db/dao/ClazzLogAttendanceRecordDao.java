package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.ReportMasterItem;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.ArrayList;
import java.util.List;

@UmDao(permissionJoin = "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
        "LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid ",
selectPermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
         Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2,
updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2,
insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2
)
@UmRepository
public abstract class ClazzLogAttendanceRecordDao implements
        SyncableDao<ClazzLogAttendanceRecord, ClazzLogAttendanceRecordDao> {

    @UmInsert
    public abstract long insert(ClazzLogAttendanceRecord entity);

    @UmInsert
    public abstract void insertAsync(ClazzLogAttendanceRecord entity, UmCallback<Long> resultObject);

    @UmInsert
    public abstract void insertListAsync(List<ClazzLogAttendanceRecord> entities,
                                         UmCallback<Long[]> callback);

    @UmQuery("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    public abstract ClazzLogAttendanceRecord findByUid(long uid);

    @UmQuery("SELECT * from ClazzLogAttendanceRecord " +
            "WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid")
    public abstract UmProvider<ClazzLogAttendanceRecord> findAttendanceLogsByClassLogId(long clazzLogUid);

    public static class AttendanceByThresholdRow {

        private int age;

        private int totalLowAttendanceMale;

        private int totalLowAttendanceFemale;

        private int totalMediumAttendanceMale;

        private int totalMediumAttendanceFemale;

        private int totalHighAttendanceMale;

        private int totalHighAttendanceFemale;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getTotalLowAttendanceMale() {
            return totalLowAttendanceMale;
        }

        public void setTotalLowAttendanceMale(int totalLowAttendanceMale) {
            this.totalLowAttendanceMale = totalLowAttendanceMale;
        }

        public int getTotalLowAttendanceFemale() {
            return totalLowAttendanceFemale;
        }

        public void setTotalLowAttendanceFemale(int totalLowAttendanceFemale) {
            this.totalLowAttendanceFemale = totalLowAttendanceFemale;
        }

        public int getTotalMediumAttendanceMale() {
            return totalMediumAttendanceMale;
        }

        public void setTotalMediumAttendanceMale(int totalMediumAttendanceMale) {
            this.totalMediumAttendanceMale = totalMediumAttendanceMale;
        }

        public int getTotalMediumAttendanceFemale() {
            return totalMediumAttendanceFemale;
        }

        public void setTotalMediumAttendanceFemale(int totalMediumAttendanceFemale) {
            this.totalMediumAttendanceFemale = totalMediumAttendanceFemale;
        }

        public int getTotalHighAttendanceMale() {
            return totalHighAttendanceMale;
        }

        public void setTotalHighAttendanceMale(int totalHighAttendanceMale) {
            this.totalHighAttendanceMale = totalHighAttendanceMale;
        }

        public int getTotalHighAttendanceFemale() {
            return totalHighAttendanceFemale;
        }

        public void setTotalHighAttendanceFemale(int totalHighAttendanceFemale) {
            this.totalHighAttendanceFemale = totalHighAttendanceFemale;
        }
    }

    public class AttendanceResultGroupedByAgeAndThreshold{
        int total;
        int gender;
        int age;
        String thresholdGroup;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getThresholdGroup() {
            return thresholdGroup;
        }

        public void setThresholdGroup(String thresholdGroup) {
            this.thresholdGroup = thresholdGroup;
        }
    }

    @UmQuery("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "  " +
            "  " +
            " " +
            " " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid " +
            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "    " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            "    " +
            "    " +
            " ) numSessionsTbl " +
            " " +
            " " +
            " " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    public abstract void getAttendanceGroupedByThresholds(long datetimeNow, long fromTime,
                          long toTime, float lowAttendanceThreshold,
                          float midAttendanceThreshold,
                          UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> resultList);

    @UmQuery("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid " +
            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    public abstract void getAttendanceGroupedByThresholds(long datetimeNow, long fromTime,
                          long toTime, float lowAttendanceThreshold,
                          float midAttendanceThreshold, List<Long> clazzes,
                          UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> resultList);

    @UmQuery("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid, " +

            "   locationUid " +         //added

            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            "   LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +       //added
            "   LEFT JOIN Location ON Location.locationUid = Clazz.clazzLocationUid " + //added

            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE numSessionsTbl.locationUid = :locationUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    public abstract void getAttendanceGroupedByThresholds(long datetimeNow, long fromTime,
                          long toTime, float lowAttendanceThreshold,
                          float midAttendanceThreshold, List<Long> clazzes,
                          long locationUid,
                          UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> resultList);

    @UmQuery("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid, " +

            "   locationUid " +         //added

            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            "   LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +       //added
            "   LEFT JOIN Location ON Location.locationUid = Clazz.clazzLocationUid " + //added

            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE numSessionsTbl.locationUid = :locationUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    public abstract void getAttendanceGroupedByThresholds(long datetimeNow, long fromTime,
                          long toTime, float lowAttendanceThreshold,
                          float midAttendanceThreshold,
                          long locationUid,
                          UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> resultList);

    public void getAttendanceGroupedByThresholds(long datetimeNow, long fromTime,
                 long toTime, float lowAttendanceThreshold, float midAttendanceThreshold,
                 List<Long> clazzes, List<Long> locations,
                 UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> resultList ){
        if(clazzes.isEmpty()){
            getAttendanceGroupedByThresholds(datetimeNow, fromTime, toTime, lowAttendanceThreshold,
                    midAttendanceThreshold, resultList);
        }else{
            getAttendanceGroupedByThresholds(datetimeNow, fromTime, toTime, lowAttendanceThreshold,
                    midAttendanceThreshold, clazzes, resultList);
        }
    }

    @UmQuery("SELECT ClazzLogAttendanceRecord.* , Person.* " +
            " FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzMember " +
            " on ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            "AND ClazzMember.role = 1")
    public abstract UmProvider<ClazzLogAttendanceRecordWithPerson>
                                    findAttendanceRecordsWithPersonByClassLogId (long clazzLogUid);

    @UmQuery("SELECT ClazzMember.clazzMemberUid FROM ClazzMember WHERE " +
            " ClazzMember.clazzMemberClazzUid = :clazzId " +
            " AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT +
            " AND ClazzMember.clazzMemberClazzUid " +
            " EXCEPT " +
            "SELECT clazzLogAttendanceRecordClazzMemberUid FROM ClazzLogAttendanceRecord " +
            " WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid"
    )
    public abstract void findPersonUidsWithNoClazzAttendanceRecord(long clazzId, long clazzLogUid,
                                                                   UmCallback<List<Long>> callback);


    @UmQuery("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, ClazzLog.logDate, " +
            " sum(case when attendanceStatus = 1 then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = 2 then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = 4 then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " (:clazzUid) as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE+
            " then 1 else 0 end) *1.0/ COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_MALE +
            " then 1 else 0 end) *1.0/ COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " LEFT JOIN ClazzMember ON " +
            "ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +

            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            " AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            "group by (ClazzLog.logDate)")
    public abstract void findDailyAttendanceByClazzUidAndDateAsync( long clazzUid, long fromDate,
                            long toDate, UmCallback<List<DailyAttendanceNumbers>> resultObject);


    @UmQuery("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    public abstract void findOverallDailyAttendanceNumbersByDateAndStuff(long fromDate,
            long toDate, List<Long> clazzes, UmCallback<List<DailyAttendanceNumbers>> resultObject);


    @UmQuery("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    public abstract void findOverallDailyAttendanceNumbersByDateAndStuff(long fromDate,
             long toDate, UmCallback<List<DailyAttendanceNumbers>> resultObject);


    /**
     * TODO: Account for Locations,.
     * @param fromDate
     * @param toDate
     * @param clazzes
     * @param locations
     * @param resultObject
     */
    public void findOverallDailyAttendanceNumbersByDateAndStuff(long fromDate, long toDate,
                List<Long> clazzes, List<Long> locations,
                UmCallback<List<DailyAttendanceNumbers>> resultObject){
        if(clazzes.isEmpty()){
            findOverallDailyAttendanceNumbersByDateAndStuff(fromDate, toDate, resultObject);
        }else{
            findOverallDailyAttendanceNumbersByDateAndStuff(fromDate, toDate,clazzes,resultObject);
        }
    }

    /**
     * Checks for ClazzMembers not in a particular Clazz that are not part of the
     * ClazzLogAttendanceRecord and creates their ClazzLogAttendanceRecords.
     *
     * @param clazzId
     * @param clazzLogUid
     * @param callback
     */
    public void insertAllAttendanceRecords(long clazzId, long clazzLogUid,
                                           UmCallback<Long[]> callback) {
        findPersonUidsWithNoClazzAttendanceRecord(clazzId, clazzLogUid, new UmCallback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> result) {
                if(result.isEmpty()) {
                    UmCallbackUtil.onSuccessIfNotNull(callback, null);
                }else {
                    List<ClazzLogAttendanceRecord> toInsert = new ArrayList<>();
                    for(long clazzMemberUid : result) {
                        ClazzLogAttendanceRecord record = new ClazzLogAttendanceRecord();
                        record.setClazzLogAttendanceRecordClazzLogUid(clazzLogUid);
                        record.setClazzLogAttendanceRecordClazzMemberUid(clazzMemberUid);
                        toInsert.add(record);
                    }

                    insertListAsync(toInsert, callback);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });
    }

    @UmQuery("SELECT " +
            " Clazz.clazzName, Clazz.clazzUid as clazzUid, Person.firstNames, Person.lastName, Person.personUid, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED + " THEN 1 ELSE 0 END) as daysPresent, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT + " THEN 1 ELSE 0 END) as daysAbsent, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL + " THEN 1 ELSE 0 END) as daysPartial, " +
            " COUNT(*) as clazzDays, " +
            " ClazzMember.dateLeft, " +
            " ClazzMember.clazzMemberActive,  " +
            " Person.gender, " +
            " Person.dateOfBirth " +
            " FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = clazzLogAttendanceRecordClazzMemberUid " +
            " LEFT JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid " +
            " LEFT JOIN ClazzLog ON ClazzLog.clazzLogUid = clazzLogAttendanceRecordClazzLogUid " +
            " LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +
            " WHERE " +
            " ClazzLog.done = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            " GROUP BY clazzMemberUid " +
            " ORDER BY clazzName ")
    public abstract void findMasterReportDataForAllAsync(long fromDate, long toDate,
        UmCallback<List<ReportMasterItem>> resultList);

    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid AND " +
            "attendanceStatus != :attendanceStatus")
    public abstract void updateAllByClazzLogUid(long clazzLogUid, int attendanceStatus,
                                                UmCallback<Integer> callback);


    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordUid = :clazzLogAttendanceRecordUid AND " +
            " attendanceStatus != :attendanceStatus")
    public abstract void updateAttendanceStatus(long clazzLogAttendanceRecordUid,
                                                int attendanceStatus,
                                                UmCallback<Integer> callback);

    @UmQuery("SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            "where clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            "AND attendanceStatus = :attendanceStatus")
    public abstract int getAttedanceStatusCount(long clazzLogUid, int attendanceStatus);

}
