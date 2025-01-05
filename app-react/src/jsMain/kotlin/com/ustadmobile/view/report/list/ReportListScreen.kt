package com.ustadmobile.view.report.list

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.core.viewmodel.report.list.ReportListUiState
import com.ustadmobile.core.viewmodel.report.list.ReportListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndListDisplayDetails
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.view.person.list.PersonListComponent2
import com.ustadmobile.view.person.list.PersonListProps
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

external interface ReportListProps : Props {
    var uiState: ReportListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (Report) -> Unit
    var onClickAddItem: () -> Unit
}

val ReportListComponent2 = FC<ReportListProps> { props ->
    val strings = useStringProvider()

    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.reportList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, Report>, Throwable> =
        usePagingSource(
            remoteMediatorResult.pagingSourceFactory, true, 50
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
                key = { it.reportUid.toString() }
            ) { reportAndDetails ->
                ListItem.create {
                    ListItemButton {
                        onClick = {
                            reportAndDetails?.also { props.onListItemClick(it) }
                        }

                        ListItemText {
                            primary = ReactNode(reportAndDetails?.reportTitle ?: "fggfdfdf")
                        }
                    }
                }
            }
        }
        Container {
            VirtualListOutlet()
        }
    }
}

val ReportListScreen = FC<Props> {
    val location = useLocation()
    val strings = useStringProvider()

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }
    ReportListComponent2 {
        this.uiState = uiState
        onListItemClick = viewModel::onClickEntry
        onClickAddItem = viewModel::onClickAdd
    }
}