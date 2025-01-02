package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.Container
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
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
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.AlignItems
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.VerticalAlign
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

            VirtualList {
                style = jso {
                    height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                    width = LIST_HEIGHT.pct
                    contain = Contain.strict
                    overflowY = Overflow.scroll
                }
                content =
                    virtualListContent {
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
                                                "${attemptsPersonListItems?.numAttempts.toString()} $attempts"
                                            )
                                        }


                                    }
                                    if (attemptsPersonListItems?.maxScore != null || attemptsPersonListItems?.maxProgress != null) {
                                        Stack {
                                            direction = responsive(StackDirection.row) // Ensure horizontal layout
                                            sx {
                                                verticalAlign = VerticalAlign.middle // Align stack elements in the middle
                                                marginLeft = MARGIN_LEFT.px
                                                paddingTop = PADDING_TOP.px
                                                alignItems = AlignItems.center // This ensures vertical centering of all stack children
                                            }
                                            LinearProgress {
                                                sx {
                                                    width = LINEAR_PROGRESS_WIDTH.px
                                                    height = LINEAR_PROGRESS_HEIGHT.px
                                                }
                                                variant = LinearProgressVariant.determinate
                                                value = attemptsPersonListItems.maxProgress
                                                    ?: (attemptsPersonListItems.maxScore?.times(100) ?: 0)
                                            }
                                            ListItemText {
                                                primary = ReactNode(
                                                    attemptsPersonListItems.maxProgress?.let {
                                                        "${it}% $percentageCompletion"
                                                    } ?: "${((attemptsPersonListItems.maxScore ?: 0f) * 100).toInt()}% $percentageScore"
                                                )
                                                sx {
                                                    verticalAlign = VerticalAlign.middle // Ensure vertical alignment within the item
                                                    marginLeft = MARGIN_LEFT.px
                                                    paddingTop = PADDING_TOP.px
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

    }


}
