package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.combinedFlagValue
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ScopedGrant

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ScopedGrantEditView.Companion.ARG_PERMISSION_LIST
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlinx.serialization.builtins.ListSerializer


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


    override fun onLoadFromJson(bundle: Map<String, String>): ScopedGrant? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ScopedGrant? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ScopedGrant.serializer(), entityJsonStr)
        }else {
            editEntity = ScopedGrant()
        }

        val permissionListKey = arguments[ARG_PERMISSION_LIST]?.toInt()
            ?: throw IllegalArgumentException("Invalid permission list flag")

        val permissionList = PERMISSION_LIST_MAP[permissionListKey]
            ?: throw IllegalArgumentException("Invalid permission list key")

        view.bitmaskList = DoorMutableLiveData(permissionList.map {
            BitmaskFlag(it.first, it.second, editEntity.sgPermissions.hasFlag(it.first))
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
        GlobalScope.launch(doorMainDispatcher()) {
            val permissionsList = view.bitmaskList?.getValue() ?: throw IllegalStateException("No bitmask list")
            entity.sgPermissions = permissionsList.combinedFlagValue

            val serializedResult = safeStringify(di, ListSerializer(ScopedGrant.serializer()),
                listOf(entity))
            finishWithResult(serializedResult)
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers


        /*
         * Map of those permissions that are to be shown for a given table:
         * tableId to list of Pairs(permission, messageId for permission)
         */
        val PERMISSION_LIST_MAP: Map<Int, List<Pair<Long, Int>>> = mapOf(
            Clazz.TABLE_ID to listOf(
                Role.PERMISSION_PERSON_DELEGATE to MessageID.permission_person_delegate,
                Role.PERMISSION_CLAZZ_SELECT to MessageID.permission_clazz_select,
                Role.PERMISSION_CLAZZ_UPDATE to MessageID.permission_clazz_update,
                Role.PERMISSION_CLAZZ_ADD_STUDENT to MessageID.permission_clazz_add_student,
                Role.PERMISSION_CLAZZ_ADD_TEACHER to MessageID.permission_clazz_add_teacher,
                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT to MessageID.permission_attendance_select,
                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE to MessageID.permission_attendance_update,
                Role.PERMISSION_CLAZZ_CONTENT_SELECT to MessageID.view_class_content,
                Role.PERMISSION_CLAZZ_CONTENT_UPDATE to MessageID.edit_class_content,
                Role.PERMISSION_CLAZZWORK_SELECT to MessageID.permission_clazz_assignment_view,
                Role.PERMISSION_CLAZZWORK_UPDATE to MessageID.permission_clazz_asignment_edit,
                Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT to MessageID.view_class_learning_records,
                Role.PERMISSION_PERSON_SELECT to MessageID.permission_person_select,
                Role.PERMISSION_PERSON_UPDATE to MessageID.permission_person_update,
                Role.PERMISSION_PERSONCONTACT_SELECT to MessageID.view_contact_details_of_members,
                Role.PERMISSION_PERSONCONTACT_UPDATE to MessageID.edit_contact_details_of_members,
                Role.PERMISSION_PERSONSOCIOECONOMIC_SELECT to MessageID.view_socioeconomic_details_of_members,
                Role.PERMISSION_PERSONSOCIOECONOMIC_UPDATE to MessageID.edit_socioeconomic_details_of_members)
        )

    }

}