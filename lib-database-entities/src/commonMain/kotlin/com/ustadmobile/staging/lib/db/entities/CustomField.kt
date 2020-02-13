package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 56)
@Entity
@Serializable
class CustomField {

    @PrimaryKey(autoGenerate = true)
    var customFieldUid: Long = 0

    //name of field eg: Department
    var customFieldName: String? = null
    //Name alt of field eg : in Arabic or Dari
    var customFieldNameAlt: String? = null

    //Pashto
    var customFieldNameAltTwo : String? = null

    //Title in message id
    var customFieldLabelMessageID: Int = 0
    //Icon string
    var customFieldIcon: String? = null
    //Type: dropdown or text. Flags defined here
    var customFieldType: Int = 0
    //Entity type table id (eg: Class or Person)
    var customFieldEntityType: Int = 0
    //if false it is considered not active and it wont show up in the app. effectively "deleted"
    var customFieldActive: Boolean = false
    //Default value
    var customFieldDefaultValue: String? = null

    @MasterChangeSeqNum
    var customFieldMCSN: Long = 0

    @LocalChangeSeqNum
    var customFieldLCSN: Long = 0

    @LastChangedBy
    var customFieldLCB: Int = 0

    fun getNameByLocale(locale: String): String{
        if(locale.equals("fa")){
            return customFieldNameAlt as String
        }else if(locale.equals("ps")){
            return customFieldNameAltTwo as String
        }else{
            return customFieldName as String
        }
    }

    companion object {

        val FIELD_TYPE_TEXT = 1
        val FIELD_TYPE_DROPDOWN = 2
    }
}
