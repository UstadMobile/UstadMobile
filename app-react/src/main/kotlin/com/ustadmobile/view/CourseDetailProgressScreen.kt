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

    var headerIndex by useState { 0 }

    val (selectedItems, setSelectedItems) = useState(setOf<Int>())

    Container {
        maxWidth = "lg"

//        sx {
//            height = "calc(100vh - ${props.muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
//            width = 100.pct
//        }


        Box {
            sx {
                overflowX = Overflow.scroll
//                width = 400.px
                display = Display.flex
                flexDirection = FlexDirection.row
//                transform = rotate(270.deg)
            }
            onScroll= { event ->

                headerIndex = 20
//                sx {
//                    position = Position.absolute
//                    marginLeft = (120-90*headerIndex).px
//                    color = Color("ff0000")
//                }
            }

            props.uiState.results.forEachIndexed { index, item ->
                ListItem {

                    sx {
                        if (headerIndex > index){
                            position = Position.absolute
                            marginLeft = -(90*index).px
                        }
                        width = 90.px
                        transform = rotate(270.deg)
                    }

                    onClick = {
                        headerIndex = index
                    }
                    ListItemText {
                        primary = ReactNode(item)
                    }
                }
            }
        }

        Typography {
            + "$headerIndex"
        }

        VirtualList {
            style = jso {
                height = "calc(100vh - ${props.muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
                width = 100.pct
                contain = Contain.strict
                overflowY = Overflow.scroll
//                position = Position.absolute
//                marginLeft = 0.px
//                marginTop = 100.px
            }

            content = virtualListContent {



                infiniteQueryPagingItems(
                    items = infiniteQueryResult,
                    key = { it.personUid.toString() }
                ) { person ->
                    ListItem.create {


                        ListItemButton{
                                sx {
//                                position = Position.absolute
//                                marginLeft = 0.px
//                                height = 190.px
//                                marginTop = 0.px
                                    width = 120.px
//                                backgroundColor = Color("#FFFFFF")
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

                        secondaryAction = List.create {
                            sx {
                                display = Display.flex
                                flexDirection = FlexDirection.row
                                overflowX = Overflow.scroll
                                position = Position.absolute
//                                width = 320.px
                                marginLeft = 120.px
                            }

                            onScroll = { event ->
                                headerIndex = event.currentTarget.scrollLeft.toInt()
                            }

                            props.uiState.results.forEachIndexed { index, item ->
                                ListItem {

                                    sx {
                                        if (headerIndex > index){
                                            position = Position.absolute
                                            marginLeft = (120-90*index).px
                                            color = Color("000000")
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
}