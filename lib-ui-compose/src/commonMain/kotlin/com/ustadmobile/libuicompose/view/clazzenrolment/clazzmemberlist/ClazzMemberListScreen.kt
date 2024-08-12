package com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndPersonDetails
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadTooltipBox
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import java.text.DateFormat
import java.util.TimeZone

@Composable
fun ClazzMemberListScreen(
    viewModel: ClazzMemberListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzMemberListUiState())

    ClazzMemberListScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickEntry = viewModel::onClickEntry,
        onClickAddNewMember = viewModel::onClickAddNewMember,
        onClickPendingRequest = viewModel::onClickRespondToPendingEnrolment,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickFilterChip = viewModel::onClickFilterChip,
    )
}

@Composable
fun ClazzMemberListScreen(
    uiState: ClazzMemberListUiState = ClazzMemberListUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (PersonAndClazzMemberListDetails) -> Unit = {},
    onClickAddNewMember: (role: Int) -> Unit = {},
    onClickPendingRequest: (
        enrolment: EnrolmentRequest,
        approved: Boolean
    ) -> Unit = {_, _ -> },
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    val teacherListPager = rememberDoorRepositoryPager(
        uiState.teacherList, refreshCommandFlow
    )
    val teacherListItems = teacherListPager.lazyPagingItems

    val studentListPager = rememberDoorRepositoryPager(uiState.studentList, refreshCommandFlow)
    val studentListItems = studentListPager.lazyPagingItems

    val pendingStudentListPager = rememberDoorRepositoryPager(
        uiState.pendingStudentList, refreshCommandFlow
    )
    val pendingStudentListItems = pendingStudentListPager.lazyPagingItems

    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        item {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onSortOrderChanged,
            )
        }

        item {
            ListItem(
                headlineContent = {
                    Text(text = uiState.terminologyStrings?.get(MR.strings.teachers_literal)
                        ?: stringResource(MR.strings.teachers_literal))
                }
            )
        }

        item {
            if (uiState.addTeacherVisible){
                UstadAddListItem(
                    text = uiState.terminologyStrings?.get(MR.strings.add_a_teacher)
                        ?: stringResource(MR.strings.add_a_teacher),
                    enabled = uiState.fieldsEnabled,
                    icon = Icons.Filled.PersonAdd,
                    onClickAdd = {
                        onClickAddNewMember(ClazzEnrolment.ROLE_TEACHER)
                    }
                )
            }
        }

        ustadPagedItems(
            pagingItems = teacherListItems,
            key = { Pair(1, it.person?.personUid ?: -1) }
        ){ memberDetails ->
            ListItem (
                modifier = Modifier.clickable {
                    memberDetails?.also(onClickEntry)
                },
                headlineContent = {
                    Text(text = memberDetails?.person?.fullName() ?: "")
                },
                leadingContent = {
                    UstadPersonAvatar(
                        pictureUri = memberDetails?.personPicture?.personPictureThumbnailUri,
                        personName = memberDetails?.person?.fullName(),
                    )
                }
            )
        }

        item {
            ListItem(
                headlineContent = {
                    Text(text = uiState.terminologyStrings?.get(MR.strings.students)
                        ?: stringResource(MR.strings.students))
                }
            )
        }

        item {
            if (uiState.addStudentVisible){
                UstadAddListItem(
                    text = uiState.terminologyStrings?.get(MR.strings.add_a_student)
                        ?: stringResource(MR.strings.add_a_student),
                    enabled = uiState.fieldsEnabled,
                    icon = Icons.Filled.PersonAdd,
                    onClickAdd = {
                        onClickAddNewMember(ClazzEnrolment.ROLE_STUDENT)
                    }
                )
            }
        }

        ustadPagedItems(
            pagingItems = studentListItems,
            key = { Pair(2, it.person?.personUid ?: -1) }
        ){ personItem ->
            StudentListItem(
                student = personItem,
                onClick = onClickEntry
            )
        }

        if(pendingStudentListItems.itemCount > 0) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(MR.strings.pending_requests)) }
                )
            }
        }
        
        ustadPagedItems(
            pagingItems = pendingStudentListItems,
            key = { Pair(3, it.enrolmentRequest?.erUid ?: -1) }
        ){ request ->
            PendingStudentListItem(
                request = request,
                onClick = onClickPendingRequest,
                localDateTimeNow = uiState.localDateTimeNow,
                timeFormatter = timeFormatter,
                dateFormatter = dateFormatter,
                dayOfWeekStringMap = uiState.dayOfWeekStrings,
            )
        }
    }
}

 @Composable
 fun StudentListItem(
     student: PersonAndClazzMemberListDetails?,
     onClick: (PersonAndClazzMemberListDetails) -> Unit,
 ){
     ListItem (
         modifier = Modifier.clickable {
             student?.also(onClick)
         },
         headlineContent = {
             Text(text = student?.person?.fullName() ?: "")
         },
         leadingContent = {
             UstadPersonAvatar(
                 pictureUri = student?.personPicture?.personPictureThumbnailUri,
                 personName = student?.person?.fullName(),
             )
         }
     )
 }

@Composable
fun PendingStudentListItem(
    request: EnrolmentRequestAndPersonDetails?,
    onClick: (enrolment: EnrolmentRequest, approved: Boolean) -> Unit,
    localDateTimeNow: LocalDateTime,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
){
    val timeStr = rememberDayOrDate(
        localDateTimeNow = localDateTimeNow,
        timestamp = request?.enrolmentRequest?.erRequestTime ?: 0,
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormatter,
        dayOfWeekStringMap = dayOfWeekStringMap,
    )

    ListItem (
        headlineContent = {
            Text(text = request?.enrolmentRequest?.erPersonFullname ?: "")
        },
        leadingContent = {
            UstadPersonAvatar(
                pictureUri = request?.personPicture?.personPictureThumbnailUri,
                personName = request?.enrolmentRequest?.erPersonFullname,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Person,
                    contentDescription = stringResource(MR.strings.username),
                    modifier = Modifier.size(16.dp),
                )

                Spacer(Modifier.width(8.dp))

                Text(request?.enrolmentRequest?.erPersonUsername ?: "")

                Spacer(Modifier.width(8.dp))

                Icon(Icons.Default.Schedule,
                    contentDescription = stringResource(MR.strings.time),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(timeStr)
            }
        },
        trailingContent = {
            Row {
                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.accept)
                ) {
                    IconButton(
                        onClick = {
                            request?.enrolmentRequest?.also{ onClick(it, true) }
                        }
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = stringResource(MR.strings.accept)
                        )
                    }
                }

                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.reject)
                ) {
                    IconButton(
                        onClick = {
                            request?.enrolmentRequest?.also { onClick(it, false) }
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(MR.strings.reject)
                        )
                    }
                }
            }
        }
    )
}