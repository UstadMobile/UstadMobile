package com.ustadmobile.view

import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.PersonListUiState
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import js.core.jso
import kotlinx.coroutines.GlobalScope
import mui.icons.material.AccountCircle
import mui.material.*
import react.FC
import react.ReactNode
import react.create
import tanstack.query.core.QueryKey
import tanstack.react.query.UseInfiniteQueryResult


external interface PersonListProps: UstadScreenProps {
    var uiState: PersonListUiState
    var onClickSort: () -> Unit
    var onListItemClick: (PersonWithDisplayDetails) -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->

    val infiniteQueryResult: UseInfiniteQueryResult<LoadResult<Int, PersonWithDisplayDetails>, Throwable> = usePagingSource(
        props.uiState.personList, QueryKey("PersonList"), GlobalScope, true, 50
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
                    enabled = true
                    onClickSort = {
                        props.onClickSort()
                    }
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it?.personUid?.toString() ?: "" }
            ) { person ->
                ListItem.create {
                    ListItemButton{
                        onClick = {
                            person?.also { props.onListItemClick(it) }
                        }

                        ListItemIcon {
                            AccountCircle()
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
            personList = ListPagingSource(demoPersonList)
        )
    }
}