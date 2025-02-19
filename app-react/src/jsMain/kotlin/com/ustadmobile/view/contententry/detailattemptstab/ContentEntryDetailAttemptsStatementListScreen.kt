package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useFormattedDuration
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
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
import mui.icons.material.Check
import mui.icons.material.Close
import mui.icons.material.Star
import mui.icons.material.Timer
import mui.material.Box
import mui.material.Chip
import mui.material.ChipColor
import mui.material.ChipVariant
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
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

private const val LOAD_SIZE = 50
private const val WIDTH = 100

external interface ContentEntryDetailAttemptsStatementListProps : Props {
    var uiState: ContentEntryDetailAttemptsStatementListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onVerbFilterToggled: (String) -> Unit
}

val ContentEntryDetailAttemptsStatementListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsStatementListViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())

    ContentEntryDetailAttemptsStatementListComponent {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow
        onSortOrderChanged = viewModel::onSortOrderChanged
        onVerbFilterToggled = viewModel::onVerbFilterToggled
    }
}

val ContentEntryDetailAttemptsStatementListComponent = FC<ContentEntryDetailAttemptsStatementListProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val stringsXml = useStringProvider()

    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.attemptsStatementList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, StatementEntityAndVerb>, Throwable> =
        usePagingSource(remoteMediatorResult.pagingSourceFactory, true, LOAD_SIZE)

    val muiAppState = useMuiAppState()
    val score = stringsXml[MR.strings.content_score]
    val percentageCompletion = stringsXml[MR.strings.content_percentage_completion]
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
                    onClickSort = props.onSortOrderChanged
                }
            }

            item("verb_filters") {
                Box.create {
                    sx {
                        padding = theme.spacing(1)
                        overflowX = Overflow.scroll
                    }

                    Stack.create {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(1)

                        props.uiState.availableVerbs.distinctBy { it.verbUrlId }.forEach { verb ->
                            verb.verbUrlId?.let { verbId ->
                                val verbName = verbId.substringAfterLast("/")
                                    .replaceFirstChar { it.uppercase() }

                                Chip.create {
                                    key = verbId
                                    label = ReactNode(verbName)
                                    variant = ChipVariant.outlined
                                    color = if (verbId in props.uiState.selectedVerbIds) {
                                        ChipColor.primary
                                    } else {
                                        ChipColor.default
                                    }
                                    onClick = {
                                        props.onVerbFilterToggled(verbId)
                                    }
                                }.also { +it }
                            }
                        }
                    }.also { +it }
                }.also { +it }
            }
            if (isSettledEmpty) {
                item("empty_state") {
                    UstadNothingHereYet.create()
                }
            }

            val FormattedDurationComponent = FC<Props> { props ->
                val duration = props.asDynamic().duration as Long
                val formattedDuration = useFormattedDuration(timeInMillis = duration)
                ListItemText {
                    secondary = ReactNode(formattedDuration)
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it?.statementEntity?.statementLct?.toString() ?: "empty" }
            ) { attemptsStatementListItems ->

                val progress =
                    attemptsStatementListItems?.statementEntity?.extensionProgress?.takeIf { it > 0 }
                        ?.div(100f)
                        ?: attemptsStatementListItems?.statementEntity?.let { entity ->
                            val raw = entity.resultScoreRaw ?: 0f
                            val max = entity.resultScoreMax?.takeIf { it > 0 } ?: 100f
                            (raw / max).coerceIn(0f, 1f)
                        } ?: 0f

                ListItem.create {
                    Stack {
                        direction = responsive(StackDirection.column)
                        spacing = responsive(1)

                        sx {
                            width = WIDTH.pct
                        }

                        ListItemButton {
                            ListItemIcon {
                                when {
                                    attemptsStatementListItems?.statementEntity?.resultScoreRaw != null -> Star()
                                    attemptsStatementListItems?.statementEntity?.extensionProgress != null -> Close()
                                    else -> Check()
                                }
                            }
                            ListItemText {
                                primary = ReactNode(
                                    attemptsStatementListItems?.verb?.verbUrlId?.substringAfterLast(
                                        "/"
                                    )
                                        ?.replaceFirstChar { it.uppercase() } ?: ""
                                )
                            }
                        }

                        ListItemButton {
                            ListItemIcon {
                                Timer()
                                sx {
                                    padding = theme.spacing(1, 1, 1, 5)
                                }
                            }
                            FormattedDurationComponent {
                                this.asDynamic().duration = attemptsStatementListItems?.statementEntity?.resultDuration ?: 0L
                            }
                        }

                        Box.create {
                            sx {
                                padding = theme.spacing(0, 2, 1, 6)
                                width = 100.pct
                            }

                            Stack.create {
                                direction = responsive(StackDirection.row)
                                spacing = responsive(2)
                                sx {
                                    alignItems = AlignItems.center
                                }

                                LinearProgress.create {
                                    variant = LinearProgressVariant.determinate
                                    value = (progress * 100).toInt()
                                    sx {
                                        width = 70.pct
                                    }
                                }.also { +it }

                                Typography.create {
                                    +(if (attemptsStatementListItems?.statementEntity?.resultScoreRaw != null) {
                                        "$score: ${(progress * 100).toInt()}%"
                                    } else {
                                        "$percentageCompletion: ${(progress * 100).toInt()}%"
                                    })
                                }.also { +it }
                            }.also { +it }
                        }.also { +it }
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}