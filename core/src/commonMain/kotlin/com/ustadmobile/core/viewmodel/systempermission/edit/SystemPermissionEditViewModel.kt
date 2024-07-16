package com.ustadmobile.core.viewmodel.systempermission.edit

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.lib.db.entities.SystemPermission
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.toggleFlag
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.systempermission.SystemPermissionConstants
import com.ustadmobile.door.ext.withDoorTransactionAsync

data class SystemPermissionEditUiState(
    val entity: SystemPermission? = null,
    val fieldsEnabled: Boolean = false,
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
)

class SystemPermissionEditViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {


    private val _uiState = MutableStateFlow(
        SystemPermissionEditUiState())

    val uiState: Flow<SystemPermissionEditUiState> = _uiState.asStateFlow()

    private val argPersonUid = savedStateHandle[ARG_PERSON_UID]?.toLong() ?: 0L

    init {
        _appUiState.update { it.copy(hideBottomNavigation = true) }

        launchIfHasPermission(
            permissionCheck = {
                activeRepo.systemPermissionDao().personHasSystemPermission(activeUserPersonUid,
                    PermissionFlags.MANAGE_USER_PERMISSIONS)
            },
            onSetFieldsEnabled = {
                _uiState.update { prev -> prev.copy(fieldsEnabled = it) }
            }
        ) {
            loadEntity(
                serializer = SystemPermission.serializer(),
                onLoadFromDb = { db ->
                    db.systemPermissionDao().findByPersonUid(argPersonUid)
                },
                makeDefault = { null },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            entity = it,
                            permissionLabels = if(it != null) {
                                SystemPermissionConstants.SYSTEM_PERMISSION_LABELS
                            }else {
                                emptyList()
                            }
                        )
                    }
                }
            )

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@SystemPermissionEditViewModel::onClickSave
                    )
                )
            }


            val title = activeRepo.personDao().getNamesByUidAsync(argPersonUid)

            _appUiState.update { prev -> prev.copy(title = title?.toString() ?: "") }

        }
    }

    fun onTogglePermission(flag: Long) {
        _uiState.update { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    spPermissionsFlag = prev.entity.spPermissionsFlag.toggleFlag(flag)
                )
            )
        }
    }

    fun onClickSave() {
        launchWithLoadingIndicator(
            onSetFieldsEnabled = { _uiState.update { prev -> prev.copy(fieldsEnabled = it) } }
        ) {
            val entity = _uiState.value.entity ?: return@launchWithLoadingIndicator

            activeRepo.withDoorTransactionAsync {
                activeRepo.systemPermissionDao().upsertAsync(entity)
            }

            //There is no case where a new system permission is created by the user.
            navController.popBackStack(UstadView.CURRENT_DEST, true)
        }
    }

    companion object {

        const val DEST_NAME = "SystemPermissionEdit"

    }
}