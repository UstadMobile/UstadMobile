package com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzMemberListScreen(
    viewModel: ClazzMemberListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzMemberListUiState())

    ClazzMemberListScreen(
        uiState = uiState,
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
    onClickEntry: (PersonAndClazzMemberListDetails) -> Unit = {},
    onClickAddNewMember: (role: Int) -> Unit = {},
    onClickPendingRequest: (
        enrolment: PersonAndClazzMemberListDetails,
        approved: Boolean
    ) -> Unit = {_, _ -> },
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    val teacherListPager = remember(uiState.teacherList) {
        Pager(
            pagingSourceFactory = uiState.teacherList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val teacherListItems = teacherListPager.flow.collectAsLazyPagingItems()

    val studentListPager = remember(uiState.studentList) {
        Pager(
            pagingSourceFactory = uiState.studentList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val studentListItems = studentListPager.flow.collectAsLazyPagingItems()

    val pendingStudentListPager = remember(uiState.pendingStudentList) {
        Pager(
            pagingSourceFactory = uiState.pendingStudentList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val pendingStudentListItems = pendingStudentListPager.flow.collectAsLazyPagingItems()

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
            key = { Pair(3, it.person?.personUid ?: -1) }
        ){ pendingStudent ->
            PendingStudentListItem(
                member = pendingStudent,
                onClick = onClickPendingRequest
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
    member: PersonAndClazzMemberListDetails?,
    onClick: (enrolment: PersonAndClazzMemberListDetails, approved: Boolean) -> Unit
){
    ListItem (
        headlineContent = {
            Text(text = member?.person?.fullName() ?: "")
        },
        leadingContent = {
            UstadPersonAvatar(
                pictureUri = member?.personPicture?.personPictureThumbnailUri,
                personName = member?.person?.fullName(),
            )
        },
        trailingContent = {
            Row {
                IconButton(
                    onClick = {
                        member?.also{ onClick(it, true) }
                    }
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = stringResource(MR.strings.accept)
                    )
                }
                IconButton(
                    onClick = {
                        member?.also { onClick(it, false) }
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(MR.strings.reject)
                    )
                }
            }
        }
    )
}