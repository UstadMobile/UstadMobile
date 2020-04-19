package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * This entity represents all the fields including the headers and extra information that will
 * get represented on the view. This includes the field UID (not PK), the type of field
 * (header or text/date/drop-down/phone/etc), the index for order and view flags.
 *
 */
@SyncableEntity(tableId = 19)
@Entity
@Serializable
class PersonDetailPresenterField(
    //PK
    @PrimaryKey(autoGenerate = true)
    var personDetailPresenterFieldUid: Long = 0,

    //The field id associated with PersonField. For Core Fields it is as above. For Custom
    // it starts from 1000 ++
    var fieldUid: Long = 0,

    //The type of this  field (header or field)
    var fieldType: Int = 0,

    //The index used in ordering things
    var fieldIndex: Int = 0,

    //The label of the field used in the views.
    var labelMessageId: Int = 0,

    //The field icon used in the view.
    var fieldIcon: String? = null,

    //The Label of the header (if applicable)
    var headerMessageId: Int = 0,

    //If this presenter field is visible on PersonDetail
    var viewModeVisible: Boolean = false,

    //If this presenter field is visible on PersonEdit/PersonNew
    var editModeVisible: Boolean = false,

    //Set if its uneditable
    //sometimes we want to display a field but not be able to edit it. This is the flag for that.
    var isReadyOnly: Boolean = false){

    @MasterChangeSeqNum
    var personDetailPresenterFieldMastrChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personDetailPresenterFieldLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personDetailPresenterFieldLastChangedBy: Int = 0

    companion object {

        /* Begin constants that represent Person core fields */

        val PERSON_FIELD_UID_FULL_NAME = 1

        val PERSON_FIELD_UID_FIRST_NAMES = 2

        val PERSON_FIELD_UID_LAST_NAME = 3

        val PERSON_FIELD_UID_ATTENDANCE = 4

        val PERSON_FIELD_UID_CLASSES = 5

        val PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER = 6

        val PERSON_FIELD_UID_FATHER_NAME = 7

        val PERSON_FIELD_UID_FATHER_NUMBER = 8

        val PERSON_FIELD_UID_MOTHER_NAME = 9

        val PERSON_FIELD_UID_MOTHER_NUMBER = 10

        val PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER = 11

        val PERSON_FIELD_UID_BIRTHDAY = 12

        val PERSON_FIELD_UID_ADDRESS = 13

        val PERSON_FIELD_UID_USERNAME = 14
        val PERSON_FIELD_UID_PASSWORD = 15
        val PERSON_FIELD_UID_CONFIRM_PASSWORD = 16

        val TYPE_FIELD = 1

        val TYPE_HEADER = 2




        /* Field Uid constants for Person Custom fields begin at this value */
        val CUSTOM_FIELD_MIN_UID = 1000
    }
}
