package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.SaleVoiceNote;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@UmDao(hasAttachment = true,
        permissionJoin = " LEFT JOIN Sale ON SaleVoiceNote.saleVoiceNoteSaleUid = Sale.saleUid",
        selectPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class SaleVoiceNoteDao implements SyncableDao<SaleVoiceNote, SaleVoiceNoteDao> {

    @UmDbSetAttachment
    public abstract void setAttachment(long uid, InputStream pictureAttachment) throws IOException;

    @UmDbGetAttachment
    public abstract String getAttachmentPath(long uid);

    @UmDbGetAttachment
    public abstract InputStream getAttachmentStream(long uid) throws IOException;

    @UmDbSetAttachment
    public abstract void setAttachmentFromTmpFile(long uid, File tmpFile);

    @UmQuery("SELECT * FROM SaleVoiceNote where saleVoiceNoteSaleUid = :saleUid ORDER BY saleVoiceNoteTimestamp DESC LIMIT 1")
    public abstract void findBySaleUidAsync(long saleUid, UmCallback<SaleVoiceNote> findByUidCallback);

    @UmDbSetAttachment
    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public abstract void uploadAttachment(long uid, InputStream attachment) throws IOException;

    @UmDbGetAttachment
    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public abstract InputStream downloadAttachment(long uid) throws IOException;

}
