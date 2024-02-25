package com.ustadmobile.libuicompose.view.clazz.permissionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListUiState
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePermissionListScreen(
    viewModel: CoursePermissionListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CoursePermissionListUiState())

    val courseTerminologyEntries = rememberCourseTerminologyEntries(
        courseTerminology = uiState.courseTerminology
    )

    if(uiState.addOptionsVisible) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissAddOptions
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                text = stringResource(MR.strings.grant_permission_to)
            )

            UstadBottomSheetOption(
                modifier = Modifier.clickable { viewModel.onClickAddNewForRole(ClazzEnrolment.ROLE_TEACHER) },
                headlineContent = {
                    Text(courseTerminologyEntryResource(courseTerminologyEntries, MR.strings.teachers_literal))
                }
            )
            UstadBottomSheetOption(
                modifier = Modifier.clickable { viewModel.onClickAddNewForRole(ClazzEnrolment.ROLE_STUDENT) },
                headlineContent = {
                    Text(courseTerminologyEntryResource(courseTerminologyEntries, MR.strings.students))
                }
            )
            UstadBottomSheetOption(
                modifier = Modifier.clickable { viewModel.onClickAddNewForPerson() },
                headlineContent = {
                    Text(stringResource(MR.strings.select_person))
                }
            )
        }
    }

    CoursePermissionListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        courseTerminologyEntries = courseTerminologyEntries,
    )
}

@Composable
fun CoursePermissionListScreen(
    uiState: CoursePermissionListUiState,
    courseTerminologyEntries: List<TerminologyEntry>,
    onClickEntry: (CoursePermission) -> Unit = { },
) {
    val pager = remember(uiState.permissionsList) {
        Pager(
            pagingSourceFactory = uiState.permissionsList,
            config = PagingConfig(20, enablePlaceholders = true)
        )
    }

    val pagingItems = pager.flow.collectAsLazyPagingItems()

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = pagingItems,
            key = { it}
        ) { item: CoursePermissionAndListDetail? ->
            CoursePermissionListItem(
                coursePermission = item,
                permissionLabels = uiState.permissionLabels,
                modifier = Modifier.clickable {
                    item?.coursePermission?.also(onClickEntry)
                },
                courseTerminologyEntries = courseTerminologyEntries,
            )
        }
    }
}
