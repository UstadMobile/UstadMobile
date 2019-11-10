package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.SetAttachmentData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.SaleVoiceNote

@UmDao(hasAttachment = true,
        permissionJoin = " LEFT JOIN Sale ON SaleVoiceNote.saleVoiceNoteSaleUid = Sale.saleUid",
        selectPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleVoiceNoteDao : BaseDao<SaleVoiceNote> {

    @SetAttachmentData
    open fun setAttachment(entity: SaleVoiceNote, filePath: String) {
        throw Exception(Exception("Shouldn't call the Dao, call the repo instead "))
    }

    @GetAttachmentData
    open fun getAttachmentPath(entity: SaleVoiceNote): String? {
        return ""
    }

    @Query("SELECT * FROM SaleVoiceNote where  saleVoiceNoteUid = :saleUid ORDER BY "
            + " saleVoiceNoteTimestamp DESC LIMIT 1")
    abstract suspend fun findByPersonUidAsync(saleUid: Long): SaleVoiceNote?


    @Query("SELECT * FROM SaleVoiceNote where  saleVoiceNoteSaleUid = :saleUid ORDER BY "
            + " saleVoiceNoteTimestamp DESC LIMIT 1")
    abstract suspend fun findBySaleUidAsync(saleUid: Long): SaleVoiceNote?

}
