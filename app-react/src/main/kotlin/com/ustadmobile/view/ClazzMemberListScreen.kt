package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.ClazzMemberListUiState
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.util.ColorForAttendanceStatus
import com.ustadmobile.util.ext.format
import csstype.Padding
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.responsive
import mui.system.sx
import react.*

external interface ClazzMemberListScreenProps : Props {

    var uiState: ClazzMemberListUiState

    var onClickEntry: (PersonWithClazzEnrolmentDetails) -> Unit

    var onClickPendingRequest: (enrolment: PersonWithClazzEnrolmentDetails,
                                approved: Boolean) -> Unit

    var onClickFilterChip: (MessageIdOption2?) -> Unit

    var onClickAddNewTeacher: () -> Unit

    var onClickAddNewStudent: () -> Unit

    var onClickSort: () -> Unit

}


private val ClazzMemberListScreenComponent2 = FC<ClazzMemberListScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadListFilterChipsHeader {
                filterOptions = props.uiState.filterOptions
                selectedChipId = props.uiState.selectedChipId
                enabled = props.uiState.fieldsEnabled
                onClickFilterChip = props.onClickFilterChip
            }

            UstadListSortHeader {
                activeSortOrderOption = props.uiState.activeSortOrderOption
                enabled = props.uiState.fieldsEnabled
                onClickSort = props.onClickSort
            }

            List {

                ListItem {
                    ListItemText {
                        + strings[MessageID.teachers_literal]
                    }
                }

                if (props.uiState.addTeacherVisible){
                    ListItem {
                        ListItemButton {
                            onClick = { props.onClickAddNewTeacher }

                            ListItemIcon {
                                + PersonAdd.create()
                            }

                            ListItemText {
                                + strings[MessageID.add_a_teacher]
                            }
                        }
                    }
                }

                props.uiState.teacherList.forEach { person ->
                    ListItem{
                        ListItemButton {

                            onClick = { props.onClickEntry(person) }

                            ListItemIcon {
                                + AccountCircle.create()
                            }

                            ListItemText {
                                primary = ReactNode("${person.firstNames} ${person.lastName}")
                            }
                        }
                    }
                }
            }

            List{

                ListItem {
                    ListItemText {
                        + strings[MessageID.students]
                    }
                }

                if (props.uiState.addStudentVisible){
                    ListItem {
                        ListItemButton {

                            onClick = { props.onClickAddNewStudent }

                            ListItemIcon {
                                + PersonAdd.create()
                            }

                            ListItemText {
                                + strings[MessageID.add_a_student]
                            }
                        }
                    }
                }

                props.uiState.studentList.forEach { personItem ->
                    StudentListItem {
                        person = personItem
                        onClick = props.onClickEntry
                    }
                }
            }


            List{

                ListItem {
                    ListItemText {
                        + strings[MessageID.pending_requests]
                    }
                }

                props.uiState.pendingStudentList.forEach { pendingStudent ->
                    PendingStudentListItem {
                        person = pendingStudent
                        onClick = props.onClickPendingRequest
                    }
                }
            }
        }
    }
}


external interface StudentListItemProps : Props {

    var person: PersonWithClazzEnrolmentDetails

    var onClick: (PersonWithClazzEnrolmentDetails) -> Unit

}

private val StudentListItem = FC<StudentListItemProps> { props ->

    val strings = useStringsXml()

    ListItem{
        ListItemButton {
            onClick = {
                props.onClick(props.person)
            }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(
                    "${props.person.firstNames}" +
                            " ${props.person.lastName}"
                )
                secondary = Stack.create {
                    direction = responsive(StackDirection.row)

                    LensRounded {
                        sx {
                            padding = Padding(
                                top = 5.px,
                                bottom = 5.px,
                                right = 0.px,
                                left = 5.px
                            )
                        }
                        color = ColorForAttendanceStatus(props.person.attendance)
                    }

                    Typography {
                        + (props.person.attendance * 100)
                            .toString()
                            .format(strings[MessageID.x_percent_attended])
                    }
                }
            }
        }
    }
}



external interface PendingStudentListItemProps : Props {

    var person: PersonWithClazzEnrolmentDetails

    var onClick: (enrolment: PersonWithClazzEnrolmentDetails, approved: Boolean) -> Unit

}

private val PendingStudentListItem = FC<PendingStudentListItemProps> { props ->

    ListItem {
        ListItemButton {
            onClick = { props.onClick }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode("${props.person.firstNames} " +
                        "${props.person.lastName}")
            }
        }

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            Button {
                variant = ButtonVariant.text

                onClick = {
                    props.onClick(props.person, true)
                }

                + Check.create()
            }

            Button {
                variant = ButtonVariant.text

                onClick = {
                    props.onClick(props.person, false)
                }

                + Close.create()
            }
        }
    }
}


val ClazzMemberListScreenPreview = FC<Props> {

    ClazzMemberListScreenComponent2 {
        uiState = ClazzMemberListUiState(
            studentList = listOf(
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 1
                    firstNames = "Student 1"
                    lastName = "Name"
                    attendance = 20F
                },
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 3
                    firstNames = "Student 3"
                    lastName = "Name"
                    attendance = 65F
                }
            ),
            pendingStudentList = listOf(
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 1
                    firstNames = "Student 1"
                    lastName = "Name"
                    attendance = 20F
                }
            ),
            teacherList = listOf(
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 1
                    firstNames = "Teacher 1"
                    lastName = "Name"
                }
            ),
            addStudentVisible = true,
            addTeacherVisible = true
        )
    }
}