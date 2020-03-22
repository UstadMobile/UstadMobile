package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzActivity
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle
import com.ustadmobile.lib.db.entities.DailyActivityNumbers
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE

@UmDao(permissionJoin = " JOIN Clazz ON ClazzActivity.clazzActivityClazzUid = Clazz.clazzUid ", 
        selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 +
            PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT + ENTITY_LEVEL_PERMISSION_CONDITION2,
        updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 +
            PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE + ENTITY_LEVEL_PERMISSION_CONDITION2,
        insertPermissionCondition = TABLE_LEVEL_PERMISSION_CONDITION1 +
            PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT + TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzActivityDao : BaseDao<ClazzActivity> {

    @Insert
    abstract override fun insert(entity: ClazzActivity): Long

    @Update
    abstract override fun update(entity: ClazzActivity)

    @Update
    abstract suspend fun updateAsync(entity: ClazzActivity): Int

    @Query("SELECT ClazzActivityChange.clazzActivityChangeTitle AS changeTitle, ClazzActivity.* " +
            "FROM ClazzActivity " +
            "LEFT JOIN ClazzActivityChange " +
            "   ON ClazzActivity.clazzActivityClazzActivityChangeUid = ClazzActivityChange.clazzActivityChangeUid " +
            "WHERE clazzActivityClazzUid = :clazzUid " +
            "AND CAST(clazzActivityDone AS INTEGER)  = 1 " +
            "ORDER BY clazzActivityLogDate DESC")
    abstract fun findWithChangeTitleByClazzUid(clazzUid: Long): DataSource.Factory<Int, ClazzActivityWithChangeTitle>

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    abstract fun findByUid(uid: Long): ClazzActivity?

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzActivity?

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityClazzUid = :clazzUid AND "
            + " clazzActivityLogDate = :logDate")
    abstract suspend fun findByClazzAndDateAsync(clazzUid: Long, logDate: Long): ClazzActivity?

    @Query("SELECT  " +
            " COUNT(CASE WHEN ClazzActivity.isClazzActivityGoodFeedback THEN 1 END) as good, " +
            " COUNT(CASE WHEN NOT ClazzActivity.isClazzActivityGoodFeedback THEN 1 END) as bad, " +
            " (:clazzUid) as clazzUid, " +
            " ClazzActivity.clazzActivityLogDate as dayDate " +
            " FROM ClazzActivity " +
            " WHERE ClazzActivity.clazzActivityClazzUid = :clazzUid " +
            " AND ClazzActivity.clazzActivityLogDate > :fromDate " +
            " AND ClazzActivity.clazzActivityLogDate < :toDate " +
            " AND ClazzActivity.clazzActivityClazzActivityChangeUid = :activityChangeUid " +
            " GROUP BY ClazzActivity.clazzActivityLogDate ")
    abstract suspend fun getDailyAggregateFeedbackByActivityChange(
            clazzUid: Long, fromDate: Long, toDate: Long, activityChangeUid: Long)
                : List<DailyActivityNumbers>

    suspend fun createClazzActivityForDate(currentClazzUid: Long, currentLogDate: Long) : Long {

        val result = findByClazzAndDateAsync(currentClazzUid, currentLogDate)
        if (result != null) {
            return (result.clazzActivityUid)
        } else {
            //Create one
            val newClazzActivity = ClazzActivity()
            newClazzActivity.clazzActivityLogDate = currentLogDate
            newClazzActivity.clazzActivityDone = false //should be set to true with done
            newClazzActivity.clazzActivityClazzUid = currentClazzUid

            val res = insertAsync(newClazzActivity)
            if(res!= null){
                return res
            }else{
                return 0
            }
        }

    }


}
