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

    private String locationDesc;

    private String lng;

    private String lat;

    private long parentLocationUid;

    private String timeZone;

    private boolean locationActive;

    public Location(){
        this.locationActive = true;
    }

    public boolean isLocationActive() {
        return locationActive;
    }

    public void setLocationActive(boolean locationActive) {
        this.locationActive = locationActive;
    }

    @UmSyncLocalChangeSeqNum
    private long locationLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long locationMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int locationLastChangedBy;

    public Location(String title, String description, String timeZone) {
        this.title = title;
        this.locationDesc = description;
        this.timeZone = timeZone;
        this.locationActive = true;
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

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        this.locationDesc = locationDesc;
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

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
