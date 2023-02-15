package com.ustadmobile.view

import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.PersonListUiState
import com.ustadmobile.door.paging.DoorLoadResult
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.UstadListSortHeader
import csstype.*
import emotion.react.css
import js.core.jso
import kotlinx.coroutines.GlobalScope
import mui.material.*
import mui.system.responsive
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.style
import tanstack.query.core.QueryKey
import tanstack.react.virtual.useVirtualizer
import web.html.HTMLDivElement
import web.html.HTMLElement


external interface PersonListProps: Props {
    var uiState: PersonListUiState
    var onClickSort: () -> Unit
    var onListItemClick: (PersonWithDisplayDetails) -> Unit
    var parentRef: RefObject<HTMLElement>
}

val PersonListComponent2 = FC<PersonListProps> { props ->


    Container {
        maxWidth = "lg"
        val containerRef = useRef<HTMLElement>(null)

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadListSortHeader {
                activeSortOrderOption = props.uiState.sortOption
                enabled = true
                onClickSort = {
                    props.onClickSort()
                }
            }

            val infiniteQueryResult = usePagingSource(
                props.uiState.personList, QueryKey("PersonList"), GlobalScope, true, 50
            )

            // As per
            // https://tanstack.com/virtual/v3/docs/examples/react/infinite-scroll
            // https://codesandbox.io/s/github/tanstack/virtual/tree/beta/examples/react/infinite-scroll?from-embed=&file=/src/main.tsx
            val allRows = infiniteQueryResult.data?.pages?.flatMap {
                (it as? LoadResult.Page)?.data ?: listOf()
            } ?: listOf()

            val virtualizer = useVirtualizer<HTMLElement, HTMLElement>(jso {
                count = if(infiniteQueryResult.hasNextPage) { allRows.size + 1 } else { allRows.size }
                getScrollElement= { containerRef.current }
                estimateSize = { 100 }
                overscan = 5
            })

            useEffect {
                val lastItem = virtualizer.getVirtualItems().lastOrNull()
                if(lastItem != null &&
                    lastItem.index >= allRows.size &&
                    infiniteQueryResult.hasNextPage &&
                    !infiniteQueryResult.isFetchingNextPage
                ) {
                    infiniteQueryResult.fetchNextPage(jso { })
                }
            }


            div {
                ref = containerRef
                style = jso {
                    height = 600.px
                    width = 100.pct
                    overflow = Overflow.scroll
                }

                div {
                    style = jso {
                        height = virtualizer.getTotalSize().px
                        width = 100.pct
                        position = Position.relative
                    }

                    virtualizer.getVirtualItems().forEach { virtualRow ->
                        val person = allRows.getOrNull(virtualRow.index)
                        div {
                            key = "${virtualRow.index}"
                            style = jso {
                                top = 0.px
                                left = 0.px
                                width = 100.pct
                                height = virtualRow.size.px
                                transform = translatey(virtualRow.start.px)
                            }

                            ListItem {
                                ListItemText {
                                    primary = ReactNode(person?.personFullName() ?: "End of list")
                                }
                            }

                        }
                    }
                }
            }


            List{

//                props.uiState.personList.forEach { person ->
//                    ListItem{
//
//                        ListItemButton{
//
//                            onClick = {
//                                props.onListItemClick(person)
//                            }
//
//                            ListItemIcon{
//                                AccountCircle()
//                            }
//
//                            ListItemText {
//                                primary = ReactNode("${person.firstNames} ${person.lastName}")
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}

val demoPersonList = (0..100).map {
    PersonWithDisplayDetails().apply {
        firstNames = "Person"
        lastName = "$it"
        personUid = it.toLong()
    }
}

val PersonListScreenPreview = FC<UstadScreenProps> { props ->
    PersonListComponent2{
        parentRef = props.parentRef
        uiState = PersonListUiState(
//            personList = ListPagingSource(listOf(
//                PersonWithDisplayDetails().apply {
//                    firstNames = "Ahmad"
//                    lastName = "Ahmadi"
//                    admin = true
//                    personUid = 3
//                }
//            ))
            personList = ListPagingSource(demoPersonList)
        )
    }
}