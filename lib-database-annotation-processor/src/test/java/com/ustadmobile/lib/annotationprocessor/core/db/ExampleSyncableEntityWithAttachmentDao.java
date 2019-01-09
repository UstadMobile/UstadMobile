package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.io.IOException;
import java.io.InputStream;

@UmDao(hasAttachment = true)
public abstract class ExampleSyncableEntityWithAttachmentDao implements
        SyncableDao<ExampleSyncableEntityWithAttachment, ExampleSyncableEntityWithAttachmentDao> {

    @UmDbSetAttachment
    public abstract void setAttachment(long uid, InputStream attachment) throws IOException;

    @UmDbGetAttachment
    public abstract String getAttachmentUri(long uid);

    @UmDbGetAttachment
    public abstract InputStream getAttachmentStream(long uid) throws IOException;

}
