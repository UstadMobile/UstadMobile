package com.ustadmobile.libuicompose.view.clazz.permissionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListUiState
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CoursePermissionListScreen(
    viewModel: CoursePermissionListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CoursePermissionListUiState())

    val courseTerminologyEntries = rememberCourseTerminologyEntries(
        courseTerminology = uiState.courseTerminology
    )

    CoursePermissionListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        onClickDeleteEntry = viewModel::onClickDeleteEntry,
        courseTerminologyEntries = courseTerminologyEntries,
    )
}

@Composable
fun CoursePermissionListScreen(
    uiState: CoursePermissionListUiState,
    courseTerminologyEntries: List<TerminologyEntry>,
    onClickEntry: (CoursePermission) -> Unit = { },
    onClickDeleteEntry: (CoursePermission) -> Unit = { },
) {
    val refreshFlow = remember {
        emptyFlow<RefreshCommand>()
    }

    val result = rememberDoorRepositoryPager(
        uiState.permissionsList, refreshFlow
    )

    val pagingItems = result.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = pagingItems,
            key = { it.coursePermission?.cpUid ?: 0 }
        ) { item: CoursePermissionAndListDetail? ->
            CoursePermissionListItem(
                coursePermission = item,
                permissionLabels = uiState.permissionLabels,
                modifier = Modifier.clickable {
                    item?.coursePermission?.also(onClickEntry)
                },
                onClickDelete = if(uiState.showDeleteOption && item?.coursePermission?.cpToEnrolmentRole == 0) {
                    onClickDeleteEntry
                }else {
                    null
                },
                courseTerminologyEntries = courseTerminologyEntries,
            )
        }
    }
}
