package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class DiscussionPost {

    @UmPrimaryKey(autoIncrement = true)
    private long discussionPostUid;

    private long posterPersonUid;

    private String postContent;

    private long timePosted;

    private long clazzClazzUid;

    public long getDiscussionPostUid() {
        return discussionPostUid;
    }

    public void setDiscussionPostUid(long discussionPostUid) {
        this.discussionPostUid = discussionPostUid;
    }

    public long getPosterPersonUid() {
        return posterPersonUid;
    }

    public void setPosterPersonUid(long posterPersonUid) {
        this.posterPersonUid = posterPersonUid;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(long timePosted) {
        this.timePosted = timePosted;
    }

    public long getClazzClazzUid() {
        return clazzClazzUid;
    }

    public void setClazzClazzUid(long clazzClazzUid) {
        this.clazzClazzUid = clazzClazzUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscussionPost)) return false;

        DiscussionPost that = (DiscussionPost) o;

        if (discussionPostUid != that.discussionPostUid) return false;
        if (posterPersonUid != that.posterPersonUid) return false;
        if (timePosted != that.timePosted) return false;
        if (clazzClazzUid != that.clazzClazzUid) return false;
        return postContent != null ? postContent.equals(that.postContent) : that.postContent == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (discussionPostUid ^ (discussionPostUid >>> 32));
        result = 31 * result + (int) (posterPersonUid ^ (posterPersonUid >>> 32));
        result = 31 * result + (postContent != null ? postContent.hashCode() : 0);
        result = 31 * result + (int) (timePosted ^ (timePosted >>> 32));
        result = 31 * result + (int) (clazzClazzUid ^ (clazzClazzUid >>> 32));
        return result;
    }
}
