package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao(permissionJoin = "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " 
        + "LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid ", 
        selectPermissionCondition = ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
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
