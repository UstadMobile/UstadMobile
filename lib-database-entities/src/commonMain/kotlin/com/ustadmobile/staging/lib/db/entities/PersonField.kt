package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


/**
 * This Entity represents every field associated with the Person. This includes Core fields to be
 * displayed in the Edit/New/Detail pages.
 *
 * Note that fields are not associated with any specific
 * Person but apply to all Persons. Their values are mapped with the entity; PersonCustomFieldValue.
 *
 * The idea here is to build - for every core & custom
 * field - relevant label, icon and internal field name here.
 *
 * Any additional custom fields are to be added here. For eg: if you want to add a custom field
 * for measuring height of the person - you would add the relevant icon as String and field as
 * MessageID that maps to translation strings (which would be gotten via impl.getString(..) ). The
 * fieldName is internal and could just be "height of the person".
 *
 */
@SyncableEntity(tableId = 20)
@Entity
@Serializable
open class PersonField {

    @PrimaryKey(autoGenerate = true)
    var personCustomFieldUid: Long = 0

    //Any extra field names that isn't used in the views.
    var fieldName: String? = null

    //The label of the field used in the views.
    var labelMessageId: Int = 0

    //The field icon used in the view.
    var fieldIcon: String? = null

    @MasterChangeSeqNum
    var personFieldMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personFieldLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personFieldLastChangedBy: Int = 0

    companion object {

        val FIELD_TYPE_HEADER = 1
        val FIELD_TYPE_FIELD = 2
        val FIELD_TYPE_TEXT = 3
        val FIELD_TYPE_DROPDOWN = 4
        val FIELD_TYPE_PHONE_NUMBER = 5
        val FIELD_TYPE_DATE = 6
        val FIELD_TYPE_PASSWORD = 7
        val FIELD_TYPE_USERNAME = 8


        const val FIELD_HEADING_PROFILE = 1
        const val FIELD_HEADING_FULL_NAME = 2
        const val FIELD_HEADING_FIRST_NAMES = 3
        const val FIELD_HEADING_LAST_NAME = 4
        const val FIELD_HEADING_BIRTHDAY = 5
        const val FIELD_HEADING_HOME_ADDRESS = 6
        const val FIELD_HEADING_ATTENDANCE = 7
        const val FIELD_HEADING_FATHER = 8
        const val FIELD_HEADING_FATHERS_NAME = 9
        const val FIELD_HEADING_FATHERS_NUMBER = 10
        const val FIELD_HEADING_MOTHERS_NAME = 11
        const val FIELD_HEADING_MOTHERS_NUMBER = 12
        const val FIELD_HEADING_MOTHER = 13
        const val FIELD_HEADING_CLASSES = 14
        const val FIELD_HEADING_USERNAME = 15
        const val FIELD_HEADING_PASSWORD = 16
        const val FIELD_HEADING_CONFIRM_PASSWORD = 17
        const val FIELD_HEADING_ROLE_ASSIGNMENTS = 18

        const val FIELD_HEADING_FIRST_NAMES_ALT = 19
        const val FIELD_HEADING_LAST_NAME_ALT = 20
        const val FIELD_HEADING_PHONE_NUMBER = 21
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PersonField

        if (personCustomFieldUid != other.personCustomFieldUid) return false
        if (fieldName != other.fieldName) return false
        if (labelMessageId != other.labelMessageId) return false
        if (fieldIcon != other.fieldIcon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = personCustomFieldUid.hashCode()
        result = 31 * result + (fieldName?.hashCode() ?: 0)
        result = 31 * result + labelMessageId
        result = 31 * result + (fieldIcon?.hashCode() ?: 0)
        return result
    }


}
