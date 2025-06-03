package com.ustadmobile.core.viewmodel.systempermission.detail

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.systempermission.SystemPermissionConstants
import com.ustadmobile.core.viewmodel.systempermission.personHasSystemPermissionAsFlowForUser
import com.ustadmobile.lib.db.entities.SystemPermission
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.viewmodel.systempermission.edit.SystemPermissionEditViewModel
import kotlinx.coroutines.flow.collectLatest

data class SystemPermissionDetailUiState(
    val systemPermission: SystemPermission? = null,
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
)

class SystemPermissionDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<SystemPermission>(di, savedStateHandle, DEST_NAME) {

    private val argPersonUid = savedStateHandle[ARG_PERSON_UID]?.toLong() ?: 0L

    private val _uiState = MutableStateFlow(
        SystemPermissionDetailUiState()
    )

    val uiState: Flow<SystemPermissionDetailUiState> = _uiState.asStateFlow()

    init {
        val entityFlow = activeRepoWithFallback.systemPermissionDao()
            .findByPersonUidAsFlow(argPersonUid)

        val viewPermissionFlow = activeRepoWithFallback.systemPermissionDao()
            .personHasSystemPermissionAsFlowForUser(
                accountPersonUid = activeUserPersonUid, personUid = argPersonUid,
            )

        val editPermissionFlow = activeRepoWithFallback.systemPermissionDao()
            .personHasSystemPermissionAsFlow(
                accountPersonUid = activeUserPersonUid,
                permission = PermissionFlags.MANAGE_USER_PERMISSIONS
            )

        _appUiState.update {
            prev -> prev.copy(
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.edit),
                    onClick = this@SystemPermissionDetailViewModel::onClickEdit,
                    icon = FabUiState.FabIcon.EDIT,
                )
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    entityFlow.combine(viewPermissionFlow) { entity, hasViewPermission ->
                        entity.takeIf { hasViewPermission }
                    }.collectLatest {
                        _uiState.update { prev ->
                            prev.copy(
                                systemPermission = it,
                                permissionLabels = if(it != null) {
                                    SystemPermissionConstants.SYSTEM_PERMISSION_LABELS
                                }else {
                                    emptyList()
                                }
                            )
                        }

                        if(it != null) {
                            val title = activeRepoWithFallback.personDao().getNamesByUidAsync(argPersonUid)

                            _appUiState.update { prev -> prev.copy(title = title?.toString() ?: "") }
                        }
                    }
                }

                launch {
                    editPermissionFlow.collect { hasEditPermission ->
                        _appUiState.update {
                            prev -> prev.copy(
                                fabState = prev.fabState.copy(
                                    visible = hasEditPermission
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onClickEdit() {
        navController.navigate(SystemPermissionEditViewModel.DEST_NAME, buildMap {
            putFromSavedStateIfPresent(ARG_PERSON_UID)
        })
    }

    companion object {

        const val DEST_NAME = "SystemPermission"

    }
}