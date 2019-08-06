package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzActivity
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle
import com.ustadmobile.lib.db.entities.DailyActivityNumbers
import com.ustadmobile.lib.db.entities.Role

@UmDao(permissionJoin = " JOIN Clazz ON ClazzActivity.clazzActivityClazzUid = Clazz.clazzUid ", 
        selectPermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzActivityDao : BaseDao<ClazzActivity> {

    @Insert
    abstract override fun insert(entity: ClazzActivity): Long

    @Update
    abstract override fun update(entity: ClazzActivity)

    @Insert
    abstract fun insertAsync(entity: ClazzActivity, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM ClazzActivity")
    abstract fun findAllClazzActivityChanges(): DataSource.Factory<Int, ClazzActivity>

    @Update
    abstract fun updateAsync(entity: ClazzActivity, resultObject: UmCallback<Int>)

    @Query("SELECT * FROM ClazzActivity where clazzActivityClazzUid = :clazzUid AND "
            + "clazzActivityDone = 1 ORDER BY clazzActivityLogDate DESC")
    abstract fun findByClazzUid(clazzUid: Long): DataSource.Factory<Int, ClazzActivity>

    @Query("SELECT ClazzActivityChange.clazzActivityChangeTitle AS changeTitle, ClazzActivity.* " +
            "FROM ClazzActivity " +
            "LEFT JOIN ClazzActivityChange " +
            "   ON ClazzActivity.clazzActivityClazzActivityChangeUid = ClazzActivityChange.clazzActivityChangeUid " +
            "WHERE clazzActivityClazzUid = :clazzUid " +
            "AND clazzActivityDone = 1 " +
            "ORDER BY clazzActivityLogDate DESC")
    abstract fun findWithChangeTitleByClazzUid(clazzUid: Long): DataSource.Factory<Int, ClazzActivityWithChangeTitle>

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    abstract fun findByUid(uid: Long): ClazzActivity

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<ClazzActivity>)

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityClazzUid = :clazzUid AND "
            + " clazzActivityLogDate = :logDate")
    abstract fun findByClazzAndDate(clazzUid: Long, logDate: Long): ClazzActivity

    @Query("SELECT * FROM ClazzActivity WHERE clazzActivityClazzUid = :clazzUid AND "
            + " clazzActivityLogDate = :logDate")
    abstract fun findByClazzAndDateAsync(clazzUid: Long, logDate: Long,
                                         resultObject: UmCallback<ClazzActivity>)


    @Query("SELECT  " +
            " COUNT(CASE WHEN ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as good, " +
            " COUNT(CASE WHEN NOT ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as bad, " +
            " (:clazzUid) as clazzUid, " +
            " ClazzActivity.clazzActivityLogDate as dayDate " +
            " FROM ClazzActivity " +
            " WHERE ClazzActivity.clazzActivityClazzUid = :clazzUid " +
            " AND ClazzActivity.clazzActivityLogDate > :fromDate " +
            " AND ClazzActivity.clazzActivityLogDate < :toDate " +
            " GROUP BY ClazzActivity.clazzActivityLogDate ")
    abstract fun getDailyAggregateFeedback(clazzUid: Long, fromDate: Long, toDate: Long,
                                           resultList: UmCallback<List<DailyActivityNumbers>>)

    @Query("SELECT  " +
            " COUNT(CASE WHEN ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as good, " +
            " COUNT(CASE WHEN NOT ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as bad, " +
            " (:clazzUid) as clazzUid, " +
            " ClazzActivity.clazzActivityLogDate as dayDate " +
            " FROM ClazzActivity " +
            " WHERE ClazzActivity.clazzActivityClazzUid = :clazzUid " +
            " AND ClazzActivity.clazzActivityLogDate > :fromDate " +
            " AND ClazzActivity.clazzActivityLogDate < :toDate " +
            " AND ClazzActivity.clazzActivityClazzActivityChangeUid = :activityChangeUid " +
            " GROUP BY ClazzActivity.clazzActivityLogDate ")
    abstract fun getDailyAggregateFeedbackByActivityChange(
            clazzUid: Long, fromDate: Long, toDate: Long, activityChangeUid: Long,
            resultList: UmCallback<List<DailyActivityNumbers>>)

    fun createClazzActivityForDate(currentClazzUid: Long, currentLogDate: Long,
                                   callback: UmCallback<Long>) {

        findByClazzAndDateAsync(currentClazzUid, currentLogDate, object : UmCallback<ClazzActivity> {
            override fun onSuccess(result: ClazzActivity?) {
                if (result != null) {
                    callback.onSuccess(result.clazzActivityUid)
                } else {
                    //Create one
                    val newClazzActivity = ClazzActivity()
                    newClazzActivity.clazzActivityLogDate = currentLogDate
                    newClazzActivity.isClazzActivityDone = false //should be set to true with done
                    newClazzActivity.clazzActivityClazzUid = currentClazzUid

                    insertAsync(newClazzActivity, object : UmCallback<Long> {
                        override fun onSuccess(result: Long?) {
                            callback.onSuccess(result)
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
                }
            }

            override fun onFailure(exception: Throwable?) {

            }
        })
    }


}
