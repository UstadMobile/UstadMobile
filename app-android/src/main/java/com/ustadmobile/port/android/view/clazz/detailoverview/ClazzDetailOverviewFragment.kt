package com.ustadmobile.port.android.view.clazz.detailoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.paging.compose.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDateRange
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.UstadClazzAssignmentListItem
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.port.android.view.contententry.UstadContentEntryListItem

interface ClazzDetailOverviewEventListener {
    fun onClickClassCode(code: String?)

    fun onClickShare()

    fun onClickDownloadAll()

    fun onClickPermissions()
}

class ClazzDetailOverviewFragment: UstadBaseMvvmFragment() {

    private val viewModel: ClazzDetailOverviewViewModel by ustadViewModels { di, savedStateHandle ->
        ClazzDetailOverviewViewModel(di, savedStateHandle, ClazzDetailViewModel.DEST_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzDetailOverviewScreen(viewModel)
                }
            }
        }


    }

    companion object {

        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzDetailOverviewScreen(
    uiState: ClazzDetailOverviewUiState = ClazzDetailOverviewUiState(),
    onClickClassCode: (String) -> Unit = {},
    onClickCourseBlock: (CourseBlock) -> Unit = {},
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
    onClickPermissions: () -> Unit = {}
) {
    val pager = remember(uiState.courseBlockList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.courseBlockList,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val numMembers = stringResource(R.string.x_teachers_y_students,
        uiState.clazz?.numTeachers ?: 0,
        uiState.clazz?.numStudents ?: 0)

    val clazzDateRange = rememberFormattedDateRange(
        startTimeInMillis = uiState.clazz?.clazzStartTime ?: 0L,
        endTimeInMillis = uiState.clazz?.clazzEndTime ?: UNSET_DISTANT_FUTURE,
        timeZoneId = uiState.clazz?.clazzTimeZone ?: "UTC",
    )



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ){

        item {
            UstadQuickActionButton(
                labelText = stringResource(R.string.permissions),
                imageId = R.drawable.baseline_lock_24,
                onClick = onClickPermissions
            )
        }

        item {
            HtmlText(
                html = uiState.clazz?.clazzDesc ?: "",
                modifier = Modifier.defaultItemPadding()
            )
        }

        item {
            UstadDetailField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                imageId = R.drawable.ic_group_black_24dp,
                valueText = numMembers,
                labelText = stringResource(R.string.members)
            )
        }

        if (uiState.clazzCodeVisible) {
            item {
                UstadDetailField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    imageId = R.drawable.ic_login_24px,
                    valueText = uiState.clazz?.clazzCode ?: "",
                    labelText = stringResource(R.string.class_code),
                    onClick = {
                        onClickClassCode(uiState.clazz?.clazzCode ?: "")
                    }
                )
            }
        }

        if (uiState.clazzSchoolUidVisible){
            item {
                UstadDetailField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    imageId = R.drawable.ic_school_black_24dp,
                    valueText = uiState.clazz?.clazzSchool?.schoolName ?: "",
                    labelText = null,
                )
            }
        }

        if (uiState.clazzDateVisible){
            item {
                UstadDetailField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    imageId = R.drawable.ic_event_black_24dp,
                    valueText = clazzDateRange,
                    labelText = "${stringResource(R.string.start_date)} - ${stringResource(R.string.end_date)}",
                )
            }
        }

        if (uiState.clazzHolidayCalendarVisible){
            item {
                UstadDetailField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    imageId = R.drawable.ic_event_black_24dp,
                    valueText = uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: "",
                    labelText = stringResource(R.string.holiday_calendar),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        if(uiState.scheduleList.isNotEmpty()) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    text = stringResource(id = R.string.schedule)
                )
            }
        }

        items(
            items = uiState.scheduleList,
            key = { Pair(1, it.scheduleUid) }
        ){ schedule ->
            val fromTimeFormatted = rememberFormattedTime(
                timeInMs = schedule.sceduleStartTime.toInt()
            )
            val toTimeFormatted = rememberFormattedTime(
                timeInMs = schedule.scheduleEndTime.toInt()
            )
            val text = "${messageIdResource(id = schedule.scheduleFrequency)} " +
                    " ${messageIdResource(schedule.scheduleDay)} " +
                    " $fromTimeFormatted - $toTimeFormatted "

            ListItem(
                text = { Text(text) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(
            items = lazyPagingItems,
            key = { Pair(2, it.cbUid) }
        ){ courseBlock ->
            CourseBlockListItem(
                courseBlock = courseBlock,
                onClick = {
                    courseBlock?.also { onClickCourseBlock(it) }
                },
            )
        }

        item {
            Spacer(Modifier.height(128.dp))
        }
    }
}

val ICON_SIZE = 40.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseBlockListItem(
    courseBlock: CourseBlockWithCompleteEntity?,
    onClick: () -> Unit,
){

    val descriptionPlainText = remember(courseBlock?.cbDescription) {
        courseBlock?.cbDescription?.htmlToPlainText() ?: ""
    }

    when(courseBlock?.cbType ?: 0){
        CourseBlock.BLOCK_MODULE_TYPE  -> {

            val trailingIcon = if(courseBlock?.expanded != false)
                Icons.Default.KeyboardArrowUp
            else
                Icons.Default.KeyboardArrowDown
            ListItem(
                modifier = Modifier
                    .paddingCourseBlockIndent(courseBlock?.cbIndentLevel ?: 0)
                    .clickable(onClick = onClick),
                text = { Text(courseBlock?.cbTitle ?: "") },
                secondaryText = { Text(courseBlock?.cbDescription ?: "") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_folder_open_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                },
                trailing = {
                    Icon(trailingIcon, contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_DISCUSSION_TYPE -> {
            ListItem(
                modifier = Modifier
                    .paddingCourseBlockIndent(courseBlock?.cbIndentLevel ?: 0)
                    .clickable(onClick = onClick),
                text = { Text(courseBlock?.cbTitle ?: "") },
                secondaryText = {
                    Text(
                        text = descriptionPlainText,
                        maxLines = 1,
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_forum_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_TEXT_TYPE -> {
            ListItem(
                modifier = Modifier
                    .paddingCourseBlockIndent(courseBlock?.cbIndentLevel ?: 0)
                    .clickable(onClick = onClick),
                text = { Text(courseBlock?.cbTitle ?: "") },
                secondaryText = {
                    Text(
                        text = descriptionPlainText,
                        maxLines = 1,
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_title_24),
                        modifier = Modifier.size(ICON_SIZE),
                        contentDescription = "")
                }
            )
        }
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
            courseBlock?.assignment?.also {
                UstadClazzAssignmentListItem(
                    courseBlock = courseBlock,
                    onClick = onClick,
                )
            }
        }
        CourseBlock.BLOCK_CONTENT_TYPE -> {
            courseBlock?.entry?.also {
                UstadContentEntryListItem(
                    contentEntry = it,
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
fun ClazzDetailOverviewScreen(viewModel: ClazzDetailOverviewViewModel) {
    val uiState: ClazzDetailOverviewUiState by viewModel.uiState.collectAsState(
        ClazzDetailOverviewUiState()
    )

    ClazzDetailOverviewScreen(
        uiState = uiState,
        onClickCourseBlock = viewModel::onClickCourseBlock,
        onClickPermissions = viewModel::onClickPermissions,
    )

}

@Composable
@Preview
fun ClazzDetailOverviewScreenPreview() {
    val uiState = ClazzDetailOverviewUiState(
        clazz = ClazzWithDisplayDetails().apply {
            clazzDesc = "Description"
            clazzCode = "abc123"
            clazzSchoolUid = 1
            clazzStartTime = 1682074513000
            clazzEndTime = 1713682513000
            clazzSchool = School().apply {
                schoolName = "School Name"
            }
            clazzHolidayCalendar = HolidayCalendar().apply {
                umCalendarName = "Holiday Calendar"
            }
        },
        scheduleList = listOf(
            Schedule().apply {
                scheduleUid = 1
                sceduleStartTime = 0
                scheduleEndTime = 0
                scheduleFrequency = MessageID.yearly
                scheduleDay = MessageID.sunday
            },
            Schedule().apply {
                scheduleUid = 2
                sceduleStartTime = 0
                scheduleEndTime = 0
                scheduleFrequency = MessageID.yearly
                scheduleDay = MessageID.sunday
            }
        ),
        courseBlockList = {
            ListPagingSource(
                listOf(
                    CourseBlockWithCompleteEntity().apply {
                        cbUid = 1
                        cbTitle = "Module"
                        cbDescription = "Description"
                        cbType = CourseBlock.BLOCK_MODULE_TYPE
                    },
                    CourseBlockWithCompleteEntity().apply {
                        cbUid = 2
                        cbTitle = "Main discussion board"
                        cbType = CourseBlock.BLOCK_DISCUSSION_TYPE
                    },
                    CourseBlockWithCompleteEntity().apply {
                        cbUid = 3
                        cbDescription = "Description"
                        cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                        assignment = ClazzAssignmentWithMetrics().apply {
                            caTitle = "Assignment"
                            fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                            progressSummary = AssignmentProgressSummary().apply {
                                submittedStudents = 5
                                markedStudents = 10
                            }
                        }
                    },
                    CourseBlockWithCompleteEntity().apply {
                        cbUid = 4
                        cbType = CourseBlock.BLOCK_CONTENT_TYPE
                        entry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                            title = "Content Entry"
                            scoreProgress = ContentEntryStatementScoreProgress().apply {
                                success = StatementEntity.RESULT_SUCCESS
                                progress = 70
                            }
                        }
                    },
                    CourseBlockWithCompleteEntity().apply {
                        cbUid = 5
                        cbTitle = "Text Block Module"
                        cbDescription = "<pre>\n" +
                            "            GeeksforGeeks\n" +
                            "                         A Computer   Science Portal   For Geeks\n" +
                            "        </pre>"
                        cbType = CourseBlock.BLOCK_TEXT_TYPE
                    }
                )
            )
        },
        clazzCodeVisible = true
    )
    MdcTheme {
        ClazzDetailOverviewScreen(uiState)
    }
}