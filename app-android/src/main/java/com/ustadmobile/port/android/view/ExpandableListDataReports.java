package com.ustadmobile.port.android.view;

import android.content.Context;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView;
import com.ustadmobile.core.view.ReportOverallAttendanceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataReports {


    Integer icon;
    String name;
    List<ExpandableListDataReports> children;
    String reportLink;

    ExpandableListDataReports(){

    }

    ExpandableListDataReports(String name, Integer icon, String reportLink){
        this.name = name;
        this.icon = icon;
        this.reportLink = reportLink;
    }

    public static HashMap<String, ExpandableListDataReports> getDataAll(Context context){
        HashMap<String,ExpandableListDataReports> expandableListDetail = new HashMap<>();

        List<ExpandableListDataReports> attendanceReport = new ArrayList<>();
        attendanceReport.add(new ExpandableListDataReports(
                context.getText(R.string.overall_attendance).toString(),
                -1, ReportOverallAttendanceView.VIEW_NAME ));
        attendanceReport.add(new ExpandableListDataReports(
                context.getText(R.string.attendance_grouped_by_threshold).toString(),
                -1, ""));
        attendanceReport.add(new ExpandableListDataReports(
                context.getText(R.string.at_risk_students).toString(),
                -1, ""));



        List<ExpandableListDataReports> operationsReport = new ArrayList<>();
        operationsReport.add(new ExpandableListDataReports(
                context.getText(R.string.number_of_days_classes_open).toString(),
                -1, ReportNumberOfDaysClassesOpenView.VIEW_NAME));

        List<ExpandableListDataReports> ircMasterListReport = new ArrayList<>();

        List<ExpandableListDataReports> selReport = new ArrayList<>();


        ExpandableListDataReports attendanceObj = new ExpandableListDataReports();
        attendanceObj.icon = R.drawable.ic_assignment_turned_in_bcd4_24dp;
        attendanceObj.name = context.getText(R.string.attendance_report).toString();
        attendanceObj.children = attendanceReport;

        ExpandableListDataReports operationsObj = new ExpandableListDataReports();
        operationsObj.icon = R.drawable.ic_account_balance_cyan_24dp;
        operationsObj.name = context.getText(R.string.attendance_report).toString();
        operationsObj.children = operationsReport;

        ExpandableListDataReports ircMasterObj = new ExpandableListDataReports();
        ircMasterObj.icon = R.drawable.ic_event_cyan_24dp;
        ircMasterObj.name = context.getText(R.string.irc_master_list_report).toString();
        ircMasterObj.children = ircMasterListReport;

        ExpandableListDataReports selObj = new ExpandableListDataReports();
        selObj.icon = R.drawable.ic_tag_faces_cyan_24dp;
        selObj.name = context.getText(R.string.sel_report).toString();
        selObj.children = selReport;


        expandableListDetail.put(context.getText(R.string.sel_report).toString(),
                selObj);
        expandableListDetail.put(context.getText(R.string.irc_master_list_report).toString(),
                ircMasterObj);
        expandableListDetail.put(context.getText(R.string.operations_report).toString(),
                operationsObj);
        expandableListDetail.put(context.getText(R.string.attendance_report).toString(),
                attendanceObj);


        return expandableListDetail;
    }

}
