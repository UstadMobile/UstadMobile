package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.Container
import react.FC
import react.Props
import react.create
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

external interface ContentEntryDetailAttemptsSessionListProps : Props {

    var uiState: ContentEntryDetailAttemptsSessionListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (SessionTimeAndProgressInfo) -> Unit
    var onSortOrderChanged: (SortOrderOption) -> Unit

}

val ContentEntryDetailAttemptsSessionListComponent2 = FC<ContentEntryDetailAttemptsSessionListProps> { props ->
    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.attemptsSessionList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult: UseInfiniteQueryResult
    <PagingSourceLoadResult<Int, SessionTimeAndProgressInfo>, Throwable> =
        usePagingSource(
            remoteMediatorResult.pagingSourceFactory, true,
        )

    val muiAppState = useMuiAppState()
    val isSettledEmpty = infiniteQueryResult.isSettledEmpty(remoteMediatorResult)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item("sort_list_opts") {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    sortOptions = props.uiState.sortOptions
                    enabled = true
                    onClickSort = {
                        props.onSortOrderChanged(it)
                    }
                }
            }

            if (isSettledEmpty) {
                item("empty_state") {
                    UstadNothingHereYet.create()
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { "${it.contextRegistrationHi}-${it.contextRegistrationLo}" }
            ) { item ->
                ContentEntryDetailAttemptsListSessionListItem.create {
                    sessionTimeAndProgressInfo = item
                    onClick = {
                        item?.also { props.onListItemClick(it) }
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}

val ContentEntryDetailAttemptsSessionListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsSessionListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsSessionListUiState())

    ContentEntryDetailAttemptsSessionListComponent2 {
        uiState = uiStateVal
        refreshCommandFlow = viewModel.refreshCommandFlow
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged
    }

}
