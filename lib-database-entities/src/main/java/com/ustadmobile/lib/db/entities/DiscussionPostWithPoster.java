package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class DiscussionPostWithPoster extends DiscussionPost {

    @UmEmbedded
    private Person poster;

    private boolean liked;

    public Person getPoster() {
        return poster;
    }

    public void setPoster(Person poster) {
        this.poster = poster;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscussionPostWithPoster)) return false;

        DiscussionPostWithPoster that = (DiscussionPostWithPoster) o;

        if (liked != that.liked) return false;
        return poster != null ? poster.equals(that.poster) : that.poster == null;
    }

    @Override
    public int hashCode() {
        int result = poster != null ? poster.hashCode() : 0;
        result = 31 * result + (liked ? 1 : 0);
        return result;
    }
}
