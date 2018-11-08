package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DiscussionPostAttachment;

@UmDao
public abstract class DiscussionAttachmentDao {

    @UmInsert
    public abstract void insertAsync(DiscussionPostAttachment attachment, UmCallback<Long> callback);

    @UmQuery("SELECT attachmentFileName FROM DiscussionPostAttachment WHERE discussionPostUid = :discussionPostUid")
    public abstract void findAttachmentByPostId(long discussionPostUid,UmCallback<String []> callback);
}
