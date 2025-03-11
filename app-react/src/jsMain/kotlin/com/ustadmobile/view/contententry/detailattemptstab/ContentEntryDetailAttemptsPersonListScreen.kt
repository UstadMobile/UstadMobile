package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import web.cssom.FlexDirection
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.TextAlign
import web.cssom.number
import web.cssom.pct
import web.cssom.px

private const val LOAD_SIZE = 150
private const val LIST_HEIGHT = 100
private const val LINEAR_PROGRESS_WIDTH = 500
private const val LINEAR_PROGRESS_HEIGHT = 4
private const val PADDING_TOP = 1
private const val MARGIN_LEFT = 8



external interface ContentEntryDetailAttemptsPersonListProps : Props {
    var uiState: ContentEntryDetailAttemptsPersonListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (PersonAndPictureAndNumAttempts) -> Unit
    var onSortOrderChanged: (SortOrderOption) -> Unit
}

val ContentEntryDetailAttemptsPersonListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsPersonListViewModel(di, savedStateHandle)
    }
    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsPersonListUiState())

    val contentEntryDetailAttemptsPersonListComponent2 =
        FC<ContentEntryDetailAttemptsPersonListProps>
        { props ->

            val remoteMediatorResult = useDoorRemoteMediator(
                pagingSourceFactory = props.uiState.attemptsPersonList,
                refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
            )

            val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, PersonAndPictureAndNumAttempts>, Throwable> =
                usePagingSource(
                    remoteMediatorResult.pagingSourceFactory, true, LOAD_SIZE
                )

            val muiAppState = useMuiAppState()
            val stringsXml = useStringProvider()
            val attempts = stringsXml[MR.strings.attempts]
            val percentageCompletion = stringsXml[MR.strings.content_percentage_completion]
            val percentageScore = stringsXml[MR.strings.content_score]
            val isSettledEmpty = infiniteQueryResult.isSettledEmpty(remoteMediatorResult)
            val theme by useRequiredContext(ThemeContext)

            VirtualList {
                style = jso {
                    height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                    width = LIST_HEIGHT.pct
                    contain = Contain.strict
                    overflowY = Overflow.scroll
                }
                content =
                    virtualListContent {
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
                            key = { it.person.personUid.toString() }
                        ) { attemptsPersonListItems ->
                            ListItem.create {
                                Stack {
                                    direction = responsive(StackDirection.column)
                                    ListItemButton {
                                        onClick = {
                                            attemptsPersonListItems?.also { props.onListItemClick(it) }
                                        }

                                        ListItemIcon {
                                            UstadPersonAvatar {
                                                pictureUri =
                                                    attemptsPersonListItems?.picture?.personPictureThumbnailUri
                                                personName =
                                                    attemptsPersonListItems?.person?.fullName()
                                            }
                                        }
                                        ListItemText {
                                            primary =
                                                ReactNode(
                                                    attemptsPersonListItems?.person?.fullName()
                                                        ?: ""
                                                )
                                            secondary = ReactNode(
                                                "$attempts: ${attemptsPersonListItems?.numAttempts.toString()}" ?: "$attempts: 0"
                                                    ?: "0 attempts"
                                            )
                                        }


                                    }
                                    if (attemptsPersonListItems?.maxScore != null || attemptsPersonListItems?.maxProgress != null) {
                                        ListItemButton {
                                            Box {
                                                sx {
                                                    display = Display.flex
                                                    flexDirection = FlexDirection.column
                                                    gap = theme.spacing(2)
                                                    width = 100.pct
                                                    paddingLeft = theme.spacing(5)
                                                }

                                                if (attemptsPersonListItems.maxProgress!=null)
                                                {
                                                    Box {
                                                        sx {
                                                            display = Display.flex
                                                            alignItems = AlignItems.center
                                                            gap = theme.spacing(2)
                                                            width = LINEAR_PROGRESS_WIDTH.px
                                                        }

                                                        LinearProgress {
                                                            sx {
                                                                flexGrow = number(1.0)
                                                                height = LINEAR_PROGRESS_HEIGHT.px
                                                            }
                                                            variant = LinearProgressVariant.determinate
                                                            value = (attemptsPersonListItems.maxProgress?.toFloat() ?: 0f).coerceIn(0f, 100f)
                                                        }

                                                        Typography {
                                                            sx {
                                                                color = theme.palette.text.secondary
                                                                marginLeft = MARGIN_LEFT.px
                                                                width = 80.px
                                                                textAlign = TextAlign.end
                                                            }
                                                            +"${(attemptsPersonListItems.maxProgress ?: 0f).toInt()}% $percentageCompletion"
                                                        }
                                                    }
                                                }

                                                if (attemptsPersonListItems.maxScore!=null )  {
                                                    Box {
                                                        sx {
                                                            display = Display.flex
                                                            alignItems = AlignItems.center
                                                            gap = theme.spacing(2)
                                                            width = LINEAR_PROGRESS_WIDTH.px
                                                            marginTop = PADDING_TOP.px
                                                        }

                                                        LinearProgress {
                                                            sx {
                                                                flexGrow = number(1.0)
                                                                height = LINEAR_PROGRESS_HEIGHT.px
                                                            }
                                                            variant = LinearProgressVariant.determinate
                                                            value = ((attemptsPersonListItems.maxScore ?: 0f) * 100).coerceIn(0f, 100f)
                                                        }

                                                        Typography {
                                                            sx {
                                                                color = theme.palette.text.secondary
                                                                marginLeft = MARGIN_LEFT.px
                                                                width = 80.px
                                                                textAlign = TextAlign.end
                                                            }
                                                            +"${((attemptsPersonListItems.maxScore ?: 0f) * 100).toInt()}% $percentageScore"
                                                        }
                                                    }
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

    contentEntryDetailAttemptsPersonListComponent2 {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged


    }


}
