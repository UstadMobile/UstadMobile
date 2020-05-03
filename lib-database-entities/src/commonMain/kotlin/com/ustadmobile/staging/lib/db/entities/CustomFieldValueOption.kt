package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 55)
@Entity
@Serializable
class CustomFieldValueOption {

    @PrimaryKey(autoGenerate = true)
    var customFieldValueOptionUid: Long = 0

    //name of the option
    var customFieldValueOptionName: String? = null

    //custom field uid
    var customFieldValueOptionFieldUid: Long = 0

    //icon string
    var customFieldValueOptionIcon: String? = null

    //title string (message id)
    var customFieldValueOptionMessageId: Int = 0

    //active
    var customFieldValueOptionActive: Boolean = false

    @MasterChangeSeqNum
    var customFieldValueOptionMCSN: Long = 0

    @LocalChangeSeqNum
    var customFieldValueOptionLCSN: Long = 0

    @LastChangedBy
    var customFieldValueOptionLCB: Int = 0

    override fun toString(): String {
        return customFieldValueOptionName ?: "unnamed option"
    }
}
