package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = Report.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId as deviceId, ${Report.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN Report ON ChangeLog.chTableId = ${Report.TABLE_ID} AND ChangeLog.chEntityPk = Report.reportUid
        JOIN DeviceSession ON Report.reportOwnerUid = DeviceSession.dsPersonUid"""],
    syncFindAllQuery = """
        SELECT Report.* FROM
        Report
        JOIN DeviceSession ON Report.reportOwnerUid = DeviceSession.dsPersonUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """
)
@Serializable
open class Report {

    @PrimaryKey(autoGenerate = true)
    var reportUid: Long = 0

    var reportOwnerUid: Long = 0

    var xAxis: Int = DAY

    var fromDate: Long = 0

    var toDate: Long = 0

    var reportTitle: String? = null

    var reportSeries: String? = null

    var reportInactive: Boolean = false

    @MasterChangeSeqNum
    var reportMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var reportLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var reportLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 101

        // TODO: moves to series
        const val BAR_CHART = 100

        // TODO: moves to series
        const val LINE_GRAPH = 101

        val listOfGraphs = arrayOf(BAR_CHART, LINE_GRAPH)

        const val SCORE = 200

        const val DURATION = 201

        const val AVG_DURATION = 202

        const val COUNT_ACTIVITIES = 203

        val yAxisList = arrayOf(SCORE, DURATION, AVG_DURATION, COUNT_ACTIVITIES)*/

        const val DAY = 300

        const val WEEK = 301

        const val MONTH = 302

        const val CONTENT_ENTRY = 304

        //TODO to be put back when varuna merges his branch
        // private const val LOCATION = MessageID.xapi_location

        const val GENDER = 306

        const val CLASS = 307

        val xAxisList = arrayOf(DAY, WEEK, MONTH, CONTENT_ENTRY, /*LOCATION, */ GENDER, CLASS)


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Report

        if (reportUid != other.reportUid) return false
        if (reportOwnerUid != other.reportOwnerUid) return false
        if (chartType != other.chartType) return false
        if (xAxis != other.xAxis) return false
        if (yAxis != other.yAxis) return false
        if (subGroup != other.subGroup) return false
        if (fromDate != other.fromDate) return false
        if (toDate != other.toDate) return false
        if (reportTitle != other.reportTitle) return false
        if (reportInactive != other.reportInactive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportUid.hashCode()
        result = 31 * result + reportOwnerUid.hashCode()
        result = 31 * result + chartType
        result = 31 * result + xAxis
        result = 31 * result + yAxis
        result = 31 * result + subGroup
        result = 31 * result + fromDate.hashCode()
        result = 31 * result + toDate.hashCode()
        result = 31 * result + (reportTitle?.hashCode() ?: 0)
        result = 31 * result + reportInactive.hashCode()
        return result
    }

}