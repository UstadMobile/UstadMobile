package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 14)
public class DiscussionPost {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long discussionPostUid;

    private long posterPersonUid;

    private String postContent;

    private String quotedContent;

    private long timePosted;

    private long clazzClazzUid;

    private long hasAttachments;

    @UmSyncLocalChangeSeqNum
    private long discussionPostLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long discussionPostMasterChangeSeqNum;

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

    public long getHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(long hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public long getDiscussionPostLocalChangeSeqNum() {
        return discussionPostLocalChangeSeqNum;
    }

    public void setDiscussionPostLocalChangeSeqNum(long discussionPostLocalChangeSeqNum) {
        this.discussionPostLocalChangeSeqNum = discussionPostLocalChangeSeqNum;
    }

    public long getDiscussionPostMasterChangeSeqNum() {
        return discussionPostMasterChangeSeqNum;
    }

    public void setDiscussionPostMasterChangeSeqNum(long discussionPostMasterChangeSeqNum) {
        this.discussionPostMasterChangeSeqNum = discussionPostMasterChangeSeqNum;
    }
}
