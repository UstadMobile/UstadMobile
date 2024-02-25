package com.ustadmobile.core.viewmodel.clazz.permissiondetail

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazz.getTitleForCoursePermission
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.CoursePermissionConstants
import dev.icerock.moko.resources.StringResource

data class CoursePermissionDetailUiState(
    val coursePermission: CoursePermission? = null,
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
)

class CoursePermissionDetailViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<CoursePermission>(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        CoursePermissionDetailUiState(
            permissionLabels = CoursePermissionConstants.COURSE_PERMISSIONS_LABELS,
        )
    )

    val uiState: Flow<CoursePermissionDetailUiState> = _uiState.asStateFlow()

    init {
        val coursePermissionFlow = activeRepo.coursePermissionDao.findByUidAsFlow(
            entityUidArg)

        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.edit),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = this@CoursePermissionDetailViewModel::onClickEdit
                )
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    coursePermissionFlow.collectLatest { coursePermission ->
                        _uiState.update { prev ->
                            prev.copy(coursePermission = coursePermission)
                        }

                        launch {
                            val title = getTitleForCoursePermission(coursePermission)

                            _appUiState.update { prev -> prev.copy(title = title) }
                        }

                        activeRepo.clazzDao.personHasPermissionWithClazzAsFlow(
                            accountPersonUid = accountManager.currentAccount.personUid,
                            clazzUid = coursePermission?.cpClazzUid ?: 0L,
                            permission = Role.PERMISSION_CLAZZ_UPDATE
                        ).collect { hasEditPermission ->
                            _appUiState.update { prev ->
                                prev.copy(
                                    fabState = prev.fabState.copy(
                                        visible = coursePermission != null && hasEditPermission
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickEdit() {
        navController.navigate(
            CoursePermissionEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to (_uiState.value.coursePermission?.cpUid?.toString() ?: "0"))
        )
    }


    companion object {

        const val DEST_NAME = "CoursePermissionDetail"

    }
}