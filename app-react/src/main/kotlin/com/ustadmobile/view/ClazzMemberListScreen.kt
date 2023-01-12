package com.ustadmobile.view

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.ClazzMemberListUiState
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.util.ext.format
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.*

external interface ClazzMemberListScreenProps : Props {

    var uiState: ClazzMemberListUiState

    var onClickEntry: (PersonWithClazzEnrolmentDetails) -> Unit

    var onClickPendingRequest: (enrolment: PersonWithClazzEnrolmentDetails,
                                approved: Boolean) -> Unit

}


private val ClazzMemberListScreenComponent2 = FC<ClazzMemberListScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadListFilterChipsHeader{
                filterOptions = listOf(
                    MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
                    MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
                    MessageIdOption2(MessageID.all, 0),
                )
                selectedChipId = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
            }

            Stack {
                direction = responsive(StackDirection.row)

                Typography{
                    + strings[MessageID.sort_by]
                }

                UstadListSortHeader {
                    activeSortOrderOption = SortOrderOption(
                        MessageID.name,
                        ClazzDaoCommon.SORT_CLAZZNAME_ASC,
                        true
                    )
                    enabled = true
                    onClickSort = { }
                }
            }

            List {

                Typography {
                    strings[MessageID.teachers_literal]
                }

                if (props.uiState.addTeacherVisible){
                    ListItem {
                        ListItemButton {

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
                    ListItemText {
                        primary = ReactNode("${person.firstNames} ${person.lastName}")
                    }
                }
            }

            List{

                Typography {
                    strings[MessageID.students]
                }


                if (props.uiState.addStudentVisible){
                    ListItem {
                        ListItemButton {

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

                Typography {
                    strings[MessageID.pending_requests]
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

    val statusColor = if ((props.person.attendance/100) >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_GOOD)
        IconColor.success
    else if ((props.person.attendance/100)
        >= ClazzLogAttendanceRecord.ATTENDANCE_THRESHOLD_WARNING)
        IconColor.warning
    else
        IconColor.error

    ListItem{
        ListItemButton {
            onClick = {
                props.onClick(props.person)
            }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode("${props.person.firstNames} ${props.person.lastName}")
                secondary = Stack.create {
                    direction = responsive(StackDirection.row)

                    Icon {
                        color = statusColor
                        + LensRounded.create()
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
                    personUid = 2
                    firstNames = "Student 2"
                    lastName = "Name"
                    attendance = 80F
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
                },
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 2
                    firstNames = "Student 2"
                    lastName = "Name"
                    attendance = 80F
                }
            ),
            teacherList = listOf(
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 1
                    firstNames = "Teacher 1"
                    lastName = "Name"
                },
                PersonWithClazzEnrolmentDetails().apply {
                    personUid = 2
                    firstNames = "Teacher 2"
                    lastName = "Name"
                }
            ),
            addStudentVisible = true,
            addTeacherVisible = true
        )
    }
}