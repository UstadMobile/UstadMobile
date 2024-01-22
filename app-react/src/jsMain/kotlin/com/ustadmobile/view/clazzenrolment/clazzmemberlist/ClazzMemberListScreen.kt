package com.ustadmobile.view.clazzenrolment.clazzmemberlist

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.core.jso
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.PersonAdd as PersonAddIcon
import mui.icons.material.AccountCircle as AccountCircleIcon
import mui.icons.material.Check as CheckIcon
import mui.icons.material.Close as CloseIcon
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.*


external interface ClazzMemberListScreenProps : Props {

    var uiState: ClazzMemberListUiState

    var onClickEntry: (PersonAndClazzMemberListDetails) -> Unit

    var onClickPendingRequest: (enrolment: PersonAndClazzMemberListDetails,
                                approved: Boolean) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickAddNewMember: (role: Int) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

}


private val ClazzMemberListScreenComponent2 = FC<ClazzMemberListScreenProps> { props ->

    val strings = useStringProvider()

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val teachersInfiniteQueryResult = usePagingSource(props.uiState.teacherList, true)

    val studentsInfiniteQueryResult = usePagingSource(props.uiState.studentList, true)

    val pendingStudentsInfiniteQueryResult = usePagingSource(props.uiState.pendingStudentList, true)


    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
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
                        + (props.uiState.terminologyStrings?.get(MR.strings.teachers_literal)
                            ?: strings[MR.strings.teachers_literal])
                    }
                }
            }

            if (props.uiState.addTeacherVisible){
                item {
                    UstadAddListItem.create {
                        text = props.uiState.terminologyStrings?.get(MR.strings.add_a_teacher)
                            ?: strings[MR.strings.add_a_teacher]
                        enabled = props.uiState.fieldsEnabled
                        icon = PersonAddIcon.create()
                        onClickAdd = { props.onClickAddNewMember(ClazzEnrolment.ROLE_TEACHER) }
                    }
                }
            }

            infiniteQueryPagingItems(
                items = teachersInfiniteQueryResult,
                key = { "t_${it.person?.personUid}" }
            ) { member ->
                ListItem.create {
                    ListItemButton {
                        onClick = { member?.also { props.onClickEntry(it) } }

                        ListItemIcon {
                            UstadPersonAvatar {
                                pictureUri = member?.personPicture?.personPictureThumbnailUri
                                personName = member?.person?.fullName()
                            }
                        }

                        ListItemText {
                            primary = ReactNode(member?.person?.fullName() ?: "")
                        }
                    }
                }
            }

            item {
                ListItem.create {
                    ListItemText {
                        + (props.uiState.terminologyStrings?.get(MR.strings.students)
                            ?: strings[MR.strings.students])
                    }
                }
            }

            if (props.uiState.addStudentVisible){
                item {
                    UstadAddListItem.create {
                        text = props.uiState.terminologyStrings?.get(MR.strings.add_a_student)
                            ?: strings[MR.strings.add_a_student]
                        enabled = props.uiState.fieldsEnabled
                        icon = PersonAddIcon.create()
                        onClickAdd = { props.onClickAddNewMember(ClazzEnrolment.ROLE_STUDENT) }
                    }
                }
            }

            infiniteQueryPagingItems(
                items = studentsInfiniteQueryResult,
                key = { "s_${it.person?.personUid}"}
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
                            + strings[MR.strings.pending_requests]
                        }
                    }
                }

                infiniteQueryPagingItems(
                    items = pendingStudentsInfiniteQueryResult,
                    key = { "p_${it.person?.personUid} "}
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

    var person: PersonAndClazzMemberListDetails?

    var onClick: (PersonAndClazzMemberListDetails) -> Unit

}

private val StudentListItem = FC<StudentListItemProps> { props ->
    ListItem{
        ListItemButton {
            onClick = {
                props.person?.also { props.onClick(it) }
            }

            ListItemIcon {
                UstadPersonAvatar {
                    pictureUri = props.person?.personPicture?.personPictureThumbnailUri
                    personName = props.person?.person?.fullName()
                }
            }

            ListItemText {
                primary = ReactNode(props.person?.person?.fullName() ?: "")
            }
        }
    }
}



external interface PendingStudentListItemProps : Props {

    var person: PersonAndClazzMemberListDetails?

    var onClick: (enrolment: PersonAndClazzMemberListDetails, approved: Boolean) -> Unit

}

private val PendingStudentListItem = FC<PendingStudentListItemProps> { props ->

    ListItem {
        ListItemButton {
            onClick = { props.onClick }

            ListItemIcon {
                UstadPersonAvatar {
                    pictureUri = props.person?.personPicture?.personPictureThumbnailUri
                    personName = props.person?.person?.fullName()
                }
            }

            ListItemText {
                primary = ReactNode(props.person?.person?.fullName())
            }
        }

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            Button {
                variant = ButtonVariant.text
                onClick = {
                    props.person?.also { props.onClick(it, true) }
                }

                + CheckIcon.create()
            }

            Button {
                variant = ButtonVariant.text

                onClick = {
                    props.person?.also { props.onClick(it, false) }
                }

                + CloseIcon.create()
            }
        }
    }
}

