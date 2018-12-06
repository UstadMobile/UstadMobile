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
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.ArrayList;
import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
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


    @UmQuery("SELECT ClazzLogAttendanceRecord.* , Person.* " +
            " FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzMember " +
            " on ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            "AND ClazzMember.role = 1")
    public abstract UmProvider<ClazzLogAttendanceRecordWithPerson> findAttendanceRecordsWithPersonByClassLogId (long clazzLogUid);

    @UmQuery("SELECT ClazzMember.clazzMemberUid FROM ClazzMember WHERE " +
            " ClazzMember.clazzMemberClazzUid = :clazzId " +
            " AND ClazzMember.clazzMemberActive = 1 " +
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
            " AND ClazzLog.clazzClazzUid = :clazzUid " +
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
            " ClazzLog.clazzClazzUid as clazzUid, " +
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

    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid")
    public abstract void updateAllByClazzLogUid(long clazzLogUid, int attendanceStatus,
                                                UmCallback<Integer> callback);


    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordUid = :clazzLogAttendanceRecordUid")
    public abstract void updateAttendanceStatus(long clazzLogAttendanceRecordUid,
                                                int attendanceStatus,
                                                UmCallback<Integer> callback);

    @UmQuery("SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            "where clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            "AND attendanceStatus = :attendanceStatus")
    public abstract int getAttedanceStatusCount(long clazzLogUid, int attendanceStatus);

}
