package com.ustadmobile.view.person.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
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
import js.core.jso
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation


external interface PersonListProps: Props {
    var uiState: PersonListUiState
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onListItemClick: (Person) -> Unit
    var onClickAddItem: () -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->
    val strings = useStringProvider()

    val infiniteQueryResult = usePagingSource(
        props.uiState.personList, true, 50
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
            item {
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
                item {
                    UstadAddListItem.create {
                        text = strings[MR.strings.add_a_new_person]
                        onClickAdd = props.onClickAddItem
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
                                personUid = personAndDetails?.person?.personUid ?: 0
                                pictureUri = personAndDetails?.picture?.personPictureUri
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
    val viewModel = useUstadViewModel {di, savedStateHandle ->
        PersonListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    PersonListComponent2 {
        this.uiState = uiState
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged
        onClickAddItem = viewModel::onClickAdd
    }


}
