package com.ustadmobile.libuicompose.view.clazz.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.libuicompose.components.SortListMode
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadLazyVerticalGrid
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.libuicompose.util.ext.copyWithNewFabOnClick
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.defaultSortListMode
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberHtmlToPlainText
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import com.ustadmobile.libuicompose.view.clazz.CourseImage
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import moe.tlaster.precompose.navigation.BackStackEntry
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClazzListScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
    onShowSnackBar: SnackBarDispatcher,
    destName: String,
) {
    var createNewOptionsVisible by remember {
        mutableStateOf(false)
    }


    val viewModel = ustadViewModel(
        modelClass = ClazzListViewModel::class,
        backStackEntry = backStackEntry,
        onSetAppUiState = onSetAppUiState,
        navController = navController,
        navResultReturner = navResultReturner,
        onShowSnackBar = onShowSnackBar,
        appUiStateMap = {
            it.copyWithNewFabOnClick {
                createNewOptionsVisible = true
            }
        }
    ) { di, savedStateHandle ->
        ClazzListViewModel(di, savedStateHandle, destName)
    }

    val uiState: ClazzListUiState by viewModel.uiState.collectAsState(initial = ClazzListUiState())

    if(createNewOptionsVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                createNewOptionsVisible = false
            }
        ) {
            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    createNewOptionsVisible = false
                    viewModel.onClickJoinExistingClazz()
                },
                headlineContent = { Text(stringResource(MR.strings.join_existing_course)) },
                leadingContent = { Icon(Icons.AutoMirrored.Default.Login, contentDescription = null) },
            )

            if(uiState.canAddNewCourse) {
                UstadBottomSheetOption(
                    modifier = Modifier.clickable {
                        createNewOptionsVisible = false
                        viewModel.onClickAdd()
                    },
                    headlineContent = { Text(stringResource(MR.strings.add_a_new_course)) },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                )
            }
        }
    }

    ClazzListScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickClazz = viewModel::onClickEntry,
        onClickFilterChip = viewModel::onClickFilterChip,
        onClickSortOption = viewModel::onSortOrderChanged,
        onClickCancelEnrolmentRequest = viewModel::onClickCancelEnrolmentRequest,
    )
}

@Composable
fun ClazzListScreen(
    uiState: ClazzListUiState = ClazzListUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = emptyFlow(),
    onClickClazz: (Clazz) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickSortOption: (SortOrderOption) -> Unit = { },
    onClickCancelEnrolmentRequest: (EnrolmentRequest) -> Unit = { },
    sortListMode: SortListMode = defaultSortListMode(),
) {
    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.clazzList,
        refreshCommandFlow = refreshCommandFlow,
    )

    val hasPendingEnrolments = uiState.pendingEnrolments.isNotEmpty()
    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    UstadLazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),

        // 600 width of the smallest iPad,
        // subtracted 16 = horizontal padding & space between cards,
        // half of 584 is 292
        // card width = 292dp.
        columns = GridCells.Adaptive(292.dp)
    ) {

        if(hasPendingEnrolments) {
            item(key = "pending_enrolments_header", span = { GridItemSpan(maxLineSpan) }) {
                UstadDetailHeader { Text(stringResource(MR.strings.pending_requests)) }
            }

            items(
                items = uiState.pendingEnrolments,
                key = {
                    Pair("pendingRequest", it.enrolmentRequest?.erUid ?: System.identityHashCode(it))
                },
                span = { GridItemSpan(maxLineSpan) },
            ) {
                PendingEnrolmentListItem(
                    request = it,
                    onClickCancel = onClickCancelEnrolmentRequest,
                    timeNow = uiState.localDateTimeNow,
                    timeFormatter = timeFormatter,
                    dateFormatter = dateFormatter,
                    dayOfWeekMap = uiState.dayOfWeekStrings,
                )
            }

            item(
                key = "pending_divider",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
            }
        }


        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                enabled = uiState.fieldsEnabled,
                sortOptions = uiState.sortOptions,
                onClickSortOption = onClickSortOption,
                mode = sortListMode,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListFilterChipsHeader(
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }


        if(!hasPendingEnrolments && doorRepoPager.isSettledEmpty) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "empty_message") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.clazzUid }
        ){
            ClazzListItem(
                clazz = it,
                onClickClazz = onClickClazz
            )
        }

        //Host fragment thinks scroll bar behavior increases available height - need to compensate
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(176.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClazzListItem(
    clazz: ClazzWithListDisplayDetails?,
    onClickClazz: (Clazz) -> Unit
){


    @Suppress("UNUSED_VARIABLE") //Reserved for future use
    val role = RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == clazz?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.stringResource

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Black),
            onClick = {
                clazz?.also { onClickClazz(it) }
            },
            modifier = Modifier.defaultItemPadding()
        ) {
            Column {
                CourseImage(
                    coursePicture = clazz?.coursePicture,
                    clazzName = clazz?.clazzName,
                    modifier = Modifier.height(96.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp))
                )

                Text(
                    text = clazz?.clazzName ?: "",
                    maxLines = 1,
                    modifier = Modifier.defaultItemPadding(bottom = 0.dp),
                )

                Text(
                    text = rememberHtmlToPlainText(clazz?.clazzDesc ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    modifier = Modifier.defaultItemPadding(top = 0.dp),
                )
            }
        }

        val enrolment = clazz?.clazzActiveEnrolment
        if(enrolment != null) {
            val terminologyEntries = rememberCourseTerminologyEntries(
                clazz.terminology
            )
            val stringResource = when(enrolment.clazzEnrolmentRole) {
                ClazzEnrolment.ROLE_STUDENT -> MR.strings.student
                ClazzEnrolment.ROLE_TEACHER -> MR.strings.teacher
                else -> null
            }
            if(stringResource != null) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )

                    Text(courseTerminologyEntryResource(terminologyEntries, stringResource))
                }
            }
        }
    }
}