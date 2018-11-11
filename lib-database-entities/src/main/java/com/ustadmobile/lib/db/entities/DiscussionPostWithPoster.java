package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class DiscussionPostWithPoster extends DiscussionPost {

    @UmEmbedded
    private Person person;

    @UmEmbedded
    private WamdaPerson wamdaPerson;

    private boolean liked;

    private boolean following;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public WamdaPerson getWamdaPerson() {
        return wamdaPerson;
    }

    public void setWamdaPerson(WamdaPerson wamdaPerson) {
        this.wamdaPerson = wamdaPerson;
    }
}
