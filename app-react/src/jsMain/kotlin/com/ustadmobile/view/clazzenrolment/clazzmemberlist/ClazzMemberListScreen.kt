package com.ustadmobile.view.clazzenrolment.clazzmemberlist

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndPersonDetails
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.wrappers.intl.Intl
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.PersonAdd as PersonAddIcon
import mui.icons.material.Check as CheckIcon
import mui.icons.material.Close as CloseIcon
import mui.icons.material.Person as PersonIcon
import mui.icons.material.Schedule as ScheduleIcon
import mui.material.*
import mui.material.List
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML


external interface ClazzMemberListScreenProps : Props {

    var uiState: ClazzMemberListUiState

    var refreshCommandFlow: Flow<RefreshCommand>

    var onClickEntry: (PersonAndClazzMemberListDetails) -> Unit

    var onClickPendingRequest: (enrolment: EnrolmentRequest, approved: Boolean) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickAddNewMember: (role: Int) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

}


private val ClazzMemberListScreenComponent2 = FC<ClazzMemberListScreenProps> { props ->

    val strings = useStringProvider()

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val teachersMediatorResult = useDoorRemoteMediator(
        props.uiState.teacherList, props.refreshCommandFlow
    )
    val teachersInfiniteQueryResult = usePagingSource(
        teachersMediatorResult.pagingSourceFactory, true
    )

    val studentsMediatorResult = useDoorRemoteMediator(
        props.uiState.studentList, props.refreshCommandFlow
    )
    val studentsInfiniteQueryResult = usePagingSource(
        studentsMediatorResult.pagingSourceFactory, true
    )


    val pendingStudentsMediatorResult = useDoorRemoteMediator(
        props.uiState.pendingStudentList, props.refreshCommandFlow
    )
    val pendingStudentsInfiniteQueryResult = usePagingSource(
        pendingStudentsMediatorResult.pagingSourceFactory, true
    )

    val timeFormatterVal = useTimeFormatter()

    val dateFormatterVal = useDateFormatter()

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
                        disableGutters = true
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
                        disableGutters = true

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
                        disableGutters = true
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
                    key = { "p_${it.enrolmentRequest?.erUid} "}
                ) { pendingStudent ->
                    PendingStudentListItem.create {
                        request = pendingStudent
                        onClick = props.onClickPendingRequest
                        localDateTimeNow = props.uiState.localDateTimeNow
                        dayOfWeekStringMap = props.uiState.dayOfWeekStrings
                        timeFormatter = timeFormatterVal
                        dateFormatter = dateFormatterVal
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
            disableGutters = true

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

    var request: EnrolmentRequestAndPersonDetails?

    var onClick: (enrolment: EnrolmentRequest, approved: Boolean) -> Unit

    var localDateTimeNow: LocalDateTime

    var timeFormatter: Intl.Companion.DateTimeFormat

    var dateFormatter: Intl.Companion.DateTimeFormat

    var dayOfWeekStringMap: Map<DayOfWeek, String>
}

private val PendingStudentListItem = FC<PendingStudentListItemProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)
    val requestTimeStr = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.localDateTimeNow,
        timestamp = props.request?.enrolmentRequest?.erRequestTime ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dateFormatter = props.dateFormatter,
        dayOfWeekStringMap = props.dayOfWeekStringMap,
    )

    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                pictureUri = props.request?.personPicture?.personPictureThumbnailUri
                personName = props.request?.enrolmentRequest?.erPersonFullname
            }
        }

        ListItemText {
            primary = ReactNode(props.request?.enrolmentRequest?.erPersonFullname ?: "")
            secondary = Stack.create {
                direction = responsive(StackDirection.row)

                PersonIcon {
                    sx {
                        marginRight = theme.spacing(1)
                    }

                    fontSize = SvgIconSize.small
                }

                + (props.request?.enrolmentRequest?.erPersonUsername ?: "")

                ScheduleIcon {
                    sx  {
                        marginLeft = theme.spacing(1)
                        marginRight = theme.spacing(1)
                    }

                    fontSize = SvgIconSize.small
                }

                + requestTimeStr

            }
            secondaryTypographyProps = jso {
                component = ReactHTML.div
            }
        }


        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            Tooltip {
                title = ReactNode(strings[MR.strings.accept])
                IconButton {
                    ariaLabel = strings[MR.strings.accept]
                    onClick = {
                        props.request?.enrolmentRequest?.also { props.onClick(it, true) }
                    }

                    CheckIcon()
                }
            }

            Tooltip {
                title = ReactNode(strings[MR.strings.reject])
                IconButton {
                    ariaLabel = strings[MR.strings.reject]

                    onClick = {
                        props.request?.enrolmentRequest?.also { props.onClick(it, false) }
                    }

                    CloseIcon()
                }
            }
        }
    }
}

