package com.ustadmobile.libuicompose.view.clazz.detailoverview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.blockTypeStringResource
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.getScoreInPointsStr
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.UstadAsyncImage
import com.ustadmobile.libuicompose.components.UstadBlockIcon
import com.ustadmobile.libuicompose.components.UstadBlockStatusProgressBar
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateRange
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import com.ustadmobile.libuicompose.view.clazz.blockTypeImageVector
import com.ustadmobile.libuicompose.view.clazz.paddingCourseBlockIndent
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.libuicompose.view.clazz.painterForDefaultCourseImage
import com.ustadmobile.libuicompose.view.contententry.contentTypeImageVector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ClazzDetailOverviewScreen(viewModel: ClazzDetailOverviewViewModel) {
    val uiState: ClazzDetailOverviewUiState by viewModel.uiState.collectAsState(
        ClazzDetailOverviewUiState()
    )

    ClazzDetailOverviewScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.listRefreshCommandFlow,
        onClickCourseBlock = viewModel::onClickCourseBlock,
        onClickClassCode = viewModel::onClickClazzCode,
        onClickPermissions = viewModel::onClickPermissions,
    )
}

@Composable
fun ClazzDetailOverviewScreen(
    uiState: ClazzDetailOverviewUiState = ClazzDetailOverviewUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = emptyFlow(),
    onClickClassCode: (String) -> Unit = {},
    onClickCourseBlock: (CourseBlock) -> Unit = {},
    onClickPermissions: () -> Unit = { },
) {
    val mediatorResult = rememberDoorRepositoryPager(
        uiState.courseBlockList, refreshCommandFlow,
    )

    val lazyPagingItems = mediatorResult.lazyPagingItems

    val clazzDateRange = rememberFormattedDateRange(
        startTimeInMillis = uiState.clazz?.clazzStartTime ?: 0L,
        endTimeInMillis = uiState.clazz?.clazzEndTime ?: UNSET_DISTANT_FUTURE,
        timeZoneId = uiState.clazz?.clazzTimeZone ?: "UTC",
    )

    val courseBannerUri = uiState.clazz?.coursePicture?.coursePictureUri

    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ){
        item(key = "banner") {
            if(courseBannerUri != null){
                UstadAsyncImage(
                    uri = courseBannerUri,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(156.dp).fillMaxWidth().testTag("course_banner")
                )
            }else {
                Image(
                    painter = painterForDefaultCourseImage(uiState.clazz?.clazzName),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(156.dp).fillMaxWidth().testTag("default_banner"),
                )
            }
        }

        if(uiState.quickActionBarVisible) {
            item(key = "quick_action_row") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row {
                        if(uiState.managePermissionVisible) {
                            UstadQuickActionButton(
                                imageVector = Icons.Default.Shield,
                                labelText = stringResource(MR.strings.permissions),
                                onClick = onClickPermissions,
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp)
                }
            }
        }

        item(key = "clazz_desc") {
            UstadHtmlText(
                html = uiState.clazz?.clazzDesc ?: "",
                modifier = Modifier.defaultItemPadding()
            )
            HorizontalDivider(thickness = 1.dp)
        }

        item(key = "members") {
            UstadDetailField2(
                valueText = uiState.membersString ,
                labelText = stringResource(MR.strings.members_key).capitalizeFirstLetter(),
                icon = Icons.Filled.Group,
            )
        }

        if (uiState.clazzCodeVisible) {
            item(key = "clazzcode") {
                UstadDetailField2(
                    modifier = Modifier.clickable { onClickClassCode(uiState.clazz?.clazzCode ?: "") },
                    valueContent = { Text(uiState.clazz?.clazzCode ?: "") },
                    labelContent = { Text(stringResource(MR.strings.invite_code)) },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    },
                )
            }
        }

        if (uiState.clazzDateVisible){
            item(key = "daterange") {
                UstadDetailField2(
                    valueText = clazzDateRange,
                    labelText = "${stringResource(MR.strings.start_date)} - " +
                            stringResource(MR.strings.end_date),
                    icon = Icons.Filled.Event
                )
            }
        }

        if (uiState.clazzHolidayCalendarVisible){
            item(key = "holcal") {
                UstadDetailField2(
                    valueText = uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: "",
                    labelText = stringResource(MR.strings.holiday_calendar),
                    icon = Icons.Filled.Event
                )
            }
        }

        item {
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if(uiState.scheduleList.isNotEmpty()) {
            item(key = "scheduleheader") {
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
                headlineContent = { Text(text) },
            )
        }

        item(key = "blockdivider") {
            HorizontalDivider(thickness = 1.dp)
        }

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.courseBlock?.cbUid ?: -1 }
        ) {
            CourseBlockListItem(
                courseBlock = it,
                onClick = {
                    it?.courseBlock?.also(onClickCourseBlock)
                }
            )
        }
    }
}

@Composable
fun CourseBlockListItem(
    courseBlock: CourseBlockAndDisplayDetails?,
    onClick: () -> Unit,
){

    val descriptionPlainText = remember(courseBlock?.courseBlock?.cbDescription) {
        courseBlock?.courseBlock?.cbDescription?.htmlToPlainText() ?: ""
    }

    ListItem(
        modifier = Modifier.clickable {
            onClick()
        }
        .paddingCourseBlockIndent(courseBlock?.courseBlock?.cbIndentLevel ?: 0),
        headlineContent = {
            Text(courseBlock?.courseBlock?.cbTitle ?: "")
        },
        supportingContent = {
            val contentEntryVal = courseBlock?.contentEntry
            val courseBlockVal = courseBlock?.courseBlock
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        contentEntryVal != null -> {
                            Icon(contentEntryVal.contentTypeImageVector, "",
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(contentEntryVal.contentTypeStringResource))
                        }

                        courseBlockVal != null -> {
                            courseBlockVal.blockTypeImageVector?.also {
                                Icon(it, "", modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(courseBlockVal.blockTypeStringResource))
                        }
                    }
                }
                Text(descriptionPlainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                courseBlock?.getScoreInPointsStr()?.also { scoreInPoints ->
                    Row {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("$scoreInPoints/${courseBlockVal?.cbMaxPoints} ${stringResource(MR.strings.points)}")
                    }
                }
            }
        },
        leadingContent = {
            Box(
                Modifier.size(40.dp)
            ) {
                UstadBlockIcon(
                    title = courseBlock?.courseBlock?.cbTitle ?: "",
                    courseBlock = courseBlock?.courseBlock,
                    contentEntry = courseBlock?.contentEntry,
                    pictureUri = courseBlock?.courseBlockPicture?.cbpThumbnailUri
                        ?: courseBlock?.contentEntryPicture2?.cepThumbnailUri,
                )



                UstadBlockStatusProgressBar(
                    blockStatus = courseBlock?.status,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

        },
        trailingContent = {
            if(courseBlock?.courseBlock?.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
                val trailingIcon = if(courseBlock.expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown

                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = stringResource(
                            if(courseBlock.expanded)
                                MR.strings.collapse
                            else
                                MR.strings.expand
                        )
                    )
                }

            }
        }
    )
}
