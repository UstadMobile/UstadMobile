package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.SetAttachmentData
import com.ustadmobile.lib.db.entities.PersonPicture


@Dao
@Repository
abstract class PersonPictureDao : BaseDao<PersonPicture> {

    @SetAttachmentData
    open fun setAttachment(entity: PersonPicture, filePath: String) {

    }

    @GetAttachmentData
    open fun getAttachmentPath(entity: PersonPicture): String? {
        return ""
    }

    @Query("""SELECT * FROM PersonPicture 
        WHERE personPicturePersonUid = :personUid
        AND CAST(personPictureActive AS INTEGER) = 1
        ORDER BY picTimestamp DESC LIMIT 1""")
    abstract suspend fun findByPersonUidAsync(personUid: Long): PersonPicture?

    @Query("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " + " picTimestamp DESC LIMIT 1")
    abstract fun findByPersonUidLive(personUid: Long): DoorLiveData<PersonPicture?>


    @Update
    abstract suspend fun updateAsync(personPicture: PersonPicture)

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