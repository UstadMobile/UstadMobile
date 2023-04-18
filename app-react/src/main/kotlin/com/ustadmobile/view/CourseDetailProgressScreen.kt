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
//            overflowX = Overflow.hidden
//                marginLeft = -scrollX.px
//                display = Display.flex
//                alignItems = AlignItems.flexStart
//                justifyContent = JustifyContent.flexStart
            }

            Box {
                sx {
                    width = 200.px
                }
            }

            props.uiState.results.forEachIndexed { index, item ->

                Typography {
                    sx {
                        width = 44.px
                        transform = rotate(270.deg)
                        if (scrollX > index){
                            position = Position.absolute
                            marginLeft = -(44*index).px
                            color = Color("000000")
                        }
                    }

                    + item
                }
            }
        }
    }


    VirtualList {

        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
            width = 100.pct
//            contain = Contain.strict
            overflowY = Overflow.scroll
//            display = Display.flex
//            alignItems = AlignItems.flexStart
//            position = Position.absolute
//            marginLeft = 0.px
//            marginTop = 100.px
        }



        content = virtualListContent {

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.personUid.toString() }
            ) { person ->

                Stack.create {
                    direction = responsive(StackDirection.row)

                    Box{

                        sx {
                            position = Position.absolute
                            transform = translatex(scrollX.px)
                        }
                        onClick = {
                            person?.also { props.onClickStudent(it) }
                        }

                        UstadPersonAvatar {
                            personUid = person?.personUid ?: 0
                        }

                        Typography {
                            + "${person?.fullName()}"
                        }

                    }

                    (0..60).forEachIndexed { index, item ->
                        Typography {
                            sx {
                                width = 60.px
//                                if (scrollX > index){
//                                    position = Position.absolute
//                                    marginLeft = -(44*index).px
//                                    color = Color("000000")
//                                }
                            }
                            + "Text $index"
                        }
//                        Icon {
//
//                            sx {
//                                width = 60.px
//                                if (scrollX > index){
//                                    position = Position.absolute
//                                    marginLeft = -(44*index).px
//                                    color = Color("000000")
//                                }
//                            }
//
//                            + CheckBoxOutlined.create()
//                        }
                    }
                }
            }
        }


        Container {
            sx {
                width = 100.pct
                overflowX = Overflow.scroll
                display =  Display.flex
                alignItems = AlignItems.flexStart
            }
            onScroll = { event ->
                scrollX = event.target.unsafeCast<HTMLElement>().scrollLeft.toInt()
            }
            Box {


                VirtualListOutlet()
            }
        }
    }
}