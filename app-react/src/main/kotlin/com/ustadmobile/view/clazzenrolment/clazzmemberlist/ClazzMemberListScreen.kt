package com.ustadmobile.view.clazzenrolment.clazzmemberlist

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import csstype.px
import js.core.jso
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.PersonAdd
import mui.icons.material.AccountCircle
import mui.icons.material.LensRounded
import mui.icons.material.Check
import mui.icons.material.Close
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

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickAddNewMember: (role: Int) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

}


private val ClazzMemberListScreenComponent2 = FC<ClazzMemberListScreenProps> { props ->

    val strings = useStringsXml()

    val muiAppState = useMuiAppState()

    val teachersInfiniteQueryResult = usePagingSource(props.uiState.teacherList, true)

    val studentsInfiniteQueryResult = usePagingSource(props.uiState.studentList, true)

    val pendingStudentsInfiniteQueryResult = usePagingSource(props.uiState.pendingStudentList, true)


    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.activeSortOrderOption
                    enabled = true
                    onClickSort = props.onClickSort
                    sortOptions = props.uiState.sortOptions
                }
            }

            item {
                UstadListFilterChipsHeader.create {
                    filterOptions = props.uiState.filterOptions
                    selectedChipId = props.uiState.selectedChipId
                    enabled = props.uiState.fieldsEnabled
                    onClickFilterChip = props.onClickFilterChip
                }
            }

            item {
                ListItem.create {
                    ListItemText {
                        + (props.uiState.terminologyStrings?.get(MessageID.teachers_literal)
                            ?: strings[MessageID.teachers_literal])
                    }
                }
            }

            if (props.uiState.addTeacherVisible){
                UstadAddListItem.create {
                    text = props.uiState.terminologyStrings?.get(MessageID.add_a_teacher)
                        ?: strings[MessageID.add_a_teacher]
                    enabled = props.uiState.fieldsEnabled
                    icon = PersonAdd.create()
                    onClickAdd = { props.onClickAddNewMember(ClazzEnrolment.ROLE_TEACHER) }
                }
            }

            infiniteQueryPagingItems(
                items = teachersInfiniteQueryResult,
                key = { "t_${it.personUid}" }
            ) { person ->
                ListItem.create {
                    ListItemButton {
                        onClick = { person?.also { props.onClickEntry(it) } }

                        ListItemIcon {
                            + AccountCircle.create()
                        }

                        ListItemText {
                            primary = ReactNode("${person?.firstNames} ${person?.lastName}")
                        }
                    }
                }
            }

            item {
                ListItem.create {
                    ListItemText {
                        + (props.uiState.terminologyStrings?.get(MessageID.students)
                            ?: strings[MessageID.students])
                    }
                }
            }

            if (props.uiState.addStudentVisible){
                item {
                    UstadAddListItem.create {
                        text = props.uiState.terminologyStrings?.get(MessageID.add_a_student)
                            ?: strings[MessageID.add_a_student]
                        enabled = props.uiState.fieldsEnabled
                        icon = PersonAdd.create()
                        onClickAdd = { props.onClickAddNewMember(ClazzEnrolment.ROLE_STUDENT) }
                    }
                }
            }

            infiniteQueryPagingItems(
                items = studentsInfiniteQueryResult,
                key = { "s_${it.personUid}"}
            ) { personItem ->
                StudentListItem.create {
                    person = personItem
                    onClick = props.onClickEntry
                }
            }

            if(props.uiState.addStudentVisible) {
                item {
                    ListItem.create {
                        ListItemText {
                            + strings[MessageID.pending_requests]
                        }
                    }
                }

                infiniteQueryPagingItems(
                    items = pendingStudentsInfiniteQueryResult,
                    key = { "p_${it.personUid} "}
                ) { pendingStudent ->
                    PendingStudentListItem.create {
                        person = pendingStudent
                        onClick = props.onClickPendingRequest
                    }
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}

val ClazzMemberListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzMemberListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzMemberListUiState())

    ClazzMemberListScreenComponent2 {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
        onClickPendingRequest = viewModel::onClickRespondToPendingEnrolment
        onClickFilterChip = viewModel::onClickFilterChip
        onClickAddNewMember = viewModel::onClickAddNewMember
        onClickSort = viewModel::onSortOrderChanged
    }

}

external interface StudentListItemProps : Props {

    var person: PersonWithClazzEnrolmentDetails?

    var onClick: (PersonWithClazzEnrolmentDetails) -> Unit

}

private val StudentListItem = FC<StudentListItemProps> { props ->
    ListItem{
        ListItemButton {
            onClick = {
                props.person?.also { props.onClick(it) }
            }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(
                    "${props.person?.firstNames}" +
                            " ${props.person?.lastName}"
                )
            }
        }
    }
}



external interface PendingStudentListItemProps : Props {

    var person: PersonWithClazzEnrolmentDetails?

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
                primary = ReactNode("${props.person?.firstNames} " +
                        "${props.person?.lastName}")
            }
        }

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            Button {
                variant = ButtonVariant.text
                onClick = {
                    props.person?.also { props.onClick(it, true) }
                }

                + Check.create()
            }

            Button {
                variant = ButtonVariant.text

                onClick = {
                    props.person?.also { props.onClick(it, false) }
                }

                + Close.create()
            }
        }
    }
}


val ClazzMemberListScreenPreview = FC<Props> {

    ClazzMemberListScreenComponent2 {
        uiState = ClazzMemberListUiState(
            studentList = {
                ListPagingSource(listOf(
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
                ))
            },
            pendingStudentList = {
                ListPagingSource(listOf(
                    PersonWithClazzEnrolmentDetails().apply {
                        personUid = 1
                        firstNames = "Student 1"
                        lastName = "Name"
                        attendance = 20F
                    }
                ))
            },
            teacherList = {
                ListPagingSource(listOf(
                    PersonWithClazzEnrolmentDetails().apply {
                        personUid = 1
                        firstNames = "Teacher 1"
                        lastName = "Name"
                    }
                ))
            },
            addStudentVisible = true,
            addTeacherVisible = true
        )
    }
}
