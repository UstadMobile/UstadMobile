package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 29)
public class Location {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long locationUid;

    private String title;

    private String desc;

    private String long;

    private String lat;

    private long parentLocationUid;

    @UmSyncLocalChangeSeqNum
    private long clazzMemberLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long clazzMemberMasterChangeSeqNum;


    public long getLocationUid() {
        return locationUid;
    }

    public void setLocationUid(long locationUid) {
        this.locationUid = locationUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public long getParentLocationUid() {
        return parentLocationUid;
    }

    public void setParentLocationUid(long parentLocationUid) {
        this.parentLocationUid = parentLocationUid;
    }

    public long getClazzMemberLocalChangeSeqNum() {
        return clazzMemberLocalChangeSeqNum;
    }

    public void setClazzMemberLocalChangeSeqNum(long clazzMemberLocalChangeSeqNum) {
        this.clazzMemberLocalChangeSeqNum = clazzMemberLocalChangeSeqNum;
    }

    public long getClazzMemberMasterChangeSeqNum() {
        return clazzMemberMasterChangeSeqNum;
    }

    public void setClazzMemberMasterChangeSeqNum(long clazzMemberMasterChangeSeqNum) {
        this.clazzMemberMasterChangeSeqNum = clazzMemberMasterChangeSeqNum;
    }
}
