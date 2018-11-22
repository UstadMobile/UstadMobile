package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.DiscussionPostAttachment;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class DiscussionAttachmentDao implements SyncableDao<DiscussionPostAttachment, DiscussionAttachmentDao> {

    @UmInsert
    public abstract void insertAsync(DiscussionPostAttachment attachment, UmCallback<Long> callback);

    @UmQuery("SELECT attachmentFileName FROM DiscussionPostAttachment WHERE discussionPostUid = :discussionPostUid")
    public abstract void findAttachmentByPostId(long discussionPostUid,UmCallback<String []> callback);
}
