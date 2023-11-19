package com.ustadmobile.libuicompose.view.clazz.detailoverview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Title
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.HtmlText
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateRange
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import com.ustadmobile.libuicompose.view.clazzassignment.UstadClazzAssignmentListItem
import com.ustadmobile.libuicompose.view.contententry.UstadContentEntryListItem
import com.ustadmobile.libuicompose.view.clazz.paddingCourseBlockIndent

val ICON_SIZE = 40.dp

@Composable
fun ClazzDetailOverviewScreen(viewModel: ClazzDetailOverviewViewModel) {
    val uiState: ClazzDetailOverviewUiState by viewModel.uiState.collectAsState(
        ClazzDetailOverviewUiState()
    )

    ClazzDetailOverviewScreen(
        uiState = uiState,
        onClickCourseBlock = viewModel::onClickCourseBlock,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzDetailOverviewScreen(
    uiState: ClazzDetailOverviewUiState = ClazzDetailOverviewUiState(),
    onClickClassCode: (String) -> Unit = {},
    onClickCourseBlock: (CourseBlock) -> Unit = {},
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
) {
    //  TODO error
//    val pager = remember(uiState.courseBlockList) {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.courseBlockList,
//        )
//    }

    //  TODO error
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val numMembers = stringResource(MR.strings.x_teachers_y_students,
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
            HtmlText(
                html = uiState.clazz?.clazzDesc ?: "",
                modifier = Modifier.defaultItemPadding()
            )
        }

        item {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                icon = {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = null
                    )
                },
                text = {
                    Text(numMembers)},
                secondaryText = { Text(stringResource(MR.strings.members_key)) }
            )
        }

        if (uiState.clazzCodeVisible) {
            item {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding()
                        .clickable(
                            onClick = {
                                onClickClassCode(uiState.clazz?.clazzCode ?: "")
                            }
                        ),
                    icon = {
                        Icon(
                            Icons.Filled.Login,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(uiState.clazz?.clazzCode ?: "")},
                    secondaryText = { stringResource(MR.strings.class_code) }
                )
            }
        }

        if (uiState.clazzSchoolUidVisible){
            item {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    icon = {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null
                        )
                    },
                    text = { Text(uiState.clazz?.clazzSchool?.schoolName ?: "")}
                )
            }
        }

        if (uiState.clazzDateVisible){
            item {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    icon = {
                        Icon(
                            Icons.Filled.Event,
                            contentDescription = null
                        )
                    },
                    text = { Text(clazzDateRange)},
                    secondaryText = {
                        Text("${stringResource(MR.strings.start_date)} - " +
                                stringResource(MR.strings.end_date)
                        )
                    }
                )
            }
        }

        if (uiState.clazzHolidayCalendarVisible){
            item {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    icon = {
                        Icon(
                            Icons.Filled.Event,
                            contentDescription = null
                        )
                    },
                    text = { Text(uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: "")},
                    secondaryText = {
                        Text(stringResource(MR.strings.holiday_calendar))
                    }
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
                    text = stringResource(MR.strings.schedule)
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
            val text = buildString {
                append(stringIdMapResource(
                    map = ClazzScheduleConstants.SCHEDULE_FREQUENCY_STRING_RESOURCES,
                    key = schedule.scheduleFrequency)
                )
                append(" ")
                append(stringIdMapResource(
                    map = ClazzScheduleConstants.DAY_STRING_RESOURCES,
                    key = schedule.scheduleDay
                ))
                append(" $fromTimeFormatted - $toTimeFormatted ")

            }

            ListItem(
                text = { Text(text) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        //  TODO error
//        items(
//            items = lazyPagingItems,
//            key = { Pair(2, it.cbUid) }
//        ){ courseBlock ->
//            CourseBlockListItem(
//                courseBlock = courseBlock,
//                onClick = {
//                    courseBlock?.also { onClickCourseBlock(it) }
//                },
//            )
//        }

        item {
            Spacer(Modifier.height(128.dp))
        }
    }
}

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
                        Icons.Default.FolderOpen,
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
                        Icons.Filled.Forum,
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
                        Icons.Filled.Title,
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
