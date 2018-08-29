package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;

import java.util.ArrayList;
import java.util.List;

@UmDao
public abstract class ClazzLogAttendanceRecordDao implements BaseDao<ClazzLogAttendanceRecord>{

    @UmInsert
    public abstract long insert(ClazzLogAttendanceRecord entity);

    @UmInsert
    public abstract void insertAsync(ClazzLogAttendanceRecord entity, UmCallback<Long> result);

    @UmInsert
    public abstract void insertListAsync(List<ClazzLogAttendanceRecord> entities,
                                         UmCallback<long[]> callback);

    @UmQuery("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    public abstract ClazzLogAttendanceRecord findByUid(long uid);

    @UmQuery("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid")
    public abstract UmProvider<ClazzLogAttendanceRecord> findAttendanceLogsByClassLogId(long clazzLogUid);

    @UmQuery("SELECT ClazzMember.clazzMemberUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzId AND ClazzMember.clazzMemberClazzUid NOT IN " +
            "(SELECT clazzLogAttendanceRecordClazzMemberUid FROM ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid)")
    public abstract void findPersonUidsWithNoClazzAttendanceRecord(long clazzId, long clazzLogUid, UmCallback<List<Long>> callback);

    /**
     * Checks for ClazzMembers not in a particular Clazz that are not part of the
     * ClazzLogAttendanceRecord and creates their ClazzLogAttendanceRecords.
     *
     * @param clazzId
     * @param clazzLogUid
     * @param callback
     */
    public void insertAllAttendanceRecords(long clazzId, long clazzLogUid, UmCallback<long[]> callback) {
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

    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid")
    public abstract void updateAllByClazzLogUid(long clazzLogUid, int attendanceStatus,
                                                UmCallback<Integer> callback);


    @UmQuery("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus WHERE clazzLogAttendanceRecordUid = :clazzLogAttendanceRecordUid")
    public abstract void updateAttendanceStatus(long clazzLogAttendanceRecordUid, int attendanceStatus,
                                                UmCallback<Integer> callback);

    @UmQuery("SELECT COUNT(*) FROM ClazzLogAttendanceRecord where clazzLogAttendanceRecordClazzLogUid = :clazzLogUid AND attendanceStatus = :attendanceStatus")
    public abstract int getAttedanceStatusCount(long clazzLogUid, int attendanceStatus);

}
