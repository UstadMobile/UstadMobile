package com.ustadmobile.view.person.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
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
    var onListItemClick: (PersonWithDisplayDetails) -> Unit
    var onClickAddItem: () -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->
    val strings = useStringsXml()

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
                        text = strings[MessageID.add_a_new_person]
                        onClickAdd = props.onClickAddItem
                    }
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.personUid.toString() }
            ) { person ->
                ListItem.create {
                    ListItemButton{
                        onClick = {
                            person?.also { props.onListItemClick(it) }
                        }

                        ListItemIcon {
                            UstadPersonAvatar {
                                personUid = person?.personUid ?: 0
                            }
                        }

                        ListItemText {
                            primary = ReactNode("${person?.firstNames} ${person?.lastName}")
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
    PersonWithDisplayDetails().apply {
        firstNames = "Person"
        lastName = "$it"
        personUid = it.toLong()
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
