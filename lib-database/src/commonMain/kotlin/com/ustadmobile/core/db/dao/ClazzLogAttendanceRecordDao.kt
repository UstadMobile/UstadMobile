package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class ClazzLogAttendanceRecordDao : BaseDao<ClazzLogAttendanceRecord> {

    @Insert
    abstract suspend fun insertListAsync(entities: List<ClazzLogAttendanceRecord>)

    @Query("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLogAttendanceRecord?

    @Update
    abstract suspend fun updateListAsync(entities: List<ClazzLogAttendanceRecord>)


    @Query("""SELECT ClazzLogAttendanceRecord.*, Person.*
         FROM ClazzLogAttendanceRecord 
         LEFT JOIN Person ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid
         WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid""")
    abstract suspend fun findByClazzLogUid(clazzLogUid: Long): List<ClazzLogAttendanceRecordWithPerson>

    @Query("""UPDATE ClazzLogAttendanceRecord
        SET clazzLogAttendanceRecordClazzLogUid = :newClazzLogUid,
        clazzLogAttendanceRecordLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE clazzLogAttendanceRecordClazzLogUid = :oldClazzLogUid
    """)
    abstract fun updateRescheduledClazzLogUids(oldClazzLogUid: Long, newClazzLogUid: Long)

}
