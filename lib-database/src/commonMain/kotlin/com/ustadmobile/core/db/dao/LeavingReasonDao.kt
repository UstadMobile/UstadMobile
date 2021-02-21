package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlin.js.JsName

@Repository
@Dao
abstract class LeavingReasonDao : BaseDao<LeavingReason> {

    @Query("""SELECT * FROM LeavingReason""")
    abstract fun findAllReasons(): DataSource.Factory<Int, LeavingReason>

    @JsName("findByUid")
    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): LeavingReason?

    @JsName("findByUidList")
    @Query("SELECT leavingReasonUid FROM LeavingReason WHERE leavingReasonUid IN (:uidList)")
    abstract fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<LeavingReason>)

    @Update
    abstract suspend fun updateAsync(entity: LeavingReason): Int

    fun initPreloadedLeavingReasons() {
        val uidsInserted = findByUidList(LeavingReason.FIXED_UIDS.values.toList())
        val uidsToInsert = LeavingReason.FIXED_UIDS.filter { it.value !in uidsInserted }
        val verbListToInsert = uidsToInsert.map { reason ->
            LeavingReason(reason.value, reason.key)
        }
        replaceList(verbListToInsert)
    }


}