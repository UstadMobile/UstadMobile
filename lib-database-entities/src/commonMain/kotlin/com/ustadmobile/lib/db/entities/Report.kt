package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NONE
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

    var reportDateRangeSelection: Int = EVERYTHING

    var fromDate: Long = 0

    var fromRelTo: Int = 0

    var fromRelOffSet: Int = -0

    var fromRelUnit: Int = 0

    var toDate: Long = 0

    var toRelTo: Int = 0

    var toRelOffSet: Int = -0

    var toRelUnit: Int = 0

    var reportTitle: String? = null

    var reportDescription: String? = null

    var reportSeries: String? = null

    var reportInactive: Boolean = false

    var isTemplate: Boolean = false

    var priority: Int = 1

    var reportTitleId: Int = 0

    var reportDescId: Int = 0

    @MasterChangeSeqNum
    var reportMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var reportLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var reportLastChangedBy: Int = 0

    @LastChangedTime
    var reportLct: Long = 0

    companion object {

        const val TABLE_ID = 101

        const val DAY = 300

        const val WEEK = 301

        const val MONTH = 302

        const val CONTENT_ENTRY = 304

        const val GENDER = 306

        const val CLASS = 307

        const val ENROLMENT_OUTCOME = 308

        const val ENROLMENT_LEAVING_REASON = 309

        const val EVERYTHING = 0

        const val LAST_WEEK_DATE = 800

        const val LAST_TWO_WEEKS_DATE = 801

        const val LAST_MONTH_DATE = 802

        const val LAST_THREE_MONTHS_DATE = 803

        const val NEW_CUSTOM_RANGE_DATE = 804

        const val CUSTOM_RANGE = 805

        const val TEMPLATE_BLANK_REPORT_UID = 100000L

        private const val TEMPLATE_CONTENT_USAGE_OVER_TIME_UID = 100001L

        private const val TEMPLATE_UNIQUE_CONTENT_USERS_UID = 100002L

        private const val TEMPLATE_ATTENDANCE_OVER_TIME_BY_CLASS_UID = 100003L

        private const val TEMPLATE_CONTENT_USAGE_BY_CLASS_UID = 100004L

        private const val TEMPLATE_CONTENT_COMPLETION_UID = 100005L

        const val BLANK_REPORT = 1
        const val BLANK_REPORT_DESC = 2
        const val CONTENT_USAGE_OVER_TIME = 3
        const val CONTENT_USAGE_OVER_TIME_DESC = 4
        const val UNIQUE_CONTENT_USERS_OVER_TIME = 5
        const val UNIQUE_CONTENT_USERS_OVER_TIME_DESC = 6
        const val ATTENDANCE_OVER_TIME_BY_CLASS = 7
        const val ATTENDANCE_OVER_TIME_BY_CLASS_DESC = 8
        const val CONTENT_USAGE_BY_CLASS = 9
        const val CONTENT_USAGE_BY_CLASS_DESC = 10
        const val CONTENT_COMPLETION = 11
        const val CONTENT_COMPLETION_DESC = 12

        val FIXED_TEMPLATES = listOf(
                Report().apply {
                    reportUid = TEMPLATE_BLANK_REPORT_UID
                    reportTitle = "Blank report"
                    reportDescription = "Start "
                    isTemplate = true
                    priority = 0
                    reportTitleId = BLANK_REPORT
                    reportDescId = BLANK_REPORT_DESC
                    reportSeries = """                
                        [{
                          "reportSeriesUid": 0,
                          "reportSeriesName": "Series 1",
                          "reportSeriesYAxis": ${ReportSeries.TOTAL_DURATION},
                          "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                          "reportSeriesSubGroup": $NONE
                        }]
                    """.trimIndent()
                },
                Report().apply {
                    reportUid = TEMPLATE_CONTENT_USAGE_OVER_TIME_UID
                    reportTitle = "Content usage over time"
                    reportDescription = "Total content "
                    xAxis = GENDER
                    isTemplate = true
                    reportTitleId = CONTENT_USAGE_OVER_TIME
                    reportDescId = CONTENT_USAGE_OVER_TIME_DESC
                    reportSeries = """                
                        [{
                          "reportSeriesUid": 0,
                          "reportSeriesName": "Series 1",
                          "reportSeriesYAxis": ${ReportSeries.TOTAL_DURATION},
                          "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                          "reportSeriesSubGroup": $NONE
                        }]
                    """.trimIndent()
                },
                Report().apply {
                    reportUid = TEMPLATE_UNIQUE_CONTENT_USERS_UID
                    reportTitle = "Unique content users over time"
                    reportDescription = "Number of active users over time"
                    xAxis = MONTH
                    isTemplate = true
                    reportTitleId = UNIQUE_CONTENT_USERS_OVER_TIME
                    reportDescId = UNIQUE_CONTENT_USERS_OVER_TIME_DESC
                    reportSeries = """                
                        [{
                         "reportSeriesUid": 0,
                         "reportSeriesName": "Series 1",
                         "reportSeriesYAxis": ${ReportSeries.NUMBER_ACTIVE_USERS},
                         "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                         "reportSeriesSubGroup": $NONE
                        }]
                    """.trimIndent()
                },
                Report().apply {
                    reportUid = TEMPLATE_ATTENDANCE_OVER_TIME_BY_CLASS_UID
                    reportTitle = "Attendance over time by class"
                    reportDescription = "Percentage of students attending over time"
                    isTemplate = true
                    xAxis = CLASS
                    reportTitleId = ATTENDANCE_OVER_TIME_BY_CLASS
                    reportDescId = ATTENDANCE_OVER_TIME_BY_CLASS_DESC
                    reportSeries = """                
                        [{
                         "reportSeriesUid": 0,
                         "reportSeriesName": "Series 1",
                         "reportSeriesYAxis": ${ReportSeries.PERCENTAGE_STUDENTS_ATTENDED},
                          "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                          "reportSeriesSubGroup": $NONE
                        }]
                        """.trimIndent()
                },
                Report().apply {
                    reportUid = TEMPLATE_CONTENT_USAGE_BY_CLASS_UID
                    reportTitle = "Content usage by class"
                    reportDescription = "Total content usage duration subgroup by class"
                    xAxis = CLASS
                    isTemplate = true
                    reportTitleId = CONTENT_USAGE_BY_CLASS
                    reportDescId = CONTENT_USAGE_BY_CLASS_DESC
                    reportSeries = """
                        [{
                            "reportSeriesUid ": 0,
                            "reportSeriesName ": " Series 1",
                            "reportSeriesYAxis": ${ReportSeries.TOTAL_DURATION},
                            "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                            "reportSeriesSubGroup": $NONE
                        }]
                            """.trimIndent()
                },
                Report().apply {
                    reportUid = TEMPLATE_CONTENT_COMPLETION_UID
                    reportTitle = "Content completion"
                    reportDescription = "Number of students who have completed selected content"
                    isTemplate = true
                    xAxis = CONTENT_ENTRY
                    reportTitleId = CONTENT_COMPLETION
                    reportDescId = CONTENT_COMPLETION_DESC
                    reportSeries = """
                            [{
                                "reportSeriesUid": 0,
                                "reportSeriesName": "Series 1",
                                "reportSeriesYAxis": ${ReportSeries.NUMBER_OF_STUDENTS_COMPLETED_CONTENT},
                                "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                                "reportSeriesSubGroup": $NONE
                            }]
                            """.trimIndent()
                }
        )

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Report

        if (reportUid != other.reportUid) return false
        if (reportOwnerUid != other.reportOwnerUid) return false
        if (xAxis != other.xAxis) return false
        if (fromDate != other.fromDate) return false
        if (fromRelTo != other.fromRelTo) return false
        if (fromRelOffSet != other.fromRelOffSet) return false
        if (fromRelUnit != other.fromRelUnit) return false
        if (toDate != other.toDate) return false
        if (toRelTo != other.toRelTo) return false
        if (toRelOffSet != other.toRelOffSet) return false
        if (toRelUnit != other.toRelUnit) return false
        if (reportTitle != other.reportTitle) return false
        if (reportDescription != other.reportDescription) return false
        if (reportSeries != other.reportSeries) return false
        if (reportInactive != other.reportInactive) return false
        if (isTemplate != other.isTemplate) return false
        if (priority != other.priority) return false
        if (reportMasterChangeSeqNum != other.reportMasterChangeSeqNum) return false
        if (reportLocalChangeSeqNum != other.reportLocalChangeSeqNum) return false
        if (reportLastChangedBy != other.reportLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportUid.hashCode()
        result = 31 * result + reportOwnerUid.hashCode()
        result = 31 * result + xAxis
        result = 31 * result + fromDate.hashCode()
        result = 31 * result + fromRelTo
        result = 31 * result + fromRelOffSet
        result = 31 * result + fromRelUnit
        result = 31 * result + toDate.hashCode()
        result = 31 * result + toRelTo
        result = 31 * result + toRelOffSet
        result = 31 * result + toRelUnit
        result = 31 * result + (reportTitle?.hashCode() ?: 0)
        result = 31 * result + (reportDescription?.hashCode() ?: 0)
        result = 31 * result + (reportSeries?.hashCode() ?: 0)
        result = 31 * result + reportInactive.hashCode()
        result = 31 * result + isTemplate.hashCode()
        result = 31 * result + priority.hashCode()
        result = 31 * result + reportMasterChangeSeqNum.hashCode()
        result = 31 * result + reportLocalChangeSeqNum.hashCode()
        result = 31 * result + reportLastChangedBy
        return result
    }


}