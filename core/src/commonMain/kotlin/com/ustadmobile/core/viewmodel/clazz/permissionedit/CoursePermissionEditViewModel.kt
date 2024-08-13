package com.ustadmobile.core.viewmodel.clazz.permissionedit

import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.CoursePermissionConstants
import com.ustadmobile.lib.db.entities.CoursePermission
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.util.ext.toggleFlag
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazz.getTitleForCoursePermission
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync

data class CoursePermissionEditUiState(
    val entity: CoursePermission? = null,
    val fieldsEnabled: Boolean = false,
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
)

class CoursePermissionEditViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(CoursePermissionEditUiState())

    val uiState: Flow<CoursePermissionEditUiState> = _uiState.asStateFlow()

    private val clazzUid: Long = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        _uiState.update { prev ->
            prev.copy(
                permissionLabels = CoursePermissionConstants.COURSE_PERMISSIONS_LABELS,
            )
        }
        _appUiState.update { prev -> prev.copy(hideBottomNavigation = true) }

        launchIfHasPermission(
            permissionCheck = {
                it.coursePermissionDao().personHasPermissionWithClazzAsync2(
                    activeUserPersonUid, clazzUid, PermissionFlags.COURSE_EDIT
                )
            },
            onSetFieldsEnabled = {
                _uiState.update { prev -> prev.copy(fieldsEnabled = it) }
            },
        ) {
            val entity = loadEntity(
                serializer = CoursePermission.serializer(),
                onLoadFromDb = { db ->
                    db.coursePermissionDao().findByUidAndClazzUid(entityUidArg, clazzUid)
                },
                makeDefault = {
                    CoursePermission(
                        cpUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CoursePermission.TABLE_ID),
                        cpClazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0,
                        cpToEnrolmentRole = savedStateHandle[ARG_GRANT_TO_ROLE]?.toInt() ?: 0,
                        cpToPersonUid = savedStateHandle[ARG_PERSON_UID]?.toLong() ?: 0L
                    )
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            entity = it
                        )
                    }
                }
            )

            val title = getTitleForCoursePermission(entity)

            _appUiState.update { prev ->
                prev.copy(
                    title = title,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@CoursePermissionEditViewModel::onClickSave,
                    )
                )
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onTogglePermission(flag: Long) {
        _uiState.update { prev ->
            val entityVal = prev.entity
            if(entityVal != null) {
                prev.copy(
                    entity = entityVal.copy(
                        cpPermissionsFlag = entityVal.cpPermissionsFlag.toggleFlag(flag)
                    )
                )
            }else {
                prev
            }
        }
    }

    fun onClickSave() {
        launchWithLoadingIndicator(
            onSetFieldsEnabled = { _uiState.update { prev -> prev.copy(fieldsEnabled = it) }}
        ) {
            val entity = _uiState.value.entity ?: return@launchWithLoadingIndicator

            activeRepoWithFallback.withDoorTransactionAsync {
                activeRepoWithFallback.coursePermissionDao().upsertAsync(entity)
            }

            val popUpToOnFinish = savedStateHandle[UstadView.ARG_POPUPTO_ON_FINISH]
            if(popUpToOnFinish != null){
                navController.popBackStack(popUpToOnFinish, inclusive = false)
            }else {
                finishWithResult(CoursePermissionDetailViewModel.DEST_NAME, entity.cpUid, entity)
            }
        }
    }

    companion object {

        const val DEST_NAME = "CoursePermissionEdit"

        const val ARG_GRANT_TO_ROLE = "grantToRole"

    }
}