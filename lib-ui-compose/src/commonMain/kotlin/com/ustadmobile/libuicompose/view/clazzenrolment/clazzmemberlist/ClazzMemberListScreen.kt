package com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzMemberListScreenForViewModel(
    viewModel: ClazzMemberListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzMemberListUiState())
//    val context = LocalContext.current

    ClazzMemberListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        onClickAddNewMember = viewModel::onClickAddNewMember,
        onClickPendingRequest = viewModel::onClickRespondToPendingEnrolment,
        onClickSort = {
                      // TODO error
//            SortBottomSheetFragment(
//                sortOptions = uiState.sortOptions,
//                selectedSort = uiState.activeSortOrderOption,
//                onSortOptionSelected = {
//                    viewModel.onSortOrderChanged(it)
//                }
//            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
        onClickFilterChip = viewModel::onClickFilterChip,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzMemberListScreen(
    uiState: ClazzMemberListUiState = ClazzMemberListUiState(),
    onClickEntry: (PersonWithClazzEnrolmentDetails) -> Unit = {},
    onClickAddNewMember: (role: Int) -> Unit = {},
    onClickPendingRequest: (
        enrolment: PersonWithClazzEnrolmentDetails,
        approved: Boolean
    ) -> Unit = {_, _ -> },
    onClickSort: () -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    // TODO error
//    val teacherListPager = remember(uiState.teacherList) {
//        Pager(
//            pagingSourceFactory = uiState.teacherList,
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
//        )
//    }
//    val teacherListItems = teacherListPager.flow.collectAsLazyPagingItems()
//
//    val studentListPager = remember(uiState.studentList) {
//        Pager(
//            pagingSourceFactory = uiState.studentList,
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
//        )
//    }
//    val studentListItems = studentListPager.flow.collectAsLazyPagingItems()
//
//    val pendingStudentListPager = remember(uiState.pendingStudentList) {
//        Pager(
//            pagingSourceFactory = uiState.pendingStudentList,
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
//        )
//    }
//    val pendingStudentListItems = pendingStudentListPager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
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
                enabled = uiState.fieldsEnabled
            )
        }

        item {
            ListItem(
                text = {
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

//        items(
//            items = teacherListItems,
//            key = { Pair(1, it.personUid) }
//        ){ person ->
//            ListItem (
//                modifier = Modifier.clickable {
//                    person?.also(onClickEntry)
//                },
//                text = {
//                    Text(text = "${person?.firstNames} ${person?.lastName}")
//                },
//                icon = {
//                    Icon(
//                        imageVector = Icons.Filled.AccountCircle,
//                        contentDescription = null
//                    )
//                }
//            )
//        }

        item {
            ListItem(
                text = {
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

//        items(
//            items = studentListItems,
//            key = { Pair(2, it.personUid) }
//        ){ personItem ->
//            StudentListItem(
//                person = personItem,
//                onClick = onClickEntry
//            )
//        }

        item {
            ListItem(
                text = { Text(text = stringResource(MR.strings.pending_requests)) }
            )
        }
        
//        items(
//            items = pendingStudentListItems,
//            key = { Pair(3, it.personUid) }
//        ){ pendingStudent ->
//            PendingStudentListItem(
//                person = pendingStudent,
//                onClick = onClickPendingRequest
//            )
//        }
    }
}

 @OptIn(ExperimentalMaterialApi::class)
 @Composable
 fun StudentListItem(
     person: PersonWithClazzEnrolmentDetails?,
     onClick: (PersonWithClazzEnrolmentDetails) -> Unit,
 ){

     ListItem (
         modifier = Modifier.clickable {
             person?.also(onClick)
         },
         text = {
             Text(text = "${person?.firstNames} ${person?.lastName}")
         },
         icon = {
             Icon(
                 imageVector = Icons.Filled.AccountCircle,
                 contentDescription = null
             )
         }
     )
 }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PendingStudentListItem(
    person: PersonWithClazzEnrolmentDetails?,
    onClick: (enrolment: PersonWithClazzEnrolmentDetails, approved: Boolean) -> Unit
){
    ListItem (
        text = {
            Text(text = "${person?.firstNames} ${person?.lastName}")
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null
            )
        },
        trailing = {
            Row {
                IconButton(
                    onClick = {
                        person?.also{ onClick(it, true) }
                    }
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = stringResource(MR.strings.accept)
                    )
                }
                IconButton(
                    onClick = {
                        person?.also { onClick(it, false) }
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