package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum



@UmEntity
@Entity
class PersonCustomFieldValue() {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var personCustomFieldValueUid: Long = 0

    //The Custom field's uid
    val personCustomFieldValuePersonCustomFieldUid: Long = 0

    //The person associated uid
    val personCustomFieldValuePersonUid: Long = 0

    //The value itself
    val fieldValue: String? = null

    @UmSyncMasterChangeSeqNum
    val personCustomFieldValueMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    val personCustomFieldValueLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    val personCustomFieldValueLastChangedBy: Int = 0
}
