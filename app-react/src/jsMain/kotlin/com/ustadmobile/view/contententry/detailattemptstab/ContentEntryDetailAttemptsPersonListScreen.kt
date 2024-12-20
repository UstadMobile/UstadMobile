package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.hooks.collectAsState
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
import web.cssom.AlignContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.VerticalAlign
import web.cssom.pct
import web.cssom.px


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
            println("remoteMediatorResult: $remoteMediatorResult")

            val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, PersonAndPictureAndNumAttempts>, Throwable> =
                usePagingSource(
                    remoteMediatorResult.pagingSourceFactory, true, 150
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
                                                "${attemptsPersonListItems?.numAttempts.toString()} attempts"
                                            )
                                        }


                                    }
                                    if(attemptsPersonListItems?.maxScore!=null || attemptsPersonListItems?.maxProgress!=null) {
                                        Stack {
                                            direction = responsive(StackDirection.row)
                                            LinearProgress {
                                                sx {
                                                    width = 500.px
                                                    height = 4.px

                                                }
                                                variant = LinearProgressVariant.determinate
                                                value =
                                                    attemptsPersonListItems.maxProgress
                                                        ?: (attemptsPersonListItems.maxScore?.times(
                                                            100
                                                        ) ?: 0)
                                            }
                                            ListItemText {
                                                primary = ReactNode(
                                                    attemptsPersonListItems.maxProgress?.let {
                                                        "${(it)}% Completion"
                                                    }
                                                        ?: "${((attemptsPersonListItems.maxScore ?: 0f) * 100).toInt()}% Score"
                                                )
                                                sx {
                                                    verticalAlign = VerticalAlign.middle
                                                    marginLeft = 8.px
                                                    paddingTop = 1.px
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
