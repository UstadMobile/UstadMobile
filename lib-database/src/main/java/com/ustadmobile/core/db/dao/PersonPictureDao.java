package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@UmDao(hasAttachment = true)
public abstract class PersonPictureDao implements SyncableDao<PersonPicture, PersonPictureDao> {

    @UmDbSetAttachment
    public abstract void setAttachment(long uid, InputStream pictureAttachment) throws IOException;

    @UmDbGetAttachment
    public abstract String getAttachmentPath(long uid);

    @UmDbGetAttachment
    public abstract InputStream getAttachmentStream(long uid) throws IOException;

    @UmDbSetAttachment
    public abstract void setAttachmentFromTmpFile(long uid, File tmpFile);

}
