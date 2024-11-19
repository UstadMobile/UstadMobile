package com.ustadmobile.view.contententry.detailattemptstab

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndAttemptInfo
import com.ustadmobile.mui.components.UstadAddListItem
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
import react.FC
import react.Props
import react.ReactNode
import react.create
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct


external interface ContentEntryDetailAttemptsPersonListProps: Props {
    var uiState: ContentEntryDetailAttemptsPersonListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (PersonAndAttemptInfo) -> Unit

}
val ContentEntryDetailAttemptsPersonListScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailAttemptsPersonListViewModel(di, savedStateHandle)
    }
    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsPersonListUiState())

    val contentEntryDetailAttemptsPersonListComponent2 = FC<ContentEntryDetailAttemptsPersonListProps>
    { props ->

        val remoteMediatorResult = useDoorRemoteMediator(
            pagingSourceFactory = props.uiState.attemptsPersonList,
            refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
        )
        println("remoteMediatorResult: $remoteMediatorResult")

        val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, PersonAndAttemptInfo>, Throwable> = usePagingSource(
            remoteMediatorResult.pagingSourceFactory, true, 150
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
                    key = { it.person?.personUid?.toString() ?: "0" }
                ) { attemptsPersonListItems ->
                    ListItem.create {
                        ListItemButton{
                            onClick = {
                                attemptsPersonListItems?.also { props.onListItemClick(it) }
                            }
                            ListItemIcon {
                                UstadPersonAvatar {
                                    pictureUri = attemptsPersonListItems?.personPicture?.personPictureThumbnailUri
                                    personName = attemptsPersonListItems?.person?.fullName()
                                }
                            }
                            ListItemText {
                                primary = ReactNode(attemptsPersonListItems?.person?.fullName()?:"")
                            }
                        }
                    }
                }?: run {
                    println("No person data found")
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
