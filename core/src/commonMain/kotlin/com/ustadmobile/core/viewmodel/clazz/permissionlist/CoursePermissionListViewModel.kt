package com.ustadmobile.core.viewmodel.clazz.permissionlist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.CoursePermissionConstants
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.CourseTerminology
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CoursePermissionListUiState(
    val permissionsList: ListPagingSourceFactory<CoursePermissionAndListDetail> = { EmptyPagingSource() },
    val permissionLabels: List<Pair<StringResource, Long>> = emptyList(),
    val courseTerminology: CourseTerminology? = null,
    val showDeleteOption: Boolean = false,
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
        activeRepo.coursePermissionDao().findByClazzUidAsPagingSource(
            clazzUid = clazzUid, includeDeleted = false,
        )
    }

    init {
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
            _uiState.whenSubscribed {
                activeRepo.coursePermissionDao().personHasPermissionWithClazzPairAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    clazzUid = clazzUid,
                    firstPermission = PermissionFlags.COURSE_VIEW,
                    secondPermission = PermissionFlags.COURSE_EDIT
                ).distinctUntilChanged().collect {
                    val (hasViewPermission, hasEditPermission) = it
                    _uiState.update { prev ->
                        prev.copy(
                            permissionLabels = CoursePermissionConstants.COURSE_PERMISSIONS_LABELS,
                            permissionsList = pagingSource.takeIf { hasViewPermission }
                                ?: { EmptyPagingSource() },
                            showDeleteOption = hasEditPermission,
                        )
                    }

                    _appUiState.update { prev ->
                        prev.copy(
                            fabState = prev.fabState.copy(
                                visible = hasEditPermission
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        //not in use here
    }

    override fun onClickAdd() {
        val goToOnPersonSelectedArg = CoursePermissionEditViewModel.DEST_NAME
            .appendQueryArgs(
                mapOf(
                    UstadView.ARG_CLAZZUID to clazzUid.toString(),
                    UstadView.ARG_POPUPTO_ON_FINISH to destinationName,
                )
            )

        navigateForResult(
            nextViewName = PersonListViewModel.DEST_NAME,
            key = RESULT_KEY_PERMISSION,
            currentValue = null,
            serializer = CoursePermission.serializer(),
            args = buildMap {
                put(UstadView.ARG_LISTMODE, ListViewMode.PICKER.mode)
                put(PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED, goToOnPersonSelectedArg)
            },
        )
    }

    fun onClickEntry(coursePermission: CoursePermission) {
        navController.navigate(
            CoursePermissionDetailViewModel.DEST_NAME,
            buildMap {
                put(ARG_ENTITY_UID, coursePermission.cpUid.toString())
                putFromSavedStateIfPresent(ARG_CLAZZUID)
            }
        )
    }

    fun onClickDeleteEntry(coursePermission: CoursePermission) {
        viewModelScope.launch {
            activeRepo.coursePermissionDao().setDeleted(
                coursePermission.cpUid, true, systemTimeInMillis(),
            )
        }
    }


    companion object {

        const val DEST_NAME = "CoursePermissionList"

        const val RESULT_KEY_PERMISSION = "permissionResult"

    }
}