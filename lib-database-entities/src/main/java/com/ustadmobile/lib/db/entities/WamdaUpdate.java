package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaUpdate {

    @UmPrimaryKey (autoIncrement = true)
    private long wamdUpdateUid;

    private long wamdaUpdatePersonUid;

    private String wamdaUpdateDesitination;

    private String wamdaUpdateText;

    private long timestamp;


    public long getWamdUpdateUid() {
        return wamdUpdateUid;
    }

    public void setWamdUpdateUid(long wamdUpdateUid) {
        this.wamdUpdateUid = wamdUpdateUid;
    }

    public long getWamdaUpdatePersonUid() {
        return wamdaUpdatePersonUid;
    }

    public void setWamdaUpdatePersonUid(long wamdaUpdatePersonUid) {
        this.wamdaUpdatePersonUid = wamdaUpdatePersonUid;
    }

    public String getWamdaUpdateDesitination() {
        return wamdaUpdateDesitination;
    }

    public void setWamdaUpdateDesitination(String wamdaUpdateDesitination) {
        this.wamdaUpdateDesitination = wamdaUpdateDesitination;
    }

    public String getWamdaUpdateText() {
        return wamdaUpdateText;
    }

    public void setWamdaUpdateText(String wamdaUpdateText) {
        this.wamdaUpdateText = wamdaUpdateText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
