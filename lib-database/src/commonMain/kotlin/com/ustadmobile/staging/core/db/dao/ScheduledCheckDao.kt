package com.ustadmobile.core.db.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ScheduledCheck


@UmDao
@Dao
@UmRepository
abstract class ScheduledCheckDao : BaseDao<ScheduledCheck> {

    @Delete
    abstract fun deleteCheck(scheduledCheck: ScheduledCheck)

    @Query("SELECT ClazzLog.* FROM ClazzLog " +
            " WHERE NOT EXISTS(SELECT scClazzLogUid FROM ScheduledCheck WHERE " +
            " scClazzLogUid = ClazzLog.clazzLogUid AND ScheduledCheck.checkType = :checkType) " +
            " AND ClazzLog.logDate >= :fromDate")
    abstract fun findPendingLogsWithoutScheduledCheck(checkType: Int, fromDate: Long): List<ClazzLog>

    @Query("SELECT * FROM ScheduledCheck WHERE scheduledCheckUid = :uid")
    abstract fun findByUid(uid:Long):ScheduledCheck?
}
