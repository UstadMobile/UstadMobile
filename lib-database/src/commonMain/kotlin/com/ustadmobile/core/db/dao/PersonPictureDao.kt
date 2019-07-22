package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.Role


@UmDao(hasAttachment = true, permissionJoin = " LEFT JOIN Person ON PersonPicture.personPicturePersonUid = Person.personUid ", selectPermissionCondition = PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_PERSON_PICTURE_SELECT + PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION2, updatePermissionCondition = PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_PERSON_PICTURE_UPDATE + PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION2)
@Dao
@UmRepository
abstract class PersonPictureDao : BaseDao<PersonPicture> {

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

    @Query("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " + " picTimestamp DESC LIMIT 1")
    abstract suspend fun findByPersonUidAsync(personUid: Long): PersonPicture?

    @Query("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " + " picTimestamp DESC LIMIT 1")
    abstract fun findByPersonUidLive(personUid: Long): DoorLiveData<PersonPicture?>


//    @UmDbSetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun uploadAttachment(uid: Long, attachment: InputStream)
//
//    @UmDbGetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun downloadAttachment(uid: Long): InputStream

    companion object {

        val TABLE_LEVEL_PERMISSION = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
                "OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                " WHERE " +
                " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND EntityRole.erTableId = " + PersonPicture.TABLE_ID +
                " AND Role.rolePermissions & "

        protected val TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)"
    }


}