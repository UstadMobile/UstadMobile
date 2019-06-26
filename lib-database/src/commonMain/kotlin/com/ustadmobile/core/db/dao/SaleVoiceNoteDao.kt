package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.SaleVoiceNote
import kotlinx.io.InputStream

@UmDao(hasAttachment = true, permissionJoin = " LEFT JOIN Sale ON SaleVoiceNote.saleVoiceNoteSaleUid = Sale.saleUid", selectPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleVoiceNoteDao : SyncableDao<SaleVoiceNote, SaleVoiceNoteDao> {

//    @UmDbSetAttachment
//    abstract fun setAttachment(uid: Long, pictureAttachment: InputStream)
//
//    @UmDbGetAttachment
//    abstract fun getAttachmentPath(uid: Long): String
//
//    @UmDbGetAttachment
//    abstract fun getAttachmentStream(uid: Long): InputStream
//
//    @UmDbSetAttachment
//    abstract fun setAttachmentFromTmpFile(uid: Long, tmpFilePath: String)
//
//    @Query("SELECT * FROM SaleVoiceNote where saleVoiceNoteSaleUid = :saleUid ORDER BY saleVoiceNoteTimestamp DESC LIMIT 1")
//    abstract fun findBySaleUidAsync(saleUid: Long, findByUidCallback: UmCallback<SaleVoiceNote>)
//
//    @UmDbSetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun uploadAttachment(uid: Long, attachment: InputStream)
//
//    @UmDbGetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun downloadAttachment(uid: Long):  InputStream

}
