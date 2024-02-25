package com.ustadmobile.core.viewmodel.clazz.permissionlist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.PermissionConstants
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.Role
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CoursePermissionListUiState(
    val permissionsList: ListPagingSourceFactory<CoursePermissionAndListDetail> = { EmptyPagingSource() },
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
    val addOptionsVisible: Boolean = false,
    val courseTerminology: CourseTerminology? = null,
)

class CoursePermissionListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<CoursePermissionListUiState>(
    di, savedStateHandle, CoursePermissionListUiState(), DEST_NAME
) {

    private val clazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzuid")

    private val pagingSource: () -> PagingSource<Int, CoursePermissionAndListDetail> = {
        activeRepo.coursePermissionDao.findByClazzUid(
            clazzUid = clazzUid, includeDeleted = false,
        )
    }

    init {
        _uiState.update { prev ->
            prev.copy(
                permissionLabels = PermissionConstants.COURSE_PERMISSIONS_LABELS,
                permissionsList = pagingSource
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.permissions),
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.permissions),
                    visible = false,
                    icon = FabUiState.FabIcon.ADD,
                    onClick = this@CoursePermissionListViewModel::onClickAdd
                )
            )
        }

        viewModelScope.launch {
            activeRepo.clazzDao.personHasPermissionWithClazzAsFlow(
                accountPersonUid = activeUserPersonUid,
                clazzUid = clazzUid,
                permission = Role.PERMISSION_CLAZZ_UPDATE,
            ).collect { hasPermission ->
                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = hasPermission
                        )
                    )
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        //not in use here
    }

    override fun onClickAdd() {
        _uiState.update { prev -> prev.copy(addOptionsVisible = true) }
    }

    fun onDismissAddOptions() {
        _uiState.update { prev -> prev.copy(addOptionsVisible = false) }
    }

    fun onClickAddNewForPerson() {

    }

    fun onClickAddNewForRole(role: Int) {
        navigateForResult(
            nextViewName = CoursePermissionEditViewModel.DEST_NAME,
            key = RESULT_KEY_PERMISSION,
            currentValue = null,
            serializer = CoursePermission.serializer(),
            args = mapOf(
                CoursePermissionEditViewModel.ARG_GRANT_TO_ROLE to role.toString(),
            ),
        )
    }

    fun onClickEntry(coursePermission: CoursePermission) {

    }


    companion object {

        const val DEST_NAME = "CoursePermissionList"

        const val RESULT_KEY_PERMISSION = "permissionResult"

    }
}