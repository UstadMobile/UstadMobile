package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
@Entity
public class LocationAncestorJoin {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    private long locationAncestorId;

    private long locationAncestorChildLocationUid;

    private long locationAncestorAncestorLocationUid;

    public LocationAncestorJoin() {

    }

    public LocationAncestorJoin(long locationAncestorChildLocationUid, long locationAncestorAncestorLocationUid) {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid;
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid;
    }


    public long getLocationAncestorId() {
        return locationAncestorId;
    }

    public void setLocationAncestorId(long locationAncestorId) {
        this.locationAncestorId = locationAncestorId;
    }

    public long getLocationAncestorChildLocationUid() {
        return locationAncestorChildLocationUid;
    }

    public void setLocationAncestorChildLocationUid(long locationAncestorChildLocationUid) {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid;
    }

    public long getLocationAncestorAncestorLocationUid() {
        return locationAncestorAncestorLocationUid;
    }

    public void setLocationAncestorAncestorLocationUid(long locationAncestorAncestorLocationUid) {
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid;
    }
}
