package com.ustadmobile.libuicompose.view.clazz.detailoverview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.UstadAsyncImage
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateRange
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import com.ustadmobile.libuicompose.view.clazz.paddingCourseBlockIndent
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.libuicompose.view.clazz.iconContent
import com.ustadmobile.libuicompose.view.clazz.painterForDefaultCourseImage

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
) {
    val pager = remember(uiState.courseBlockList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.courseBlockList,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val clazzDateRange = rememberFormattedDateRange(
        startTimeInMillis = uiState.clazz?.clazzStartTime ?: 0L,
        endTimeInMillis = uiState.clazz?.clazzEndTime ?: UNSET_DISTANT_FUTURE,
        timeZoneId = uiState.clazz?.clazzTimeZone ?: "UTC",
    )

    val courseBannerUri = uiState.clazz?.coursePicture?.coursePictureUri

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ){
        item(key = "banner") {
            if(courseBannerUri != null){
                UstadAsyncImage(
                    uri = courseBannerUri,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(156.dp).fillMaxWidth()
                )
            }else {
                Image(
                    painter = painterForDefaultCourseImage(uiState.clazz?.clazzName),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(156.dp).fillMaxWidth(),
                )
            }
        }

        item(key = "clazz_desc") {
            UstadHtmlText(
                html = uiState.clazz?.clazzDesc ?: "",
                modifier = Modifier.defaultItemPadding()
            )
            Divider(Modifier.height(1.dp))
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
                    valueText = uiState.clazz?.clazzCode ?: "",
                    labelText = stringResource(MR.strings.class_code),
                    icon = Icons.Filled.Login
                )
            }
        }

        if (uiState.clazzSchoolUidVisible){
            item(key = "schoolname") {
                UstadDetailField2(
                    valueText = uiState.clazz?.clazzSchool?.schoolName ?: "",
                    labelText = "",
                    icon = Icons.Filled.School
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
            Divider(Modifier.height(1.dp))
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
            Divider(thickness = 1.dp)
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
            Text(descriptionPlainText)
        },
        leadingContent = {
            courseBlock?.courseBlock?.iconContent()
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
