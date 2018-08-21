package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class DiscussionPostWithPoster extends DiscussionPost {

    @UmEmbedded
    private Person poster;

    public Person getPoster() {
        return poster;
    }

    public void setPoster(Person poster) {
        this.poster = poster;
    }
}
