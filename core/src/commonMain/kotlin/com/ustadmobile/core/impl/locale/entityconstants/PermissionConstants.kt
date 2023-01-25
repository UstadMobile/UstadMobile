package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.lib.db.entities.Role

object PermissionConstants {

    val PERMISSION_MESSAGE_IDS = listOf(
        BitmaskMessageId(Role.PERMISSION_PERSON_DELEGATE, MessageID.permission_person_delegate),
        BitmaskMessageId(Role.PERMISSION_SCHOOL_SELECT, MessageID.view_school),
        BitmaskMessageId(Role.PERMISSION_SCHOOL_UPDATE, MessageID.edit_school),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_STUDENT, MessageID.enrol_and_unenrol_students),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_TEACHER, MessageID.enrol_and_unenrol_teachers),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_SELECT, MessageID.view_clazzes),
        BitmaskMessageId(Role.PERMISSION_ADD_CLASS_TO_SCHOOL, MessageID.add_new_clazz_to_school),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_UPDATE, MessageID.edit_clazzes),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT, MessageID.permission_attendance_insert),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT, MessageID.permission_attendance_select),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE, MessageID.permission_attendance_update),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_CONTENT_SELECT, MessageID.view_class_content),
        BitmaskMessageId(Role.PERMISSION_CLAZZ_CONTENT_UPDATE, MessageID.edit_class_content),
        BitmaskMessageId(Role.PERMISSION_ASSIGNMENT_SELECT, MessageID.view_assignments),
        BitmaskMessageId(Role.PERMISSION_ASSIGNMENT_UPDATE, MessageID.add_or_edit_assignment),
        BitmaskMessageId(Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT, MessageID.view_class_learning_records),
        BitmaskMessageId(Role.PERMISSION_PERSON_SELECT, MessageID.view_basic_profile_of_members),
        BitmaskMessageId(Role.PERMISSION_PERSON_UPDATE, MessageID.edit_basic_profile_of_members),
        BitmaskMessageId(Role.PERMISSION_PERSONCONTACT_SELECT, MessageID.view_contact_details_of_members),
        BitmaskMessageId(Role.PERMISSION_PERSONCONTACT_UPDATE, MessageID.edit_contact_details_of_members),
        BitmaskMessageId(Role.PERMISSION_PERSONSOCIOECONOMIC_SELECT, MessageID.view_socioeconomic_details_of_members),
        BitmaskMessageId(Role.PERMISSION_PERSONSOCIOECONOMIC_UPDATE, MessageID.edit_socioeconomic_details_of_members)
    )

}