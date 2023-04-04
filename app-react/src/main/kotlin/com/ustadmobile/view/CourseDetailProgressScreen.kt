package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.Header
import csstype.*
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

external interface CourseDetailProgressProps : Props {

    var uiState: CourseDetailProgressUiState

    var onClickStudent: (Person) -> Unit

}

val CourseDetailProgressScreenPreview = FC<Props> {

    val strings = useStringsXml()

    CourseDetailProgressScreenComponent2 {
        uiState = CourseDetailProgressUiState(
            students = listOf(
                Person().apply {
                    personUid = 1
                    firstNames = "Bart"
                    lastName = "Simpson"
                },
                Person().apply {
                    personUid = 2
                    firstNames = "Shelly"
                    lastName = "Mackleberry"
                },
                Person().apply {
                    personUid = 3
                    firstNames = "Tracy"
                    lastName = "Mackleberry"
                },
                Person().apply {
                    personUid = 4
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 5
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 6
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 7
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 8
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 9
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 10
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 11
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 12
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 8
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 9
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 10
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 11
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                },
                Person().apply {
                    personUid = 12
                    firstNames = "Nelzon"
                    lastName = "Muntz"
                }
            ),
            results = listOf(
                strings[MessageID.discussion_board],
                strings[MessageID.dashboard],
                strings[MessageID.module],
                strings[MessageID.video],
                strings[MessageID.assignments],
                strings[MessageID.document]+"6",
                strings[MessageID.audio],
                strings[MessageID.phone],
                strings[MessageID.change_photo],
                strings[MessageID.ebook],
                strings[MessageID.discussion_board],
                strings[MessageID.dashboard],
                strings[MessageID.module],
                strings[MessageID.video],
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

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            val (selectedItems, setSelectedItems) = useState(setOf<Int>())

            ListSubheader {
                List {
                    sx {
                            display = Display.flex
                            flexDirection = FlexDirection.row
                        }
                    props.uiState.results.forEachIndexed { index, item ->
                        ListItem {
                            selected = selectedItems.contains(index)
                            ListItemButton {
                                onClick = {
                                    if (selectedItems.contains(index)) {
                                        setSelectedItems(selectedItems - index)
                                    } else {
                                        setSelectedItems(selectedItems + index)
                                    }
                                }

                                ListItemText {
                                    primary = ReactNode(item)
                                }
                            }
                        }
                    }
                }
            }

            List {
                sx {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                }
                props.uiState.results.forEachIndexed { index, item ->
                    ListItem {
                        selected = selectedItems.contains(index)
                        ListItemButton {
                            onClick = {
                                if (selectedItems.contains(index)) {
                                    setSelectedItems(selectedItems - index)
                                } else {
                                    setSelectedItems(selectedItems + index)
                                }
                            }

                            ListItemText {
                                primary = ReactNode(item)
                            }
                        }
                    }
                }
            }

//            List {
//                ariaOrientation = AriaOrientation.horizontal
//                ListSubheader {
//                    sx {
////                        position = Position.fixed
////                        transform = rotate(270.deg)
//                        width = 400.px
//                            height = 120.px
//                    }
//
//                    List {
//
//                        sx {
//                            display = Display.flex
//                            flexDirection = FlexDirection.row
//                            width = 120.px
//                            height = 120.px
//                        }
//                        props.uiState.results.forEachIndexed { index, result ->
//
//                            ListItem{
//
//                                autoFocus = index == 6
//
//                                ListItemText {
//                                    primary = ReactNode(result+index)
//                                }
//                            }
//                        }
//                    }
//                }
//
////                props.uiState.students.forEach { student ->
////
////                    ListItem{
////                        ListItemButton {
////                            onClick = {
////                                props.onClickStudent(student)
////                            }
////
////                            ListItemText {
////                                primary = ReactNode(student.fullName())
////                            }
////                        }
////
//////                        secondaryAction = Box.create {
////////                            direction = responsive(StackDirection.row)
////////                            sx {
////////                            width = 120.px
////////                        }
//////
////////                            List {
////////
////////                                sx {
////////                                    display = Display.flex
////////                                    flexDirection = FlexDirection.row
////////                                }
////////
////////                                props.uiState.results.forEach { result ->
////////
////////                                    ListItem{
////////                                        ListItemText {
////////                                            primary = ReactNode(result)
////////                                        }
////////                                    }
////////                                }
////////                            }
//////                        }
////                    }
////                }
//            }

        }
    }
}