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
    private val personCustomFieldValuePersonCustomFieldUid: Long = 0

    //The person associated uid
    private val personCustomFieldValuePersonUid: Long = 0

    //The value itself
    private val fieldValue: String? = null

    @UmSyncMasterChangeSeqNum
    private val personCustomFieldValueMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    private val personCustomFieldValueLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    private val personCustomFieldValueLastChangedBy: Int = 0
}
