package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
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
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import react.dom.html.ReactHTML.div
import com.ustadmobile.mui.components.UstadProgressBarWithLabel

external interface ContentEntryDetailAttemptsPersonListProps : Props {
    var uiState: ContentEntryDetailAttemptsPersonListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (PersonAndPictureAndNumAttempts) -> Unit
    var onSortOrderChanged: (SortOrderOption) -> Unit
}

val ContentEntryDetailAttemptsPersonListComponent = FC<ContentEntryDetailAttemptsPersonListProps> { props ->

    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.attemptsPersonList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, PersonAndPictureAndNumAttempts>, Throwable> =
        usePagingSource(
            remoteMediatorResult.pagingSourceFactory, true
        )

    val muiAppState = useMuiAppState()
    val stringsXml = useStringProvider()

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

            if (infiniteQueryResult.isSettledEmpty(remoteMediatorResult)) {
                item("empty_state") {
                    UstadNothingHereYet.create()
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.person.personUid.toString() }
            ) { attemptsPersonListItem ->
                ListItem.create {
                    ListItemButton {
                        onClick = {
                            attemptsPersonListItem?.also { props.onListItemClick(it) }
                        }


                        ListItemIcon {
                            UstadPersonAvatar {
                                pictureUri =
                                    attemptsPersonListItem?.picture?.personPictureThumbnailUri
                                personName =
                                    attemptsPersonListItem?.person?.fullName()
                            }
                        }

                        val personHeaderText = (attemptsPersonListItem?.person?.fullName() ?: "") +
                                ": ${attemptsPersonListItem?.numAttempts} ${stringsXml[MR.strings.attempts]}"

                        ListItemText {
                            primary = ReactNode(personHeaderText)

                            secondary = Stack.create {
                                direction = responsive(StackDirection.column)

                                attemptsPersonListItem?.maxScore?.also { maxScoreVal ->
                                    UstadProgressBarWithLabel {
                                        label = ReactNode(
                                            stringsXml[MR.strings.content_score].capitalizeFirstLetter()
                                        )
                                        progressValue = (maxScoreVal * 100).toInt()
                                    }
                                }

                                attemptsPersonListItem?.maxProgress?.also { maxProgressVal ->
                                    UstadProgressBarWithLabel {
                                        label = ReactNode(
                                            stringsXml[MR.strings.completion_key].capitalizeFirstLetter()
                                        )
                                        progressValue = maxProgressVal
                                    }
                                }
                            }

                            secondaryTypographyProps = jso {
                                component = div
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

val ContentEntryDetailAttemptsPersonListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsPersonListViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsPersonListUiState())

    ContentEntryDetailAttemptsPersonListComponent {
        uiState = uiStateVal
        refreshCommandFlow = viewModel.refreshCommandFlow
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged
    }

}
