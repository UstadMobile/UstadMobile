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

    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WamdaFollowerWithPerson)) return false;
        if (!super.equals(object)) return false;

        WamdaFollowerWithPerson person = (WamdaFollowerWithPerson) object;

        if (following != person.following) return false;
        if (followerPerson != null ? !followerPerson.equals(person.followerPerson) : person.followerPerson != null)
            return false;
        if (wamdaPerson != null ? !wamdaPerson.equals(person.wamdaPerson) : person.wamdaPerson != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (followerPerson != null ? followerPerson.hashCode() : 0);
        result = 31 * result + (wamdaPerson != null ? wamdaPerson.hashCode() : 0);
        result = 31 * result + (following ? 1 : 0);
        return result;
    }
}
