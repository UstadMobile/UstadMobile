package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.*

@Dao
abstract class ClazzLogAttendanceRecordDao : BaseDao<ClazzLogAttendanceRecord> {

    @Insert
    abstract override fun insert(entity: ClazzLogAttendanceRecord): Long

    @Insert
    abstract suspend fun insertListAsync(entities: List<ClazzLogAttendanceRecord>): Array<Long>

    @Query("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLogAttendanceRecord?

    @Update
    abstract suspend fun updateListAsync(entities: List<ClazzLogAttendanceRecord>)


    @Query("""SELECT ClazzLogAttendanceRecord.*, Person.*
         FROM ClazzLogAttendanceRecord 
         LEFT JOIN ClazzMember ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid
         LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
         WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid""")
    abstract fun findByClazzLogUid(clazzLogUid: Long): List<ClazzLogAttendanceRecordWithPerson>

    @Query("""UPDATE ClazzLogAttendanceRecord
        SET clazzLogAttendanceRecordClazzLogUid = :newClazzLogUid,
        clazzLogAttendanceRecordLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE clazzLogAttendanceRecordClazzLogUid = :oldClazzLogUid
    """)
    abstract fun updateRescheduledClazzLogUids(oldClazzLogUid: Long, newClazzLogUid: Long)

}
