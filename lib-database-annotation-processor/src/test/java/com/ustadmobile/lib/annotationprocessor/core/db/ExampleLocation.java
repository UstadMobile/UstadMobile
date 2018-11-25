package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ExampleLocation {

    @UmPrimaryKey(autoIncrement = true)
    private int locationUid;

    private long latitude;

    private long longitutde;

    public int getLocationUid() {
        return locationUid;
    }

    public void setLocationUid(int locationUid) {
        this.locationUid = locationUid;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitutde() {
        return longitutde;
    }

    public void setLongitutde(long longitutde) {
        this.longitutde = longitutde;
    }
}
