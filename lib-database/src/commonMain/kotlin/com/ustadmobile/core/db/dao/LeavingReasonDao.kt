package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class LeavingReasonDao : BaseDao<LeavingReason> {

    @Query("""SELECT * FROM LeavingReason""")
    abstract fun findAllReasons(): PagingSource<Int, LeavingReason>

    @Query("SELECT * FROM LeavingReason")
    abstract fun findAllReasonsLive(): List<LeavingReason>

    @Query("SELECT * FROM LeavingReason")
    abstract suspend fun findAllReasonsAsync(): List<LeavingReason>

    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): LeavingReason?

    @Query("SELECT leavingReasonUid FROM LeavingReason WHERE leavingReasonUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>

    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<LeavingReason?>

    @Query("""SELECT LeavingReason.leavingReasonUid AS uid, 
            LeavingReason.leavingReasonTitle As labelName  
            FROM LeavingReason WHERE leavingReasonUid IN (:uidList)""")
    abstract suspend fun getReasonsFromUids(uidList: List<Long>): List<UidAndLabel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<LeavingReason>)

    @Update
    abstract suspend fun updateAsync(entity: LeavingReason): Int


}