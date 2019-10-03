package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 178)
@Entity
@Serializable
class PersonCustomFieldValue() {

    @PrimaryKey(autoGenerate = true)
    var personCustomFieldValueUid: Long = 0

    //The Custom field's uid
    var personCustomFieldValuePersonCustomFieldUid: Long = 0

    //The person associated uid
    var personCustomFieldValuePersonUid: Long = 0

    //The value itself
    var fieldValue: String? = null

    @MasterChangeSeqNum
    var personCustomFieldValueMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personCustomFieldValueLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personCustomFieldValueLastChangedBy: Int = 0
}
