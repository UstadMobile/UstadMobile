package com.ustadmobile.view.components.virtuallist

import app.cash.paging.PagingSourceLoadResultPage
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Person
import web.cssom.*
import js.objects.jso
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemText
import mui.material.Typography
import react.*
import web.cssom.Contain
import web.cssom.Overflow

val demoPersonList = (0..100).map {
    Person().apply {
        firstNames = "Person"
        lastName = "$it"
        personUid = it.toLong()
    }
}

val demoPagingSource = { ListPagingSource(demoPersonList) }

val VirtualListPreview = FC<Props> {

    val infiniteQueryResult = usePagingSource(
        demoPagingSource, true, 50
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
                Typography.create {
                    +"List Header "
                }
            }

            items(
                list = (0..100).toList(),
                key = { "$it" }
            ) { number ->
                ListItem.create {
                    ListItemText {
                        + "item $number"
                    }
                }
            }

            infiniteQueryItems(
                infiniteQueryResult = infiniteQueryResult,
                dataPagesToItems = { pages ->
                    pages.mapNotNull { it as? PagingSourceLoadResultPage<Int, Person> }.flatMap {
                        it.data
                    }
                },
                itemToKey = { "${it.personUid}" }
            ) { person ->
                ListItem.create {
                    ListItemText {
                        +person?.fullName()
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}

val VirtualListPreviewReverse = FC<Props> {
    val muiAppState = useMuiAppState()

    val infiniteQueryResult = usePagingSource(
        demoPagingSource, true, 50
    )

    VirtualList {
        reverseLayout = true
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.personUid.toString() }
            ) { person ->
                ListItem.create {
                    ListItemText {
                        +person?.fullName()
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}
