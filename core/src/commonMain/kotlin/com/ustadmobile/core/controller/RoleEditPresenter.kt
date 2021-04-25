package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.RoleEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class RoleEditPresenter(context: Any,
                          arguments: Map<String, String>, view: RoleEditView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<RoleEditView, Role>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Role? {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val role = withTimeoutOrNull(2000) {
            db.roleDao.findByUidAsync(entityUid)
        } ?: Role()

        view.permissionList = DoorMutableLiveData(
                FLAGS_AVAILABLE.map { BitmaskFlag(it.flagVal, it.messageId,
                        (role.rolePermissions and it.flagVal) == it.flagVal ) })

        return role
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Role? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Role? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, Role.serializer(), entityJsonStr)
        }else {
            editEntity = Role()
        }

        view.permissionList = DoorMutableLiveData(
                FLAGS_AVAILABLE.map { BitmaskFlag(it.flagVal, it.messageId,
                        (editEntity.rolePermissions and it.flagVal) == it.flagVal ) }
        )

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Role) {

        entity.rolePermissions = view.permissionList?.getValue()?.fold(0L, {acc, flag ->
            acc + (if(flag.enabled) flag.flagVal else 0)
        }) ?: 0L

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.roleUid == 0L) {
                entity.roleUid = repo.roleDao.insertAsync(entity)
            }else {
                repo.roleDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))

        }
    }

    companion object {

        val FLAGS_AVAILABLE = listOf(
            BitmaskFlag(Role.PERMISSION_CLAZZ_SELECT, MessageID.permission_clazz_select),
            BitmaskFlag(Role.PERMISSION_CLAZZ_INSERT, MessageID.permission_clazz_insert),
            BitmaskFlag(Role.PERMISSION_CLAZZ_UPDATE, MessageID.permission_clazz_update),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT, MessageID.permission_attendance_insert),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT, MessageID.permission_activity_insert),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT, MessageID.permission_sel_question_insert),
            BitmaskFlag(Role.PERMISSION_PERSON_SELECT, MessageID.permission_person_select),
            BitmaskFlag(Role.PERMISSION_PERSON_INSERT, MessageID.permission_person_insert),
            BitmaskFlag(Role.PERMISSION_PERSON_UPDATE, MessageID.permission_person_update),
            BitmaskFlag(Role.PERMISSION_CLAZZ_ADD_TEACHER, MessageID.permission_clazz_add_teacher),
            BitmaskFlag(Role.PERMISSION_CLAZZ_ADD_STUDENT, MessageID.permission_clazz_add_student),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT, MessageID.permission_attendance_select),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE, MessageID.permission_attendance_update),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE, MessageID.permission_activity_update),
            BitmaskFlag(Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT, MessageID.permission_activity_select),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT, MessageID.permission_sel_select),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE, MessageID.permission_sel_update),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_SELECT, MessageID.permission_sel_question_select),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_INSERT, MessageID.permission_sel_question_insert),
            BitmaskFlag(Role.PERMISSION_SEL_QUESTION_UPDATE, MessageID.permission_sel_question_update),
            BitmaskFlag(Role.PERMISSION_PERSON_PICTURE_SELECT, MessageID.permission_person_picture_select),
            BitmaskFlag(Role.PERMISSION_PERSON_PICTURE_INSERT, MessageID.permission_person_picture_insert),
            BitmaskFlag(Role.PERMISSION_PERSON_PICTURE_UPDATE, MessageID.permission_person_picture_update),
            BitmaskFlag(Role.PERMISSION_ASSIGNMENT_SELECT , MessageID.permission_clazz_assignment_view),
            BitmaskFlag(Role.PERMISSION_ASSIGNMENT_UPDATE , MessageID.permission_clazz_asignment_edit),
            BitmaskFlag(Role.PERMISSION_PERSON_DELEGATE, MessageID.permission_person_delegate),
            BitmaskFlag(Role.PERMISSION_ROLE_SELECT, MessageID.permission_role_select),
            BitmaskFlag(Role.PERMISSION_ROLE_INSERT, MessageID.permission_role_insert),
            BitmaskFlag(Role.PERMISSION_SCHOOL_SELECT, MessageID.permission_school_select),
            BitmaskFlag(Role.PERMISSION_SCHOOL_INSERT, MessageID.permission_school_insert),
            BitmaskFlag(Role.PERMISSION_SCHOOL_UPDATE, MessageID.permission_school_update),
            BitmaskFlag(Role.PERMISSION_SCHOOL_ADD_STUDENT, MessageID.add_student_to_school),
            BitmaskFlag(Role.PERMISSION_SCHOOL_ADD_STAFF, MessageID.add_staff_to_school),
            BitmaskFlag(Role.PERMISSION_RESET_PASSWORD, MessageID.permission_password_reset)



        )

    }

}