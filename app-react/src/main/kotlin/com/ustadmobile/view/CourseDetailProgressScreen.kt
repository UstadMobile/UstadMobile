package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.core.viewmodel.PersonWithResults
import com.ustadmobile.core.viewmodel.StudentResult
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import mui.icons.material.CheckBoxOutlineBlank
import mui.icons.material.CheckBoxOutlined
import mui.icons.material.Message
import mui.material.*
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.style
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
                    results = listOf(
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        )
                    ),
                    person = Person().apply {
                        personUid = 1
                        firstNames = "Bart"
                        lastName = "Simpson"
                    }
                ),
                PersonWithResults(
                    results = listOf(
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        )
                    ),
                    person = Person().apply {
                        personUid = 2
                        firstNames = "Shelly"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        )
                    ),
                    person = Person().apply {
                        personUid = 3
                        firstNames = "Tracy"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        )
                    ),
                    person = Person().apply {
                        personUid = 4
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = false
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        ),
                        StudentResult(
                            personUid = 0,
                            courseBlockUid = 0,
                            clazzUid = 0,
                            completed = true
                        )
                    ),
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
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.module]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.video]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.audio]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.document]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.module]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.video]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.audio]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.document]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.discussion_board]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.module]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.video]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.audio]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.document]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.module]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.video]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.audio]
                },
                CourseBlock().apply {
                    cbTitle = strings[MessageID.document]
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
                    justifyContent = JustifyContent.spaceBetween

                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(10.px)

                        sx {
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

                    Stack {
                        direction = responsive(StackDirection.row)
                        props.uiState.courseBlocks.forEachIndexed { index, item ->

                            val bool = person?.results?.first { it.courseBlockUid == item.cbUid}

                            Icon {

                                sx {
                                    width = 60.px
                                    if (scrollX > index){
//                                        position = Position.absolute
//                                        marginLeft = -(44*index).px
                                        color = Color("#006400")
                                    }
                                }
//                                if (bool?.completed == true){
                                    + CheckBoxOutlined.create()
//                                } else {
//                                    + CheckBoxOutlineBlank.create()
//                                }
                            }
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
                sx {
                    marginLeft = 360.px
                }
                Stack {
                    direction = responsive(StackDirection.row)

                    props.uiState.courseBlocks.forEachIndexed { index, item ->

                        Typography {

                            sx {
                                width = 60.px
                                transform = rotate(270.deg)
                                textOverflow = TextOverflow.ellipsis
                                height = 100.px
//                                    overflow = Overflow.hidden
//                        if (scrollX > index){
//                            position = Position.absolute
//                            marginLeft = -(44*index).px
//                            color = Color("000000")
//                        }
                            }

                            + item.cbTitle
                        }
                    }
                }
            }

            VirtualListOutlet()
        }
    }
}