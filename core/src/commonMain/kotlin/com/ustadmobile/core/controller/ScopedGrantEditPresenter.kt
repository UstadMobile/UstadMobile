package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.core.util.ext.combinedFlagValue
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.ScopedGrantEditView.Companion.ARG_PERMISSION_LIST
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ScopedGrantEditPresenter(context: Any,
        arguments: Map<String, String>, view: ScopedGrantEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<ScopedGrantEditView, ScopedGrant>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }


    override fun onLoadFromJson(bundle: Map<String, String>): ScopedGrant {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: ScopedGrant?
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ScopedGrant.serializer(), entityJsonStr)
        }else {
            editEntity = ScopedGrant().also {
                it.sgGroupUid = arguments[ScopedGrantEditView.ARG_GRANT_TO_GROUPUID]?.toLong() ?: 0
            }
        }

        val permissionListKey = arguments[ARG_PERMISSION_LIST]?.toInt()
            ?: throw IllegalArgumentException("Invalid permission list flag")

        val permissionList = PERMISSION_LIST_MAP[permissionListKey]
            ?: throw IllegalArgumentException("Invalid permission list key")

        view.bitmaskList = DoorMutableLiveData(permissionList.map {
            it.toBitmaskFlag(editEntity.sgPermissions)
        })

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: ScopedGrant) {
        val permissionsList = view.bitmaskList?.getValue() ?: throw IllegalStateException("No bitmask list")
        entity.sgPermissions = permissionsList.combinedFlagValue

        val serializedResult = safeStringify(di, ListSerializer(ScopedGrant.serializer()),
            listOf(entity))
        finishWithResult(serializedResult)
    }

    companion object {


        /**
         * List of all permission flags and the respective message ID
         */
        val PERMISSION_MESSAGE_ID_LIST = listOf(
            BitmaskMessageId(Role.PERMISSION_PERSON_DELEGATE, MessageID.permission_person_delegate),
            BitmaskMessageId(Role.PERMISSION_SCHOOL_SELECT, MessageID.view_school),
            BitmaskMessageId(Role.PERMISSION_SCHOOL_UPDATE, MessageID.edit_school),
            BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_STUDENT, MessageID.enrol_and_unenrol_students),
            BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_TEACHER, MessageID.enrol_and_unenrol_teachers),
            BitmaskMessageId(Role.PERMISSION_CLAZZ_SELECT, MessageID.view_clazzes),
            BitmaskMessageId(Role.PERMISSION_ADD_CLASS_TO_SCHOOL, MessageID.add_new_clazz_to_school),
            BitmaskMessageId(Role.PERMISSION_CLAZZ_UPDATE, MessageID.edit_clazzes),
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
            BitmaskMessageId(Role.PERMISSION_PERSONSOCIOECONOMIC_UPDATE, MessageID.edit_socioeconomic_details_of_members))


        //TODO: DRY principle - these should be based on the main list and just filter it...
        /*
         * Map of those permissions that are to be shown for a given table:
         * tableId to list of Pairs(permission, messageId for permission)
         */
        val PERMISSION_LIST_MAP: Map<Int, List<BitmaskMessageId>> = mapOf(
            Clazz.TABLE_ID to listOf(
                BitmaskMessageId(Role.PERMISSION_PERSON_DELEGATE, MessageID.permission_person_delegate),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_SELECT, MessageID.view_clazz),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_UPDATE, MessageID.edit_clazz),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_STUDENT, MessageID.enrol_and_unenrol_students),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_TEACHER, MessageID.enrol_and_unenrol_teachers),
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
                BitmaskMessageId(Role.PERMISSION_PERSONSOCIOECONOMIC_UPDATE, MessageID.edit_socioeconomic_details_of_members)),
            School.TABLE_ID to listOf(
                BitmaskMessageId(Role.PERMISSION_PERSON_DELEGATE, MessageID.permission_person_delegate),
                BitmaskMessageId(Role.PERMISSION_SCHOOL_SELECT, MessageID.view_school),
                BitmaskMessageId(Role.PERMISSION_SCHOOL_UPDATE, MessageID.edit_school),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_STUDENT, MessageID.enrol_and_unenrol_students),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_ADD_TEACHER, MessageID.enrol_and_unenrol_teachers),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_SELECT, MessageID.view_clazzes),
                BitmaskMessageId(Role.PERMISSION_ADD_CLASS_TO_SCHOOL, MessageID.add_new_clazz_to_school),
                BitmaskMessageId(Role.PERMISSION_CLAZZ_UPDATE, MessageID.edit_clazzes),
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
                BitmaskMessageId(Role.PERMISSION_PERSONSOCIOECONOMIC_UPDATE, MessageID.edit_socioeconomic_details_of_members)),
            )

    }

}