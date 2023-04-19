package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.core.viewmodel.PersonWithResults
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Overflow
import csstype.pct
import csstype.rotate
import csstype.deg
import csstype.px
import csstype.Color
import csstype.unaryMinus
import csstype.Contain
import csstype.translatex
import csstype.Position
import csstype.Height
import csstype.Display
import csstype.AlignItems
import js.core.jso
import mui.icons.material.CheckBoxOutlined
import mui.icons.material.Message
import mui.material.*
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.create
import react.useState

external interface CourseDetailProgressProps : Props {

    var uiState: CourseDetailProgressUiState

    var onClickStudent: (Person) -> Unit

}

val CourseDetailProgressScreenPreview = FC<Props> { props ->

    val strings = useStringsXml()
    CourseDetailProgressScreenComponent2 {
        uiState = CourseDetailProgressUiState(
            students = { ListPagingSource(listOf(
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 1
                        firstNames = "Bart"
                        lastName = "Simpson"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 2
                        firstNames = "Shelly"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 3
                        firstNames = "Tracy"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 4
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 5
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 6
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 7
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 8
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 9
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 10
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 11
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 12
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                )
            )) },
            courseBlocks = listOf(
                CourseBlock().apply {
                    cbTitle = strings[MessageID.discussion_board]
                }
            )
        )
    }
}


val CourseDetailProgressScreenComponent2 = FC<CourseDetailProgressProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.students, true, 50
    )

    val muiAppState = useMuiAppState()

    var scrollX by useState { 0 }

    val (selectedItems, setSelectedItems) = useState(setOf<Int>())


    VirtualList {

        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
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
                key = { it.person.personUid.toString() }
            ) { person ->

                Stack.create {
                    direction = responsive(StackDirection.row)

                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(10.px)

                        sx {
//                            position = Position.absolute
                            transform = translatex(scrollX.px)
                        }

                        onClick = {
                            person?.also { props.onClickStudent(it.person) }
                        }

                        UstadPersonAvatar {

                            personUid = person?.person?.personUid ?: 0
                        }

                        Typography {
                            sx {
                                width = 300.px
                            }
                            + "${person?.person?.fullName()}"
                        }
                    }

                    (0..60).forEachIndexed { index, item ->
                        Icon {

                            sx {
                                width = 60.px
                                if (scrollX > index){
//                                        position = Position.absolute
//                                        marginLeft = -(44*index).px
                                    color = Color("#006400")
                                }
                            }

                            + CheckBoxOutlined.create()
                        }
                    }

                }
            }
        }


        Container {
            sx {
                width = 100.pct
                overflowX = Overflow.scroll
//                display =  Display.flex
//                alignItems = AlignItems.flexStart
            }
            onScroll = { event ->
                scrollX = event.target.unsafeCast<HTMLElement>().scrollLeft.toInt()
            }

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

            props.uiState.courseBlocks.forEachIndexed { index, item ->

                Typography {
                    sx {
                        width = 44.px
                        transform = rotate(270.deg)
//                        if (scrollX > index){
//                            position = Position.absolute
//                            marginLeft = -(44*index).px
//                            color = Color("000000")
//                        }
                    }

                    + item
                }
            }
                }
            }

            VirtualListOutlet()
        }
    }
}