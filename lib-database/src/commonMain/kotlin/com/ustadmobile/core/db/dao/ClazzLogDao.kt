package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes
import com.ustadmobile.lib.db.entities.Role
import kotlinx.serialization.Serializable


@UmDao(permissionJoin = "INNER JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid", 
        selectPermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzLogDao : BaseDao<ClazzLog> {

    @Insert
    abstract override fun insert(entity: ClazzLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzLog): Long

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzLog?>

    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE clazzLogClazzUid = :clazzUid
        AND clazzLog.clazzLogStatusFlag != :excludeStatus
        ORDER BY ClazzLog.logDate DESC""")
    abstract fun findByClazzUidAsFactory(clazzUid: Long, excludeStatus: Int): DataSource.Factory<Int, ClazzLog>


    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
    """)
    abstract fun findByClazzUidWithinTimeRange(clazzUid: Long, fromTime: Long, toTime: Long): List<ClazzLog>

    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:statusFilter = 0 OR ClazzLog.clazzLogStatusFlag = :statusFilter)
        ORDER BY ClazzLog.logDate
    """)
    abstract fun findByClazzUidWithinTimeRangeLive(clazzUid: Long, fromTime: Long, toTime: Long, statusFilter: Int): DoorLiveData<List<ClazzLog>>


    @Query("""UPDATE ClazzLog 
        SET clazzLogStatusFlag = :newStatus,
        clazzLogLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE clazzLogUid = :clazzLogUid""")
    abstract fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int)

}
