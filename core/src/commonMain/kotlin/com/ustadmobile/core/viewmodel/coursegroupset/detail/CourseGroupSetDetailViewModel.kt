package com.ustadmobile.core.viewmodel.coursegroupset.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CourseGroupSetDetailUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList()
)

class CourseGroupSetDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<CourseGroupSet>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(CourseGroupSetDetailUiState())

    val uiState: Flow<CourseGroupSetDetailUiState> = _uiState.asStateFlow()

    val argClazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        viewModelScope.launch {
            val permissionsFlow = activeRepo.coursePermissionDao()
                .personHasPermissionWithClazzPairAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    clazzUid = argClazzUid,
                    firstPermission = PermissionFlags.COURSE_VIEW,
                    secondPermission = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
                ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

            val memberListFlow = activeRepo.courseGroupMemberDao()
                .findByCourseGroupSetAndClazzAsFlow(
                    cgsUid = entityUidArg,
                    clazzUid = argClazzUid,
                    time = systemTimeInMillis(),
                    activeFilter = 0,
                    accountPersonUid = activeUserPersonUid,
                )

            val entityFlow = activeRepo.courseGroupSetDao().findByUidAsFlow(
                entityUidArg
            )

            _uiState.whenSubscribed {
                launch {
                    memberListFlow.combine(permissionsFlow) { entity, permissionPair ->
                        entity to permissionPair
                    }.distinctUntilChanged().collect {
                        val (hasCourseViewPermission, hasManageStudentPermission) = it.second
                        _uiState.update { prev ->
                            prev.copy(
                                membersList = it.first.takeIf { hasCourseViewPermission } ?: emptyList()
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = FabUiState(
                                    visible = hasManageStudentPermission,
                                    text = systemImpl.getString(MR.strings.edit),
                                    icon = FabUiState.FabIcon.EDIT,
                                    onClick = this@CourseGroupSetDetailViewModel::onClickEdit
                                )
                            )
                        }
                    }
                }

                launch {
                    entityFlow.combine(permissionsFlow) { entity, permissionPair ->
                        entity.takeIf { permissionPair.firstPermission }
                    }.collect { courseGroupSet ->
                        _uiState.update { prev ->
                            prev.copy(
                                courseGroupSet = courseGroupSet
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                title = courseGroupSet?.cgsName ?: ""
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickEdit() {
        navController.navigate(
            viewName = CourseGroupSetEditViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_ENTITY_UID to entityUidArg.toString(),
                ARG_CLAZZUID to argClazzUid.toString(),
            )
        )
    }

    companion object {

        const val DEST_NAME = "CourseGroupSet"

    }
}