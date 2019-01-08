package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;

import java.io.InputStream;

@UmDao(hasAttachment = true)
public abstract class ExampleSyncableEntityWithAttachmentDao {

    public abstract void setAttachment(long uid, InputStream attachment);

    public abstract String getAttachmentUri(long uid);

    @UmRestAccessible
    public abstract InputStream serveAttachment(long uid,
                                                @UmRestAuthorizedUidParam long accountUid);

}
