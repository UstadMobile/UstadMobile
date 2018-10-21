package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class DiscussionPost {

    @UmPrimaryKey(autoIncrement = true)
    private long discussionPostUid;

    private long posterPersonUid;

    private String postContent;

    private String quotedContent;

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

    public String getQuotedContent() {
        return quotedContent;
    }

    public void setQuotedContent(String quotedContent) {
        this.quotedContent = quotedContent;
    }
}
