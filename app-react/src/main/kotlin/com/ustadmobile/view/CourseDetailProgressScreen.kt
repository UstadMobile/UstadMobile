package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.Header
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import kotlinx.css.select
import mui.material.*
import mui.material.List
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.aria.AriaOrientation
import react.dom.aria.AriaRole
import react.dom.aria.ariaOrientation
import react.dom.html.ReactHTML.div
import react.dom.onChange
import tanstack.virtual.core.windowScroll

external interface CourseDetailProgressProps : UstadScreenProps {

    var uiState: CourseDetailProgressUiState

    var onClickStudent: (Person) -> Unit

}

private val personList = (0..150).map {
    PersonWithDisplayDetails().apply {
        firstNames = "Person"
        lastName = "$it"
        personUid = it.toLong()
    }
}

val CourseDetailProgressScreenPreview = FC<UstadScreenProps> { props ->

    val strings = useStringsXml()

    CourseDetailProgressScreenComponent2 {

        + props

        uiState = CourseDetailProgressUiState(
            students = { ListPagingSource(personList) },
            results = listOf(
                strings[MessageID.discussion_board],
                strings[MessageID.dashboard],
                strings[MessageID.module],
                strings[MessageID.assignments],
                strings[MessageID.document]+"6",
                strings[MessageID.audio],
                strings[MessageID.phone],
                strings[MessageID.change_photo],
                strings[MessageID.ebook],
                strings[MessageID.discussion_board],
                strings[MessageID.dashboard],
                strings[MessageID.module],
                strings[MessageID.assignments],
                strings[MessageID.document],
                strings[MessageID.audio],
                strings[MessageID.phone],
                strings[MessageID.change_photo],
                strings[MessageID.ebook],
                strings[MessageID.assignments],
                strings[MessageID.document],
                strings[MessageID.audio],
                strings[MessageID.phone],
                strings[MessageID.change_photo],
                strings[MessageID.ebook],
                strings[MessageID.video],
                strings[MessageID.assignments],
                strings[MessageID.document],
                strings[MessageID.audio],
                strings[MessageID.phone],
                strings[MessageID.change_photo],
                strings[MessageID.ebook],
            )
        )
    }
}


val CourseDetailProgressScreenComponent2 = FC<CourseDetailProgressProps> { props ->

    val strings = useStringsXml()
    val infiniteQueryResult = usePagingSource(
        props.uiState.students, true, 50
    )

    val (selectedItems, setSelectedItems) = useState(setOf<Int>())

    List {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.row
            overflowX = Overflow.scroll
//                        position = Position.fixed
////                    transform = rotate(270.deg)
        }

        props.uiState.results.forEachIndexed { index, item ->
            ListItem {
                selected = selectedItems.contains(index)
                onClick = {
                    if (selectedItems.contains(index)) {
                        setSelectedItems(selectedItems - index)
                    } else {
                        setSelectedItems(selectedItems + index)
                    }
                }
                ListItemButton {


                    ListItemText {
                        primary = ReactNode(item)
                    }
                }
            }
        }
    }

    VirtualList {
        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
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
                    ListItemButton{
                        onClick = {
                            person?.also { props.onClickStudent(it) }
                        }

                        ListItemIcon {
                            UstadPersonAvatar {
                                personUid = person?.personUid ?: 0
                            }
                        }

                        ListItemText {
                            primary = ReactNode("${person?.fullName()}")
                        }

                    }

                    secondaryAction = List.create {
                        sx {
                            display = Display.flex
                            flexDirection = FlexDirection.row
//                            overflowX = Overflow.scroll
                            width = 400.px
                        }

                        props.uiState.results.forEachIndexed { index, item ->
                            ListItem {
                                selected = selectedItems.contains(index)
                                onClick = {
                                    if (selectedItems.contains(index)) {
                                        setSelectedItems(selectedItems - index)
                                    } else {
                                        setSelectedItems(selectedItems + index)
                                    }
                                }
                                ListItemButton {


                                    ListItemText {
                                        primary = ReactNode(item)
                                    }
                                }
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