package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 82)
public class DashboardEntryTag {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long dashboardEntryTagUid;

    private long dashboardEntryTagDashboardEntryUid;

    private long dashboardEntryTagDashboardTagUid;

    private boolean dashboardEntryTagActive;

    @UmSyncMasterChangeSeqNum
    private long dashboardEntryTagMCSN;

    @UmSyncLocalChangeSeqNum
    private long dashboardEntryTagLCSN;

    @UmSyncLastChangedBy
    private int dashboardEntryTagLCB;

    public DashboardEntryTag(){
        dashboardEntryTagActive = true;
    }

    public DashboardEntryTag(long entryUid, long tagUid){
        dashboardEntryTagActive = true;
        this.dashboardEntryTagDashboardEntryUid = entryUid;
        this.dashboardEntryTagDashboardTagUid = tagUid;
    }

    public long getDashboardEntryTagUid() {
        return dashboardEntryTagUid;
    }

    public void setDashboardEntryTagUid(long dashboardEntryTagUid) {
        this.dashboardEntryTagUid = dashboardEntryTagUid;
    }

    public long getDashboardEntryTagDashboardEntryUid() {
        return dashboardEntryTagDashboardEntryUid;
    }

    public void setDashboardEntryTagDashboardEntryUid(long dashboardEntryTagDashboardEntryUid) {
        this.dashboardEntryTagDashboardEntryUid = dashboardEntryTagDashboardEntryUid;
    }

    public long getDashboardEntryTagDashboardTagUid() {
        return dashboardEntryTagDashboardTagUid;
    }

    public void setDashboardEntryTagDashboardTagUid(long dashboardEntryTagDashboardTagUid) {
        this.dashboardEntryTagDashboardTagUid = dashboardEntryTagDashboardTagUid;
    }

    public boolean isDashboardEntryTagActive() {
        return dashboardEntryTagActive;
    }

    public void setDashboardEntryTagActive(boolean dashboardEntryTagActive) {
        this.dashboardEntryTagActive = dashboardEntryTagActive;
    }

    public long getDashboardEntryTagMCSN() {
        return dashboardEntryTagMCSN;
    }

    public void setDashboardEntryTagMCSN(long dashboardEntryTagMCSN) {
        this.dashboardEntryTagMCSN = dashboardEntryTagMCSN;
    }

    public long getDashboardEntryTagLCSN() {
        return dashboardEntryTagLCSN;
    }

    public void setDashboardEntryTagLCSN(long dashboardEntryTagLCSN) {
        this.dashboardEntryTagLCSN = dashboardEntryTagLCSN;
    }

    public int getDashboardEntryTagLCB() {
        return dashboardEntryTagLCB;
    }

    public void setDashboardEntryTagLCB(int dashboardEntryTagLCB) {
        this.dashboardEntryTagLCB = dashboardEntryTagLCB;
    }
}
