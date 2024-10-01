package com.ustadmobile.core.viewmodel.clazz.permissiondetail

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.viewmodel.clazz.CoursePermissionConstants
import com.ustadmobile.core.viewmodel.clazz.getTitleForCoursePermission
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

data class CoursePermissionDetailUiState(
    val coursePermission: CoursePermission? = null,
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
)

class CoursePermissionDetailViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<CoursePermission>(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        CoursePermissionDetailUiState()
    )

    val uiState: Flow<CoursePermissionDetailUiState> = _uiState.asStateFlow()

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        val entityFlow = activeRepoWithFallback.coursePermissionDao().findByUidAndClazzUidAsFlow(
            entityUidArg, clazzUid)

        val permissionFlow = activeRepoWithFallback.coursePermissionDao().personHasPermissionWithClazzPairAsFlow(
            accountPersonUid = activeUserPersonUid,
            clazzUid = clazzUid,
            firstPermission = PermissionFlags.COURSE_VIEW,
            secondPermission = PermissionFlags.COURSE_EDIT
        )


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
                    entityFlow.combine(permissionFlow) { entity, permissions ->
                        Pair(entity.takeIf { permissions.firstPermission }, permissions)
                    }.distinctUntilChanged().collectLatest {
                        val (entity, permissionPair) = it
                        val (_, hasEditPermission) = permissionPair

                        _uiState.update { prev ->
                            prev.copy(
                                coursePermission = entity,
                                permissionLabels = if(entity != null){
                                    CoursePermissionConstants.COURSE_PERMISSIONS_LABELS
                                }else {
                                    emptyList()
                                }
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(
                                    visible = hasEditPermission
                                )
                            )
                        }

                        if(entity != null) {
                            val title = getTitleForCoursePermission(entity)
                            _appUiState.update { prev ->
                                prev.copy(title = title)
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
            buildMap {
                put(ARG_ENTITY_UID, (_uiState.value.coursePermission?.cpUid?.toString() ?: "0"))
                putFromSavedStateIfPresent(ARG_CLAZZUID)
            }
        )
    }


    companion object {

        const val DEST_NAME = "CoursePermissionDetail"

    }
}