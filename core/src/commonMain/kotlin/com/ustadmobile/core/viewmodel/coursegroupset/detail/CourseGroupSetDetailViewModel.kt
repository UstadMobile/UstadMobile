package com.ustadmobile.core.viewmodel.coursegroupset.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.courseGroupMemberDao.findByCourseGroupSetAndClazzAsFlow(
                        cgsUid = entityUidArg,
                        clazzUid = 0L,
                        time = systemTimeInMillis(),
                        activeFilter = 0,
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                membersList = it
                            )
                        }
                    }
                }

                launch {
                    activeRepo.courseGroupSetDao.findByUidAsFlow(
                        entityUidArg
                    ).collectLatest { courseGroupSet ->
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

                        activeRepo.clazzDao.takeIf {
                            courseGroupSet != null
                        }?.personHasPermissionWithClazzAsFlow(
                            accountPersonUid = activeUserPersonUid,
                            clazzUid = courseGroupSet?.cgsClazzUid ?: 0L,
                            permission = Role.PERMISSION_CLAZZ_ADD_STUDENT
                        )?.collect { hasEditPermission ->
                            _appUiState.update { prev ->
                                prev.copy(
                                    fabState = FabUiState(
                                        visible = hasEditPermission,
                                        text = systemImpl.getString(MR.strings.edit),
                                        icon = FabUiState.FabIcon.EDIT,
                                        onClick = this@CourseGroupSetDetailViewModel::onClickEdit
                                    )
                                )
                            }
                        }
                    }
                }

            }
        }
    }

    fun onClickEdit() {
        navController.navigate(
            viewName = CourseGroupSetEditViewModel.DEST_NAME,
            args = mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString())
        )
    }

    companion object {

        const val DEST_NAME = "CourseGroupSet"

    }
}