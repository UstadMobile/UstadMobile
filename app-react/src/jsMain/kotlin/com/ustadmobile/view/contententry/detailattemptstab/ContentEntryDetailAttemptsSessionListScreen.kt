package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.contentformats.epub.ncx.Text
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import dev.icerock.moko.graphics.parseColor
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.icons.material.Check
import mui.icons.material.Close
import mui.icons.material.Schedule
import mui.icons.material.Star
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Color
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px


external interface ContentEntryDetailAttemptsSessionListProps : Props {

    var uiState: ContentEntryDetailAttemptsSessionListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (SessionTimeAndProgressInfo) -> Unit

}


val ContentEntryDetailAttemptsSessionListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsSessionListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsSessionListUiState())

    val contentEntryDetailAttemptsSessionListComponent2 =
        FC<ContentEntryDetailAttemptsSessionListProps>
        { props ->
            val theme by useRequiredContext(ThemeContext)

            val remoteMediatorResult = useDoorRemoteMediator(
                pagingSourceFactory = props.uiState.attemptsSessionList,
                refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
            )

            val infiniteQueryResult: UseInfiniteQueryResult
            <PagingSourceLoadResult<Int, SessionTimeAndProgressInfo>, Throwable> =
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
                        key = { it.contextRegistrationHi.toString() }
                    ) { attemptsSessionListItems ->

                        ListItem.create {

                            Stack {
                                direction = responsive(StackDirection.column)

                                sx {

                                    width = 100.pct
                                }



                                ListItemButton {
                                    onClick = {
                                        attemptsSessionListItems?.also { props.onListItemClick(it) }
                                    }
                                    ListItemIcon {
                                        when {
                                            attemptsSessionListItems?.isSuccessful == true || attemptsSessionListItems?.isCompleted == true -> {
                                                Check()
                                            }

                                            attemptsSessionListItems?.isSuccessful == false || attemptsSessionListItems?.isCompleted == false -> {
                                                Close()
                                            }

                                            else -> {
                                                Close()
                                            }
                                        }

                                    }

                                    ListItemText {
                                        primary =
                                            ReactNode(
                                                when {
                                                    attemptsSessionListItems?.isSuccessful == true -> "Passed"
                                                    attemptsSessionListItems?.isSuccessful == false -> "Failed"
                                                    attemptsSessionListItems?.isCompleted == true -> "Completed"
                                                    else -> "Incomplete"
                                                }
                                            )

                                    }


                                }
                                ListItemButton {
                                    ListItemIcon {

                                        Star()
                                        sx {
                                            padding = theme.spacing(1, 1, 1, 5)
                                        }
                                    }
                                    ListItemText {
                                        secondary = ReactNode(
                                            when {
                                                attemptsSessionListItems?.maxScore != null -> {
                                                    "${((attemptsSessionListItems?.maxScore ?: 0f)
                                                            * 100).toInt()}" + "% Score"
                                                }

                                                else -> {
                                                    "${attemptsSessionListItems?.maxProgress}% Completion"
                                                }
                                            }
                                        )
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


    contentEntryDetailAttemptsSessionListComponent2 {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow
        onListItemClick = viewModel::onClickEntry

    }


}
