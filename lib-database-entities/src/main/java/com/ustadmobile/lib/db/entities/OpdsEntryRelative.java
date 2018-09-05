package com.ustadmobile.lib.db.entities;

/**
 * Created by mike on 3/24/18.
 */

public class OpdsEntryRelative {

    private String entryId;

    private String relativeEntryId;

    private int distance;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getRelativeEntryId() {
        return relativeEntryId;
    }

    public void setRelativeEntryId(String relativeEntryId) {
        this.relativeEntryId = relativeEntryId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
