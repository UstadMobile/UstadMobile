package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaFollowerWithPerson extends WamdaFollower {

    @UmEmbedded
    private Person followerPerson;

    @UmEmbedded
    private WamdaPerson wamdaPerson;

    private boolean following;

    public Person getFollowerPerson() {
        return followerPerson;
    }

    public void setFollowerPerson(Person followerPerson) {
        this.followerPerson = followerPerson;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public WamdaPerson getWamdaPerson() {
        return wamdaPerson;
    }

    public void setWamdaPerson(WamdaPerson wamdaPerson) {
        this.wamdaPerson = wamdaPerson;
    }
}
