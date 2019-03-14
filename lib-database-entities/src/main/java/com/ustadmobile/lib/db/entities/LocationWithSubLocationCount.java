package com.ustadmobile.lib.db.entities;

public class LocationWithSubLocationCount extends Location {

    int subLocations;

    public int getSubLocations() {
        return subLocations;
    }

    public void setSubLocations(int subLocations) {
        this.subLocations = subLocations;
    }
}
