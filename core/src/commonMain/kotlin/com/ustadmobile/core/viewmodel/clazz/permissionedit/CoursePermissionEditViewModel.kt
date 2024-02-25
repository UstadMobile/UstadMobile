package com.ustadmobile.core.viewmodel.clazz.permissionedit

import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.PermissionConstants
import com.ustadmobile.core.viewmodel.clazz.titleStringResource
import com.ustadmobile.lib.db.entities.CoursePermission
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
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

    init {
        _uiState.update { prev ->
            prev.copy(
                permissionLabels = PermissionConstants.COURSE_PERMISSIONS_LABELS,
            )
        }

        launchWithLoadingIndicator(
            onSetFieldsEnabled = {
                _uiState.update { prev -> prev.copy(fieldsEnabled = it) }
            }
        ) {
            val entity = loadEntity(
                serializer = CoursePermission.serializer(),
                onLoadFromDb = { db ->
                    db.coursePermissionDao.findByUid(entityUidArg)
                },
                makeDefault = {
                    CoursePermission(
                        cpUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CoursePermission.TABLE_ID),
                        cpClazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0,
                        cpToEnrolmentRole = savedStateHandle[ARG_GRANT_TO_ROLE]?.toInt() ?: 0,
                        cpToPersonUid = savedStateHandle[ARG_RANT_TO_PERSONUID]?.toLong() ?: 0L
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

            val roleStringResource = entity?.titleStringResource()
            val title = if(roleStringResource != null) {
                val terminology = activeRepo.localFirstThenRepoIfNull {
                    it.courseTerminologyDao.findByUidAsync(entity.cpClazzUid)
                }

                val terminologyEntries = terminology?.toTerminologyEntries(
                    json = json,
                    systemImpl = systemImpl
                ) ?: emptyList()

                terminologyEntries.firstOrNull { it.stringResource == roleStringResource }
                    ?.term ?: systemImpl.getString(roleStringResource)
            }else {
                activeRepo.localFirstThenRepoIfNull {
                    it.personDao.findByUidAsync(entity?.cpToPersonUid ?: 0)?.personFullName()
                } ?: ""
            }

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
                        cpPermissionsFlag = if(entityVal.cpPermissionsFlag.hasFlag(flag)) {
                            entityVal.cpPermissionsFlag.and(flag.inv())
                        }else {
                            entityVal.cpPermissionsFlag.or(flag)
                        }
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

            activeRepo.withDoorTransactionAsync {
                activeRepo.coursePermissionDao.upsertAsync(entity)
            }

            finishWithResult(CoursePermissionDetailViewModel.DEST_NAME, entity.cpUid, entity)
        }
    }

    companion object {

        const val DEST_NAME = "CoursePermissionEdit"

        const val ARG_GRANT_TO_ROLE = "grantToRole"

        const val ARG_RANT_TO_PERSONUID = "grantToPersonUid"


    }
}