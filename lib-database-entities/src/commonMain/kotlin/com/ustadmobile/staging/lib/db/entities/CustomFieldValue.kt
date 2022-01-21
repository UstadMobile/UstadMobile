package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

//@SyncableEntity(tableId = 57)
@Entity
@Serializable
data class CustomFieldValue(

    @PrimaryKey(autoGenerate = true)
    var customFieldValueUid: Long = 0,

    //custom field uid
    var customFieldValueFieldUid: Long = 0,

    //Entity uid (eg clazz uid / person uid)
    var customFieldValueEntityUid: Long = 0,

    //value as String
    var customFieldValueValue: String? = null,

    var customFieldValueCustomFieldValueOptionUid: Long = 0,

    @MasterChangeSeqNum
    var customFieldValueMCSN: Long = 0,

    @LocalChangeSeqNum
    var customFieldValueLCSN: Long = 0,

    @LastChangedBy
    var customFieldValueLCB: Int = 0,

    @LastChangedTime
    var customFieldLct: Long = 0
)
