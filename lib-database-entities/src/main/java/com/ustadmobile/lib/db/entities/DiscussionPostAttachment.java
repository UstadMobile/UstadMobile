package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1001)
public class DiscussionPostAttachment {

    @UmPrimaryKey (autoIncrement = true)
    private long attachmentUid;

    private String attachmentFileName;

    private long discussionPostUid;

    @UmSyncLocalChangeSeqNum
    private long attachmentLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long attachmentMasterChangeSeqNum;

    public long getAttachmentUid() {
        return attachmentUid;
    }

    public void setAttachmentUid(long attachmentUid) {
        this.attachmentUid = attachmentUid;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public long getDiscussionPostUid() {
        return discussionPostUid;
    }

    public void setDiscussionPostUid(long discussionPostUid) {
        this.discussionPostUid = discussionPostUid;
    }

    public long getAttachmentLocalChangeSeqNum() {
        return attachmentLocalChangeSeqNum;
    }

    public void setAttachmentLocalChangeSeqNum(long attachmentLocalChangeSeqNum) {
        this.attachmentLocalChangeSeqNum = attachmentLocalChangeSeqNum;
    }

    public long getAttachmentMasterChangeSeqNum() {
        return attachmentMasterChangeSeqNum;
    }

    public void setAttachmentMasterChangeSeqNum(long attachmentMasterChangeSeqNum) {
        this.attachmentMasterChangeSeqNum = attachmentMasterChangeSeqNum;
    }
}
