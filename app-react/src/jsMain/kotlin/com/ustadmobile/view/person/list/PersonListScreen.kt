package com.ustadmobile.view.person.list

import app.cash.paging.PagingSourceLoadResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.composites.PersonAndListDisplayDetails
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation
import tanstack.react.query.UseInfiniteQueryResult
import mui.icons.material.Link as LinkIcon
import mui.icons.material.CopyAll as CopyAllIcon
import mui.icons.material.PersonAdd as PersonAddIcon
import mui.icons.material.GroupAdd as GroupAddIcon


external interface PersonListProps: Props {
    var uiState: PersonListUiState
    var refreshCommandFlow: Flow<RefreshCommand> ?
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onListItemClick: (Person) -> Unit
    var onClickAddItem: () -> Unit
    var onClickInviteWithLink: () -> Unit
    var onClickCopyInviteCode: () -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->
    val strings = useStringProvider()

    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.personList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )

    val infiniteQueryResult : UseInfiniteQueryResult<PagingSourceLoadResult<Int, PersonAndListDisplayDetails>, Throwable> = usePagingSource(
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

            if(props.uiState.showAddItem) {
                item("add_new_person") {
                    UstadAddListItem.create {
                        text = strings[MR.strings.add_a_new_person]
                        onClickAdd = props.onClickAddItem
                    }
                }
            }

            props.uiState.inviteCode?.also { inviteCode ->
                item("copy_invite_code") {
                    ListItem.create {
                        ListItemButton {
                            id = "copy_invite_code_button"
                            onClick = {
                                props.onClickCopyInviteCode()
                            }

                            ListItemIcon {
                                CopyAllIcon()
                            }

                            ListItemText {
                                primary = ReactNode(strings[MR.strings.copy_invite_code])
                            }
                        }
                    }
                }
            }

            if(props.uiState.showInviteViaLink) {
                item("invite_with_link") {
                    ListItem.create {
                        ListItemButton {
                            id = "invite_with_link_button"
                            onClick = {
                                props.onClickInviteWithLink()
                            }

                            ListItemIcon {
                                LinkIcon()
                            }

                            ListItemText {
                                primary = ReactNode(strings[MR.strings.invite_with_link])
                            }
                        }
                    }
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.person?.personUid?.toString() ?: "0" }
            ) { personAndDetails ->
                ListItem.create {
                    ListItemButton{
                        onClick = {
                            personAndDetails?.person?.also { props.onListItemClick(it) }
                        }

                        ListItemIcon {
                            UstadPersonAvatar {
                                pictureUri = personAndDetails?.picture?.personPictureThumbnailUri
                                personName = personAndDetails?.person?.fullName()
                            }
                        }

                        ListItemText {
                            primary = ReactNode(personAndDetails?.person?.fullName() ?: "")
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

val demoPersonList = (0..150).map {
    PersonAndListDisplayDetails().apply {
        person = Person().apply {
            firstNames = "Person"
            lastName = "$it"
            personUid = it.toLong()
        }
    }
}

val PersonListScreenPreview = FC<Props> { props ->
    PersonListComponent2 {
        + props
        uiState = PersonListUiState(
            personList = { ListPagingSource(demoPersonList) }
        )
    }
}

val PersonListScreen = FC<Props> {
    val location = useLocation()
    val strings = useStringProvider()

    val viewModel = useUstadViewModel {di, savedStateHandle ->
        PersonListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }


    Dialog {
        open = uiState.addSheetOrDialogVisible

        onClose = { _, _ ->
            viewModel.onDismissAddSheetOrDialog()
        }

        List {
            ListItem {
                ListItemButton {
                    id = "add_person_button"
                    onClick = {
                        viewModel.onClickAdd()
                    }

                    ListItemIcon {
                        PersonAddIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.add_person])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    id = "bulk_import_button"

                    onClick = {
                        viewModel.onClickBulkAdd()
                    }

                    ListItemIcon {
                        GroupAddIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.bulk_import])
                    }
                }
            }
        }
    }

    PersonListComponent2 {
        this.uiState = uiState
        refreshCommandFlow = viewModel.refreshCommandFlow
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged
        onClickAddItem = viewModel::onClickAdd
        onClickInviteWithLink = viewModel::onClickInviteWithLink
        onClickCopyInviteCode = viewModel::onClickCopyInviteCode
    }


}
