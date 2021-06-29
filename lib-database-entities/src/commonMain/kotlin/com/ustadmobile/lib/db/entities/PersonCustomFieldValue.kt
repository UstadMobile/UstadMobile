package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = PersonCustomFieldValue.TABLE_ID)
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

    @LastChangedTime
    var personCustomFieldValueLct: Long = 0

    companion object {
        const val TABLE_ID = 178
    }
}
