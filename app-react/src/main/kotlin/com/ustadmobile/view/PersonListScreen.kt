package com.ustadmobile.view

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.PersonListUiState
import com.ustadmobile.core.viewmodel.PersonListViewModel
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
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
import mui.icons.material.AccountCircle
import mui.material.*
import react.FC
import react.ReactNode
import react.create


external interface PersonListProps: UstadScreenProps {
    var uiState: PersonListUiState
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onListItemClick: (PersonWithDisplayDetails) -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->
    val infiniteQueryResult = usePagingSource(
        props.uiState.personList, true, 50
    )

    VirtualList {
        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight}px)".unsafeCast<Height>()
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

val PersonListScreenPreview = FC<UstadScreenProps> { props ->
    PersonListComponent2 {
        + props
        uiState = PersonListUiState(
            personList = { ListPagingSource(demoPersonList) }
        )
    }
}

val PersonListScreen = FC<UstadScreenProps> { props ->
    val viewModel = useUstadViewModel(
        onAppUiStateChange = props.onAppUiStateChanged
    ) { di, savedSateHandle ->
        console.log("Creating PersonListviewModel")
        PersonListViewModel(di, savedSateHandle)
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
        + props
    }


}
