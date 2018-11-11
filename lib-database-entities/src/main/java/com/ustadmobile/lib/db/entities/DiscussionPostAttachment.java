package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class DiscussionPostAttachment {

    @UmPrimaryKey (autoIncrement = true)
    private long attachmentUid;

    private String attachmentFileName;

    private long discussionPostUid;

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
}
