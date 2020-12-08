package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Report

@Dao
@Repository
abstract class ReportDao : BaseDao<Report> {

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<Report>

    @Query("SELECT * FROM REPORT WHERE NOT reportInactive AND reportOwnerUid = :loggedInPersonUid  ORDER BY reportTitle ASC")
    abstract fun findAllActiveReportByUserAsc(loggedInPersonUid: Long): DataSource.Factory<Int, Report>

    @Query("SELECT * FROM REPORT WHERE NOT reportInactive AND reportOwnerUid = :loggedInPersonUid ORDER BY reportTitle DESC")
    abstract fun findAllActiveReportByUserDesc(loggedInPersonUid: Long): DataSource.Factory<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract suspend fun findByUid(entityUid: Long): Report?

    @Update
    abstract suspend fun updateAsync(entity: Report)

    @Query("SELECT * From Report WHERE  reportUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Report?>

    @Query("Select * From Report")
    abstract fun findAllLive(): DoorLiveData<List<Report>>

    @Query("""UPDATE Report SET reportInactive = :inactive,
                reportLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
                WHERE reportUid = :uid""")
    abstract fun updateReportInactive(inactive: Boolean, uid: Long)


}