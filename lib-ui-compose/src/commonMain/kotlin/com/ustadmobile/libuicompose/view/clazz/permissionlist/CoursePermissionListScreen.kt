package com.ustadmobile.libuicompose.view.clazz.permissionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListUiState
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries

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
