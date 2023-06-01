package com.ustadmobile.core.viewmodel.clazz.detail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzDetailUiState(
    val tabs: List<TabItem> = emptyList(),
)

class ClazzDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<Clazz>(di, savedStateHandle, ClazzDetailView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState: Flow<ClazzDetailUiState> = _uiState.asStateFlow()

    private fun createTabList(showAttendance: Boolean): List<TabItem> {
        val tabs = mutableListOf(
            TabItem(
                viewName = ClazzDetailOverviewView.VIEW_NAME,
                args = mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString()),
                label = systemImpl.getString(MessageID.course),
            ),
            TabItem(
                viewName = ClazzMemberListView.VIEW_NAME,
                args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                label = systemImpl.getString(MessageID.members),
            )
        )

        if(showAttendance) {
            tabs.add(
                TabItem(
                    viewName = ClazzLogListAttendanceView.VIEW_NAME,
                    args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                    label = systemImpl.getString(MessageID.attendance),
                )
            )
        }
        tabs.add(
            TabItem(
                viewName = CourseGroupSetListView.VIEW_NAME,
                args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                label = systemImpl.getString(MessageID.groups),
            )
        )

        return tabs.toList()
    }

    init {
        val accountPersonUid = accountManager.activeAccount.personUid
        _uiState.update { prev ->
            prev.copy(tabs = createTabList(false))
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeDb.clazzDao.findByUidAsFlow(entityUidArg)
                        .combine(activeDb.clazzDao.personHasPermissionWithClazzAsFlow(
                            accountPersonUid, entityUidArg, PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT
                        )) { clazz: Clazz?, permission: Boolean ->
                            clazz to permission
                        }.collect {
                            val (clazz, hasAttendancePermission) = it
                            val showAttendance =
                                clazz?.clazzFeatures?.hasFlag(Clazz.CLAZZ_FEATURE_ATTENDANCE) == true &&
                                hasAttendancePermission

                            _uiState.update { prev ->
                                prev.copy(tabs = createTabList(showAttendance))
                            }
                        }
                }
            }
        }
    }

}