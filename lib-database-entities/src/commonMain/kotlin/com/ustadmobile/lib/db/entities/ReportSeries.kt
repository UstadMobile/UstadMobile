package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ReportSeries.TABLE_ID,
        notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ReportSeries.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN ReportSeries ON ChangeLog.chTableId = ${ReportSeries.TABLE_ID} AND ChangeLog.chEntityPk = ReportSeries.reportSeriesUid
        JOIN Report ON ReportSeries.reportSeriesReportUid = Report.reportUid
        JOIN DeviceSession ON Report.reportOwnerUid = DeviceSession.dsPersonUid"""],
        syncFindAllQuery = """
        SELECT ReportSeries.* FROM
        ReportSeries
        JOIN Report ON ReportSeries.reportSeriesReportUid = Report.reportUid
        JOIN DeviceSession ON Report.reportOwnerUid = DeviceSession.dsPersonUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
class ReportSeries {

    @PrimaryKey(autoGenerate = true)
    var reportSeriesUid: Long = 0

    var reportSeriesReportUid: Long = 0

    var reportSeriesDataSet: Int = 0

    var reportSeriesVisualType: Int = 0

    var reportSeriesInactive: Boolean = false

    
    @MasterChangeSeqNum
    var reportSeriesMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var reportSeriesLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var reportSeriesLastChangedBy: Int = 0


    companion object {

        const val TABLE_ID = 103

    }

}