package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class DiscussionMessage implements SyncableEntity{

    @UmPrimaryKey
    private long discussionMessageUid;

    private long posterPersonUid;

    private String message;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    public long getPosterPersonUid() {
        return posterPersonUid;
    }

    public void setPosterPersonUid(long posterPersonUid) {
        this.posterPersonUid = posterPersonUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }

    @Override
    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
    }

    public long getDiscussionMessageUid() {
        return discussionMessageUid;
    }

    public void setDiscussionMessageUid(long discussionMessageUid) {
        this.discussionMessageUid = discussionMessageUid;
    }
}
