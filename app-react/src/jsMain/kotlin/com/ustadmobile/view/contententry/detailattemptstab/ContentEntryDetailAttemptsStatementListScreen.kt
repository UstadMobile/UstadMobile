package com.ustadmobile.view.contententry.detailattemptstab


import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useFormattedDuration
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.icons.material.Check
import mui.icons.material.Star
import mui.icons.material.Timer
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct


external interface ContentEntryDetailAttemptsStatementListProps : Props {
    var uiState: ContentEntryDetailAttemptsStatementListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
}


val ContentEntryDetailAttemptsStatementListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsStatementListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())

    val contentEntryDetailAttemptsStatementListComponent2 =
        FC<ContentEntryDetailAttemptsStatementListProps>
        { props ->

            val theme by useRequiredContext(ThemeContext)

            val remoteMediatorResult = useDoorRemoteMediator(
                pagingSourceFactory = props.uiState.attemptsStatementList,
                refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
            )

            val infiniteQueryResult: UseInfiniteQueryResult
            <PagingSourceLoadResult<Int, StatementEntityAndVerb>, Throwable> = usePagingSource(
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
                        key = { it.statementEntity?.statementLct.toString() }
                    ) { attemptsStatementListItems ->
                        val formattedDuration =
                            attemptsStatementListItems?.statementEntity?.resultDuration?.let { it1 ->
                                useFormattedDuration(
                                    timeInMillis = it1,
                                )
                            }
                        ListItem.create {
                            Stack {
                                direction = responsive(StackDirection.column)

                                sx {

                                    width = 100.pct
                                }
                                ListItemButton {
                                    ListItemIcon {
                                        Check()
                                    }
                                    ListItemText {
                                        primary = ReactNode(
                                            attemptsStatementListItems?.verb?.verbUrlId.toString()
                                                .substringAfterLast("/")
                                                .replaceFirstChar { it.uppercaseChar() }

                                        )
                                    }
                                }
                                if (formattedDuration != null) {
                                    ListItemButton {
                                        ListItemIcon {
                                            Timer
                                            sx {
                                                padding = theme.spacing(1, 1, 1, 5)
                                            }
                                        }
                                        ListItemText {
                                            secondary = ReactNode(
                                                formattedDuration
                                            )
                                        }
                                    }
                                }

                                if (attemptsStatementListItems?.statementEntity?.resultScoreRaw != null || attemptsStatementListItems?.statementEntity?.extensionProgress != null)
                                    ListItemButton {
                                        ListItemIcon {
                                            Star()
                                            sx {
                                                padding = theme.spacing(1, 1, 1, 5)
                                            }
                                        }
                                        ListItemText {
                                            ReactNode(
                                                if (attemptsStatementListItems.statementEntity?.resultScoreRaw != null) {
                                                    "${
                                                        attemptsStatementListItems.statementEntity?.resultScoreRaw?.toInt()
                                                            .toString()
                                                    }/${
                                                        attemptsStatementListItems.statementEntity?.resultScoreMax?.toInt()
                                                            .toString()
                                                    } Score"
                                                } else {
                                                    "${attemptsStatementListItems.statementEntity?.extensionProgress} %Completion"
                                                }
                                            ).also { secondary = it }
                                        }
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


    contentEntryDetailAttemptsStatementListComponent2 {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow

    }


}
