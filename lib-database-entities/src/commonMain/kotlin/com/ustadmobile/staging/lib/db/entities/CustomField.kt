package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class CustomField(
        @PrimaryKey(autoGenerate = true)
        var customFieldUid: Long = 0,

        //name of field eg: Department
        var customFieldName: String? = null,

        //Name alt of field eg : in Arabic
        var customFieldNameAlt: String? = null,

        //Title in message id
        var customFieldLabelMessageID: Int = 0,

        //Icon string
        var customFieldIcon: String? = null,

        var customFieldIconId: Int = 0,

        var actionOnClick: String? = null,

        //Type: dropdown or text. Flags defined here
        var customFieldType: Int = 0,

        //Entity type table id (eg: Class or Person)
        var customFieldEntityType: Int = 0,

        //if false it is considered not active and it wont show up in the app. effectively "deleted"
        var customFieldActive: Boolean = false,

        //Default value
        var customFieldDefaultValue: String? = null,

        @MasterChangeSeqNum
        var customFieldMCSN: Long = 0,

        @LocalChangeSeqNum
        var customFieldLCSN: Long = 0,

        @LastChangedBy
        var customFieldLCB: Int = 0,

        @LastChangedTime
        var customFieldLct: Long = 0,

        var customFieldInputType: Int = 0x00000001


) {



    companion object {

        //Offset so that this can be used as itemType
        val FIELD_TYPE_TEXT = 5

        val FIELD_TYPE_DROPDOWN = 6

        val FIELD_TYPE_DATE_SPINNER = 7

        val FIELD_TYPE_PICTURE = 8

        // As Per Android.text.InputType
        val INPUT_TYPE_TEXT = 0x00000001

        val INPUT_TYPE_EMAIL = 0x00000020

        val INPUT_TYPE_PHONENUM = 0x00000003

        val ICON_PERSON = 1

        val ICON_PHONE = 2

        val ICON_CALENDAR = 3

        val ICON_EMAIL = 4

        val ICON_ADDRESS = 5

        val ACTION_CALL = "call"

        val ACTION_EMAIL = "email"

        val ACTION_MAPS = "map"

    }
}
