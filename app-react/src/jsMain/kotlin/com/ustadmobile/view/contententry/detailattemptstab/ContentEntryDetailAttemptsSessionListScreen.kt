package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useFormattedDuration
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.TimeZone
import mui.icons.material.CalendarToday
import mui.icons.material.Check
import mui.icons.material.Close
import mui.material.Box
import mui.material.Container
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.AlignItems
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.number
import web.cssom.pct
import web.cssom.px

private const val LOAD_SIZE = 50
private const val WIDTH = 100


external interface ContentEntryDetailAttemptsSessionListProps : Props {

    var uiState: ContentEntryDetailAttemptsSessionListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (SessionTimeAndProgressInfo) -> Unit
    var onSortOrderChanged: (SortOrderOption) -> Unit


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
                    remoteMediatorResult.pagingSourceFactory, true, LOAD_SIZE
                )
            val muiAppState = useMuiAppState()
            val stringsXml = useStringProvider()
            val percentageScore=stringsXml[MR.strings.content_score]
            val percentageCompletion=stringsXml[MR.strings.content_percentage_completion]

            val passed=stringsXml[MR.strings.passed]
            val failed=stringsXml[MR.strings.failed]
            val completed=stringsXml[MR.strings.completed]
            val incomplete=stringsXml[MR.strings.incomplete]

            val isSettledEmpty = infiniteQueryResult.isSettledEmpty(remoteMediatorResult)

            VirtualList {
                style = jso {
                    height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                    width = WIDTH.pct
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
                        key = { it.contextRegistrationHi.toString() }
                    ) { attemptsSessionListItems ->
                        val formattedDateAndTime =
                            attemptsSessionListItems?.timeStarted?.let { it1 ->
                                useFormattedDateAndTime(
                                    timeInMillis = it1,
                                    timezoneId = TimeZone.currentSystemDefault().id
                                )
                            }

                        val formattedDuration =
                            attemptsSessionListItems?.resultDuration?.let { it1 ->
                                useFormattedDuration(
                                    timeInMillis = it1,
                                )
                            }

                        ListItem.create {
                            Stack {
                                direction = responsive(StackDirection.column)

                                sx {
                                    width = WIDTH.pct
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

                                            else -> {
                                                Close()
                                            }
                                        }

                                        sx {
                                            minWidth = 40.px
                                            marginRight = 4.px
                                        }
                                    }

                                    ListItemText {
                                        primary = ReactNode(
                                            when {
                                                attemptsSessionListItems?.isSuccessful == true -> {
                                                    if (formattedDuration != null) "$passed - $formattedDuration" else passed
                                                }
                                                attemptsSessionListItems?.isSuccessful == false -> {
                                                    if (formattedDuration != null) "$failed - $formattedDuration" else failed
                                                }
                                                attemptsSessionListItems?.isCompleted == true -> completed
                                                else -> incomplete
                                            }
                                        )
                                    }


                                }
                                if (formattedDateAndTime != null) {
                                    ListItemButton {
                                        ListItemIcon {
                                            CalendarToday()
                                            sx {
                                                padding = theme.spacing(1, 1, 1, 5)
                                            }
                                        }
                                        ListItemText {
                                            secondary = ReactNode(
                                                formattedDateAndTime
                                            )
                                        }
                                    }
                                }

                                if (attemptsSessionListItems?.maxScore != null || attemptsSessionListItems?.maxProgress != null) {
                                    ListItemButton {
                                        Box {
                                            sx {
                                                display = Display.flex
                                                alignItems = AlignItems.center
                                                gap = theme.spacing(2)
                                                width = 100.pct
                                                paddingLeft = theme.spacing(5)
                                            }

                                            val progressValue = when {
                                                attemptsSessionListItems?.maxProgress != null -> {
                                                    (attemptsSessionListItems.maxProgress!!.toFloat() / 100f).coerceIn(0f, 1f)
                                                }
                                                attemptsSessionListItems?.maxScore != null -> {
                                                    attemptsSessionListItems.maxScore!!.toFloat().coerceIn(0f, 1f)
                                                }
                                                else -> 0f
                                            }

                                            LinearProgress {
                                                sx { flexGrow = number(1.0) }
                                                variant = LinearProgressVariant.determinate
                                                value = progressValue * 100
                                            }

                                            Typography {
                                                sx { color = theme.palette.text.secondary }
                                                +(when {
                                                    attemptsSessionListItems?.maxScore != null ->
                                                        "${(attemptsSessionListItems.maxScore!!* 100).toInt()}% $percentageScore"
                                                    attemptsSessionListItems?.maxProgress != null ->
                                                        "${attemptsSessionListItems.maxProgress}% $percentageCompletion"
                                                    else -> "0% $percentageCompletion"
                                                })
                                            }
                                        }
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
        onSortOrderChanged = viewModel::onSortOrderChanged

    }


}
