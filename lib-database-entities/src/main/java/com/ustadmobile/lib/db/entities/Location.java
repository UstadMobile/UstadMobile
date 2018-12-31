package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.Location.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class Location {

    public static final int TABLE_ID = 29;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long locationUid;

    private String title;

    private String description;

    private String lng;

    private String lat;

    private long parentLocationUid;

    @UmSyncLocalChangeSeqNum
    private long locationLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long locationMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int locationLastChangedBy;

    public Location() {

    }

    public Location(String title, String description) {
        this.title = title;
        this.description = description;
    }


    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public long getLocationLocalChangeSeqNum() {
        return locationLocalChangeSeqNum;
    }

    public void setLocationLocalChangeSeqNum(long locationLocalChangeSeqNum) {
        this.locationLocalChangeSeqNum = locationLocalChangeSeqNum;
    }

    public long getLocationMasterChangeSeqNum() {
        return locationMasterChangeSeqNum;
    }

    public void setLocationMasterChangeSeqNum(long locationMasterChangeSeqNum) {
        this.locationMasterChangeSeqNum = locationMasterChangeSeqNum;
    }

    public int getLocationLastChangedBy() {
        return locationLastChangedBy;
    }

    public void setLocationLastChangedBy(int locationLastChangedBy) {
        this.locationLastChangedBy = locationLastChangedBy;
    }
}
