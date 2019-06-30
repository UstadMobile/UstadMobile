package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 80)
public class DashboardEntry {

    public static final int REPORT_CHART_TYPE_BAR_CHART = 1;
    public static final int REPORT_CHART_TYPE_TABLE = 2;

    public static final int REPORT_TYPE_SALES_PERFORMANCE = 3;
    public static final int REPORT_TYPE_SALES_LOG = 4;
    public static final int REPORT_TYPE_TOP_LES = 5;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long dashboardEntryUid;

    private long dashboardEntryPersonUid;

    private String dashboardEntryTitle;

    private String dashboardEntryReportParam;

    private int dashboardEntryIndex;

    private int dashboardEntryReportType;

    private boolean dashboardEntryActive;

    @UmSyncMasterChangeSeqNum
    private long dashboardEntryMCSN;

    @UmSyncLocalChangeSeqNum
    private long dashboardEntryLCSN;

    @UmSyncLastChangedBy
    private int dashboardEntryLCB;

    public DashboardEntry(){
        this.dashboardEntryActive = false;
    }

    public DashboardEntry(String title, int reportType, long personUid){
        this.dashboardEntryTitle = title;
        this.dashboardEntryReportType = reportType;
        this.dashboardEntryPersonUid = personUid;
        this.dashboardEntryActive = true;
        this.dashboardEntryIndex = 42;
    }

    public long getDashboardEntryUid() {
        return dashboardEntryUid;
    }

    public void setDashboardEntryUid(long dashboardEntryUid) {
        this.dashboardEntryUid = dashboardEntryUid;
    }

    public long getDashboardEntryPersonUid() {
        return dashboardEntryPersonUid;
    }

    public void setDashboardEntryPersonUid(long dashboardEntryPersonUid) {
        this.dashboardEntryPersonUid = dashboardEntryPersonUid;
    }

    public String getDashboardEntryTitle() {
        return dashboardEntryTitle;
    }

    public void setDashboardEntryTitle(String dashboardEntryTitle) {
        this.dashboardEntryTitle = dashboardEntryTitle;
    }

    public String getDashboardEntryReportParam() {
        return dashboardEntryReportParam;
    }

    public void setDashboardEntryReportParam(String dashboardEntryReportParam) {
        this.dashboardEntryReportParam = dashboardEntryReportParam;
    }

    public int getDashboardEntryReportType() {
        return dashboardEntryReportType;
    }

    public void setDashboardEntryReportType(int dashboardEntryReportType) {
        this.dashboardEntryReportType = dashboardEntryReportType;
    }

    public boolean isDashboardEntryActive() {
        return dashboardEntryActive;
    }

    public void setDashboardEntryActive(boolean dashboardEntryActive) {
        this.dashboardEntryActive = dashboardEntryActive;
    }

    public long getDashboardEntryMCSN() {
        return dashboardEntryMCSN;
    }

    public void setDashboardEntryMCSN(long dashboardEntryMCSN) {
        this.dashboardEntryMCSN = dashboardEntryMCSN;
    }

    public long getDashboardEntryLCSN() {
        return dashboardEntryLCSN;
    }

    public void setDashboardEntryLCSN(long dashboardEntryLCSN) {
        this.dashboardEntryLCSN = dashboardEntryLCSN;
    }

    public int getDashboardEntryLCB() {
        return dashboardEntryLCB;
    }

    public void setDashboardEntryLCB(int dashboardEntryLCB) {
        this.dashboardEntryLCB = dashboardEntryLCB;
    }

    public int getDashboardEntryIndex() {
        return dashboardEntryIndex;
    }

    public void setDashboardEntryIndex(int dashboardEntryIndex) {
        this.dashboardEntryIndex = dashboardEntryIndex;
    }
}
