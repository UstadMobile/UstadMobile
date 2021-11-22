package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UidAndLabel
import kotlin.js.JsName

@Repository
@Dao
abstract class LeavingReasonDao : BaseDao<LeavingReason> {

    @Query("""SELECT * FROM LeavingReason""")
    abstract fun findAllReasons(): DoorDataSourceFactory<Int, LeavingReason>

    @Query("SELECT * FROM LeavingReason")
    abstract fun findAllReasonsLive(): List<LeavingReason>

    @JsName("findByUid")
    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): LeavingReason?

    @JsName("findByUidList")
    @Query("SELECT leavingReasonUid FROM LeavingReason WHERE leavingReasonUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("findByUidLive")
    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<LeavingReason?>

    @JsName("getReasonsFromUids")
    @Query("""SELECT LeavingReason.leavingReasonUid AS uid, 
            LeavingReason.leavingReasonTitle As labelName  
            FROM LeavingReason WHERE leavingReasonUid IN (:uidList)""")
    abstract suspend fun getReasonsFromUids(uidList: List<Long>): List<UidAndLabel>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<LeavingReason>)

    @Update
    abstract suspend fun updateAsync(entity: LeavingReason): Int

    suspend fun initPreloadedLeavingReasons() {
        val uidsInserted = findByUidList(LeavingReason.FIXED_UIDS.values.toList())
        val uidsToInsert = LeavingReason.FIXED_UIDS.filter { it.value !in uidsInserted }
        val verbListToInsert = uidsToInsert.map { reason ->
            LeavingReason(reason.value, reason.key)
        }
        replaceList(verbListToInsert)
    }


}