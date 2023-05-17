package com.ustadmobile.port.android.view.clazzenrolment.clazzmemberlist

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import com.ustadmobile.port.android.view.composable.UstadListSortHeader

class ClazzMemberListFragment() : UstadBaseMvvmFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return view
    }


    companion object {

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzMemberListScreen(
    uiState: ClazzMemberListUiState = ClazzMemberListUiState(),
    onClickEntry: (PersonWithClazzEnrolmentDetails) -> Unit = {},
    onClickAddNewTeacher: () -> Unit = {},
    onClickAddNewStudent: () -> Unit = {},
    onClickPendingRequest: (enrolment: PersonWithClazzEnrolmentDetails,
                            approved: Boolean) -> Unit,
    onClickSort: () -> Unit = {},
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
                enabled = uiState.fieldsEnabled,
                onClickSort = onClickSort
            )
        }

        item {
            ListItem(
                text = {
                    Text(text = uiState.terminologyStrings?.get(R.string.teachers_literal)
                        ?: stringResource(R.string.teachers_literal))
                }
            )
        }

        item {
            if (uiState.addTeacherVisible){
                UstadAddListItem(
                    text = uiState.terminologyStrings?.get(R.string.add_a_teacher)
                        ?: stringResource(R.string.add_a_teacher),
                    enabled = uiState.fieldsEnabled,
                    icon = Icons.Filled.PersonAdd,
                    onClickAdd = onClickAddNewTeacher
                )
            }
        }

        items(
            items = teacherListItems,
            key = { Pair(1, it.personUid) }
        ){ person ->
            ListItem (
                modifier = Modifier.clickable {
                    person?.also(onClickEntry)
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

        item {
            ListItem(
                text = {
                    Text(text = uiState.terminologyStrings?.get(R.string.students)
                        ?: stringResource(R.string.students))
                }
            )
        }

        item {
            if (uiState.addStudentVisible){
                UstadAddListItem(
                    text = uiState.terminologyStrings?.get(R.string.add_a_student)
                        ?: stringResource(R.string.add_a_student),
                    enabled = uiState.fieldsEnabled,
                    icon = Icons.Filled.PersonAdd,
                    onClickAdd = onClickAddNewStudent
                )
            }
        }

        items(
            items = studentListItems,
            key = { Pair(2, it.personUid) }
        ){ personItem ->
            StudentListItem(
                person = personItem,
                onClick = onClickEntry
            )
        }

        item {
            ListItem(
                text = { Text(text = stringResource(id = R.string.pending_requests)) }
            )
        }
        
        items(
            items = pendingStudentListItems,
            key = { Pair(3, it.personUid) }
        ){ pendingStudent ->
            PendingStudentListItem(
                person = pendingStudent,
                onClick = onClickPendingRequest
            )
        }
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
                        painter = painterResource(id = com.google.android.exoplayer2.ui.R.drawable.exo_ic_check),
                        contentDescription = stringResource(R.string.accept)
                    )
                }
                IconButton(
                    onClick = {
                        person?.also { onClick(it, false) }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_black_24dp),
                        contentDescription = stringResource(R.string.reject)
                    )
                }
            }
        }
    )
}

@Composable
@Preview
fun ClazzMemberListScreenPreview() {
    val uiStateVal = ClazzMemberListUiState(
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

    MdcTheme {
        ClazzMemberListScreen(
            uiState = uiStateVal,
            onClickPendingRequest = {
                    enrolment: PersonWithClazzEnrolmentDetails,
                    approved: Boolean ->  {}
            }
        )
    }
}