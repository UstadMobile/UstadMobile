package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaFollowerWithPerson extends WamdaFollower {

    @UmEmbedded
    private Person followerPerson;

    public Person getFollowerPerson() {
        return followerPerson;
    }

    public void setFollowerPerson(Person followerPerson) {
        this.followerPerson = followerPerson;
    }
}
