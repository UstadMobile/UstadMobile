package com.ustadmobile.staging.port.android.view

import android.content.Context
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.*
import java.util.*

/**
 * A POJO representing a Report item. It is also responsible for returning the default set of
 * reports and their child reports.
 */
class ExpandableListDataReports {


    var icon: Int? = null
    var name: String? = null
    lateinit var children: List<ExpandableListDataReports>
    var reportLink: String ? = null
    var showThreshold: Boolean? = null
    var showRadioGroup: Boolean? = null
    var showGenderDisaggregate: Boolean? = null
    var showClazzes: Boolean? = null
    var showLocations: Boolean? = null
    var desc: String? = null

    private constructor()

    private constructor(name: String, icon: Int?, reportLink: String, showThreshold: Boolean,
                        showRadioGroup: Boolean, showGenderDisaggregate: Boolean,
                        showClazzes: Boolean, showLocations: Boolean,
                        description: String) {
        this.name = name
        this.icon = icon
        this.reportLink = reportLink
        this.showThreshold = showThreshold
        this.showRadioGroup = showRadioGroup
        this.showGenderDisaggregate = showGenderDisaggregate
        this.desc = description
        this.showClazzes = showClazzes
        this.showLocations = showLocations

    }

    companion object {

        /**
         * Method that returns all reports as Title and ExapandableListdataReports map.
         *
         * @param context   The application context.
         * @return          The report map list
         */
        fun getDataAll(context: Context): HashMap<String, ExpandableListDataReports> {
            val expandableListDetail = HashMap<String, ExpandableListDataReports>()

            val attendanceReport = ArrayList<ExpandableListDataReports>()
            attendanceReport.add(ExpandableListDataReports(
                    context.getText(R.string.overall_attendance).toString(),
                    -1, ReportOverallAttendanceView.VIEW_NAME, false,
                    false, true, true, true,
                    context.getText(R.string.overall_attendance).toString()))
            attendanceReport.add(ExpandableListDataReports(
                    context.getText(R.string.attendance_grouped_by_threshold).toString(),
                    -1, ReportAttendanceGroupedByThresholdsView.VIEW_NAME, true,
                    true, true, true, true,
                    context.getText(R.string.attendance_grouped_by_threshold).toString()))
            attendanceReport.add(ExpandableListDataReports(
                    context.getText(R.string.at_risk_students).toString(),
                    -1, ReportAtRiskStudentsView.VIEW_NAME, false,
                    false, true, true, true,
                    context.getText(R.string.at_risk_report_desc).toString()))


            val operationsReport = ArrayList<ExpandableListDataReports>()
            operationsReport.add(ExpandableListDataReports(
                    context.getText(R.string.number_of_days_classes_open).toString(),
                    -1, ReportNumberOfDaysClassesOpenView.VIEW_NAME,
                    false, false, false, true, true,
                    context.getText(R.string.number_of_days_classes_open).toString()))

            val ircMasterListReport = ArrayList<ExpandableListDataReports>()

            val selReport = ArrayList<ExpandableListDataReports>()

            val attendanceObj = ExpandableListDataReports()
            attendanceObj.icon = R.drawable.ic_assignment_turned_in_bcd4_24dp
            attendanceObj.name = context.getText(R.string.attendance_report).toString()
            attendanceObj.children = attendanceReport
            attendanceObj.desc = attendanceObj.name
            attendanceObj.showClazzes = true
            attendanceObj.showLocations = true

            val operationsObj = ExpandableListDataReports()
            operationsObj.icon = R.drawable.ic_account_balance_cyan_24dp
            operationsObj.name = context.getText(R.string.attendance_report).toString()
            operationsObj.children = operationsReport
            operationsObj.desc = operationsObj.name
            operationsObj.showLocations = true
            operationsObj.showClazzes = true

            val ircMasterObj = ExpandableListDataReports()
            ircMasterObj.icon = R.drawable.ic_event_cyan_24dp
            ircMasterObj.name = context.getText(R.string.irc_master_list_report).toString()
            ircMasterObj.children = ircMasterListReport
            ircMasterObj.desc = ircMasterObj.name
            ircMasterObj.reportLink = ReportMasterView.VIEW_NAME
            ircMasterObj.showGenderDisaggregate = false
            ircMasterObj.showRadioGroup = false
            ircMasterObj.showThreshold = false
            ircMasterObj.showClazzes = true
            ircMasterObj.showLocations = true

            val selObj = ExpandableListDataReports()
            selObj.icon = R.drawable.ic_tag_faces_cyan_24dp
            selObj.name = context.getText(R.string.sel_report).toString()
            selObj.reportLink = ReportSELView.VIEW_NAME
            selObj.showGenderDisaggregate = false
            selObj.showRadioGroup = false
            selObj.showThreshold = false
            selObj.children = selReport
            selObj.desc = selObj.name
            selObj.showLocations = false
            selObj.showClazzes = true

            expandableListDetail[context.getText(R.string.sel_report).toString()] = selObj
            expandableListDetail[context.getText(R.string.irc_master_list_report).toString()] = ircMasterObj
            expandableListDetail[context.getText(R.string.operations_report).toString()] = operationsObj
            expandableListDetail[context.getText(R.string.attendance_report).toString()] = attendanceObj


            return expandableListDetail
        }
    }

}
