package com.ustadmobile.view.components.virtuallist

import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.view.UstadScreenProps
import csstype.*
import js.core.jso
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemText
import mui.material.Typography
import react.*

val demoPersonList = (0..100).map {
    PersonWithDisplayDetails().apply {
        firstNames = "Person"
        lastName = "$it"
        personUid = it.toLong()
    }
}

val demoPagingSource = { ListPagingSource(demoPersonList) }

val VirtualListPreview = FC<UstadScreenProps> {props ->

    val infiniteQueryResult = usePagingSource(
        demoPagingSource, true, 50
    )

    VirtualList {
        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = csstype.Contain.strict
            overflowY = csstype.Overflow.scroll
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
                dataPageToItems = {
                    (it as? LoadResult.Page)?.data ?: listOf()
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
