package com.ustadmobile.view.contententry.detailattemptstab


import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
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
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct


external interface ContentEntryDetailAttemptsStatementListProps: Props {
    var uiState: ContentEntryDetailAttemptsStatementListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
}


val ContentEntryDetailAttemptsStatementListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsStatementListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())

    val contentEntryDetailAttemptsStatementListComponent2 = FC<ContentEntryDetailAttemptsStatementListProps>
    { props ->

        val remoteMediatorResult = useDoorRemoteMediator(
            pagingSourceFactory = props.uiState.attemptsStatementList,
            refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
        )

        val infiniteQueryResult : UseInfiniteQueryResult
        <PagingSourceLoadResult<Int, StatementEntity>, Throwable> = usePagingSource(
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
                    key = { it.statementLct.toString()}
                ) { attemptsStatementListItems ->
                    ListItem.create {
                        ListItemButton{
                            ListItemIcon {
                                UstadBlankIcon()
                            }
                            ListItemText {
                                primary = ReactNode(
                                    attemptsStatementListItems?.resultCompletion?.let {
                                        if (it) "Complete" else "Incomplete"
                                    } ?: "Incomplete"
                                )                            }
                        }
                    }
                }
            }

            Container {
                VirtualListOutlet()
            }
        }
    }


    contentEntryDetailAttemptsStatementListComponent2 {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow

    }


}
