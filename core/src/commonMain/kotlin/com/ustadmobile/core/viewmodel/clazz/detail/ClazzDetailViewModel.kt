package com.ustadmobile.core.viewmodel.clazz.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzDetailUiState(
    val tabs: List<TabItem> = emptyList(),
)

class ClazzDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<Clazz>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState: Flow<ClazzDetailUiState> = _uiState.asStateFlow()

    private fun createTabList(showAttendance: Boolean): List<TabItem> {
        val tabs = mutableListOf(
            TabItem(
                viewName = ClazzDetailOverviewViewModel.DEST_NAME,
                args = mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString()),
                label = systemImpl.getString(MR.strings.course),
            ),
            TabItem(
                viewName = ClazzMemberListViewModel.DEST_NAME,
                args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                label = systemImpl.getString(MR.strings.members_key).capitalizeFirstLetter(),
            )
        )

        if(showAttendance) {
            tabs.add(
                TabItem(
                    viewName = ClazzLogListAttendanceViewModel.DEST_NAME,
                    args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                    label = systemImpl.getString(MR.strings.attendance),
                )
            )
        }
        tabs.add(
            TabItem(
                viewName = CourseGroupSetListViewModel.DEST_NAME,
                args = mapOf(UstadView.ARG_CLAZZUID to entityUidArg.toString()),
                label = systemImpl.getString(MR.strings.groups),
            )
        )

        return tabs.toList()
    }

    init {
        val viewPermissionFlow = activeRepo.clazzDao.personHasPermissionWithClazzAsFlow2(
            accountPersonUid = activeUserPersonUid,
            clazzUid = entityUidArg,
            permission = CoursePermission.PERMISSION_VIEW
        )

        val attendancePermissionFlow = activeRepo.clazzDao.personHasPermissionWithClazzAsFlow2(
            accountPersonUid = activeUserPersonUid,
            clazzUid = entityUidArg,
            permission = CoursePermission.PERMISSION_ATTENDANCE_VIEW
        )

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeDb.clazzDao.findByUidAsFlow(entityUidArg)
                        .combine(viewPermissionFlow) { clazz, hasViewPermission ->
                            clazz?.takeIf { hasViewPermission }
                        }
                        .combine(attendancePermissionFlow) { clazz, hasAttendancePermission ->
                            clazz to hasAttendancePermission
                        }.collect {
                            val (clazz, hasAttendancePermission) = it
                            val tabList = when (clazz) {
                                null -> emptyList()
                                else -> createTabList(
                                    showAttendance = clazz.clazzFeatures.hasFlag(Clazz.CLAZZ_FEATURE_ATTENDANCE) && hasAttendancePermission
                                )
                            }

                            _uiState.update { prev ->
                                prev.copy(tabs = tabList)
                            }
                        }
                }
            }
        }
    }

    companion object {
        const val DEST_NAME = "Course"
    }

}
