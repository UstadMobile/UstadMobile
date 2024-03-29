package com.ustadmobile.view.clazz.permissionlist

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListUiState
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.emptyFlow
import mui.material.List
import react.FC
import react.Props
import react.create
import react.useMemo
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

val CoursePermissionListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CoursePermissionListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CoursePermissionListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    val terminologyEntries = useCourseTerminologyEntries(uiStateVal.courseTerminology)

    CoursePermissionListComponent {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
        onClickDeleteEntry = viewModel::onClickDeleteEntry
        courseTerminologyEntries = terminologyEntries
    }

    UstadFab {
        fabState = appState.fabState
    }

}

external interface CoursePermissionListProps: Props {

    var uiState: CoursePermissionListUiState

    var onClickEntry: (CoursePermission) -> Unit

    var onClickDeleteEntry: (CoursePermission) -> Unit

    var courseTerminologyEntries: List<TerminologyEntry>

}

val CoursePermissionListComponent = FC<CoursePermissionListProps> { props ->

    val refreshFlow = useMemo(dependencies = emptyArray()) {
        emptyFlow<RefreshCommand>()
    }

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.permissionsList, refreshFlow
    )

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, true
    )

    val muiAppState = useMuiAppState()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }


        content = virtualListContent {
            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { "${it.coursePermission?.cpUid}" }
            ) { item ->
                val delete = props.uiState.showDeleteOption && item?.coursePermission?.cpToEnrolmentRole == 0
                console.log("content show delete: $delete")
                CoursePermissionListItem.create {
                    coursePermission = item
                    permissionLabels = props.uiState.permissionLabels
                    courseTerminologyEntries = props.courseTerminologyEntries
                    onClickEntry = props.onClickEntry
                    showDelete = props.uiState.showDeleteOption && item?.coursePermission?.cpToEnrolmentRole == 0
                    onClickDeleteEntry = props.onClickDeleteEntry
                }
            }
        }

        UstadStandardContainer {
            List {
                VirtualListOutlet()
            }
        }
    }

}
