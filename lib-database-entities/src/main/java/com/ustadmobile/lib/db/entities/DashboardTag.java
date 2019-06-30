package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 81)
public class DashboardTag {
    @UmPrimaryKey(autoGenerateSyncable = true)
    private long dashboardTagUid;

    private String dashboardTagTitle;

    private boolean dashboardTagActive;

    @UmSyncMasterChangeSeqNum
    private long dashboardTagMCSN;

    @UmSyncLocalChangeSeqNum
    private long dashboardTagLCSN;

    @UmSyncLastChangedBy
    private int dashboardTagLCB;

    public DashboardTag(String title){
        this.dashboardTagTitle = title;
        this.dashboardTagActive = true;
    }

    public DashboardTag(){

    }

    public long getDashboardTagUid() {
        return dashboardTagUid;
    }

    public void setDashboardTagUid(long dashboardTagUid) {
        this.dashboardTagUid = dashboardTagUid;
    }

    public String getDashboardTagTitle() {
        return dashboardTagTitle;
    }

    public void setDashboardTagTitle(String dashboardTagTitle) {
        this.dashboardTagTitle = dashboardTagTitle;
    }

    public boolean isDashboardTagActive() {
        return dashboardTagActive;
    }

    public void setDashboardTagActive(boolean dashboardTagActive) {
        this.dashboardTagActive = dashboardTagActive;
    }

    public long getDashboardTagMCSN() {
        return dashboardTagMCSN;
    }

    public void setDashboardTagMCSN(long dashboardTagMCSN) {
        this.dashboardTagMCSN = dashboardTagMCSN;
    }

    public long getDashboardTagLCSN() {
        return dashboardTagLCSN;
    }

    public void setDashboardTagLCSN(long dashboardTagLCSN) {
        this.dashboardTagLCSN = dashboardTagLCSN;
    }

    public int getDashboardTagLCB() {
        return dashboardTagLCB;
    }

    public void setDashboardTagLCB(int dashboardTagLCB) {
        this.dashboardTagLCB = dashboardTagLCB;
    }
}
