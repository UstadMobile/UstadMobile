package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaPersonWithTotalFollowers extends Person {

    private int totalNumFollowers;

    private int totalNumFollowing;

    @UmEmbedded
    private WamdaPerson wamdaPerson;

    public int getTotalNumFollowers() {
        return totalNumFollowers;
    }

    public void setTotalNumFollowers(int totalNumFollowers) {
        this.totalNumFollowers = totalNumFollowers;
    }

    public int getTotalNumFollowing() {
        return totalNumFollowing;
    }

    public void setTotalNumFollowing(int totalNumFollowing) {
        this.totalNumFollowing = totalNumFollowing;
    }

    public WamdaPerson getWamdaPerson() {
        return wamdaPerson;
    }

    public void setWamdaPerson(WamdaPerson wamdaPerson) {
        this.wamdaPerson = wamdaPerson;
    }
}
