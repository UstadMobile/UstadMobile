package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import kotlinx.browser.window
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.HTMLElement
import org.w3c.dom.ScrollToOptions
import react.*
import react.dom.onChange
import tanstack.virtual.core.elementScroll
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

private val headerList = (0..50).map {
    "Dashboard"
}

val CourseDetailProgressScreenPreview = FC<UstadScreenProps> { props ->

    CourseDetailProgressScreenComponent2 {

        + props

        uiState = CourseDetailProgressUiState(
            students = { ListPagingSource(personList) },
            results = headerList
        )
    }
}


val CourseDetailProgressScreenComponent2 = FC<CourseDetailProgressProps> { props ->

    val strings = useStringsXml()
    val infiniteQueryResult = usePagingSource(
        props.uiState.students, true, 50
    )

    var scrollX by useState { 0 }

    val (selectedItems, setSelectedItems) = useState(setOf<Int>())

    Container {
        Stack {
            direction = responsive(StackDirection.row)

            sx {
                width = 100.pct
                overflowX = Overflow.hidden
            }
            props.uiState.results.forEachIndexed { index, item ->
                ListItem {

                    sx {
                        if (scrollX > index){
                            position = Position.absolute
                            marginLeft = -(44*index).px
                        }
                        width = 44.px
                        transform = rotate(270.deg)
                    }

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
                position = Position.absolute
                marginLeft = 0.px
                marginTop = 100.px
        }

        content = virtualListContent {



            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.personUid.toString() }
            ) { person ->
                Stack.create {
                    direction = responsive(StackDirection.row)
//                    sx {
//                        position = Position.absolute
//                        marginLeft = 0.px
//                    }

                    ListItemButton{

                        sx {
                            position = Position.sticky
                            marginLeft = 0.px
                        }
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

                    Box {
                        sx {
                            display = Display.flex
                            flexDirection = FlexDirection.row
//                            overflowX = Overflow.scroll
//                                position = Position.absolute
//                            width = 300.px
//                                marginLeft = 120.px
                        }



                        props.uiState.results.forEachIndexed { index, item ->
                            ListItem {

                                sx {
                                    width = 44.px
                                    if (scrollX > index){
                                        position = Position.absolute
                                        marginLeft = -(44*index).px
                                        color = Color("000000")
                                    }
                                }

                                ListItemIcon {

                                    + CheckBoxOutlined.create()
                                }
                            }
                        }
                    }
                }
            }
        }


        Container {

            Box {
                sx {
                    width = 100.pct
                    overflowX = Overflow.scroll
                }
                onScroll = { event ->
                    scrollX = event.target.unsafeCast<HTMLElement>().scrollLeft.toInt()
                }
            }

            VirtualListOutlet()
        }
    }
}