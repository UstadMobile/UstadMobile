package com.ustadmobile.libuicompose.view.clazz.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.editIconId
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import java.util.*
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadClickableTextField
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadSwitchField
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.compose.stringIdOptionListResource
import org.burnoutcrew.reorderable.*

@Suppress("unused") // Pending add to navhost
@Composable
fun ClazzEditScreen(viewModel: ClazzEditViewModel) {

    val uiState: ClazzEditUiState by viewModel.uiState.collectAsState(initial = ClazzEditUiState())

//    val context = LocalContext.current

    ClazzEditScreen(
        uiState = uiState,
        onClazzChanged = viewModel::onEntityChanged,
        onClickTimezone = viewModel::onClickTimezone,
        onCheckedAttendance = viewModel::onCheckedAttendanceChanged,
        onClickEditDescription = viewModel::onClickEditDescription,
        onClickAddSchedule = viewModel::onClickAddSchedule,
        onClickEditSchedule = viewModel::onClickEditSchedule,
        onClickDeleteSchedule = viewModel::onClickDeleteSchedule,
        onClickEditCourseBlock = viewModel::onClickEditCourseBlock,
        onClickHideBlockPopupMenu = viewModel::onClickHideBlockPopupMenu,
        onClickUnHideBlockPopupMenu = viewModel::onClickUnHideBlockPopupMenu,
        onClickIndentBlockPopupMenu = viewModel::onClickIndentBlockPopupMenu,
        onClickUnIndentBlockPopupMenu = viewModel::onClickUnIndentBlockPopupMenu,
        onClickDeleteBlockPopupMenu = viewModel::onClickDeleteCourseBlock,
        onMoveCourseBlock = { from: ItemPosition, to: ItemPosition ->
            viewModel.onCourseBlockMoved(from.index, to.index)
        },
        onClickAddCourseBlock = {
//            val sheet = TitleDescBottomSheetOptionFragment(
//                optionsList = ADD_COURSE_BLOCK_OPTIONS(context),
//                onOptionSelected = { option ->
//                    viewModel.onAddCourseBlock(option.optionCode)
//                }
//            )
//
//            sheet.show(context.getContextSupportFragmentManager(), sheet.tag)
        }
    )

}

@Parcelize
class CourseBlockKey(val cbUid: Long): Parcelable {
    override fun equals(other: Any?): Boolean {
        return other === this || (other as? CourseBlockKey)?.cbUid == cbUid
    }

    override fun hashCode(): Int {
        return cbUid.hashCode()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState = ClazzEditUiState(),
    onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit = {},
    onMoveCourseBlock: (from: ItemPosition, to: ItemPosition) -> Unit = {_, _ -> },
    onClickSchool: () -> Unit = {},
    onClickEditDescription: () -> Unit = {},
    onClickTimezone: () -> Unit = {},
    onClickEditCourseBlock: (CourseBlockWithEntity) -> Unit = {},
    onClickAddCourseBlock: () -> Unit = {},
    onClickAddSchedule: () -> Unit = {},
    onClickEditSchedule: (Schedule) -> Unit = {},
    onClickDeleteSchedule: (Schedule) -> Unit = {},
    onClickHolidayCalendar: () -> Unit = {},
    onCheckedAttendance: (Boolean) -> Unit = {},
    onClickTerminology: () -> Unit = {},
    onClickHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit = {},
    onClickUnHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit = {},
    onClickIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit = {},
    onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit = {},
    onClickDeleteBlockPopupMenu: (CourseBlockWithEntity) -> Unit = {},
) {

    //The number of items in the LazyColumn before the start of CourseBlocks
    val courseBlockIndexOffset = 0

    val reorderLazyListState = rememberReorderableLazyListState(
        onMove = { from, to ->
            onMoveCourseBlock(
                ItemPosition(from.index - courseBlockIndexOffset, from.key),
                ItemPosition(to.index - courseBlockIndexOffset, to.key),
            )
        },
        canDragOver = { draggedOver, dragging ->
            draggedOver.key is CourseBlockKey
        },
        onDragEnd = { start, end  ->
//            validate the result
        }
    )

    LazyColumn(
        state = reorderLazyListState.listState,
        modifier = Modifier
            .reorderable(reorderLazyListState)
            .detectReorderAfterLongPress(reorderLazyListState)
    )  {
        item {
            ReorderableItem(reorderableState = reorderLazyListState, key = 1) {
                ClazzEditBasicDetails(
                    uiState = uiState,
                    onClazzChanged= onClazzChanged,
                    onClickSchool = onClickSchool,
                    onClickTimezone = onClickTimezone,
                    onClickEditDescription = onClickEditDescription,
                )
            }
        }

        item {
            ReorderableItem(reorderableState = reorderLazyListState, key = 2) {
                UstadEditHeader(stringResource(MR.strings.course_blocks))
            }

        }

        item {
            ReorderableItem(reorderableState = reorderLazyListState, key = 3) {
                ListItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onClickAddCourseBlock()
                    },
                    text = { Text(stringResource(MR.strings.add_block)) },
                )
            }

        }

        items (
            items = uiState.courseBlockList,
            key = {  CourseBlockKey(it.cbUid) }
        ) { courseBlock ->
            val courseBlockEditAlpha: Float = if (courseBlock.cbHidden) 0.5F else 1F
            val startPadding = ((courseBlock.cbIndentLevel * 24) + 8).dp
            ReorderableItem(state = reorderLazyListState, key = CourseBlockKey(courseBlock.cbUid)) { dragging ->
                ListItem(
                    modifier = Modifier
                        .clickable {
                            if (!dragging)
                                onClickEditCourseBlock(courseBlock)
                        }
                        .alpha(courseBlockEditAlpha),
                    icon = {
                        Row{
                            Icon(
                                imageVector = Icons.Filled.Reorder,
                                contentDescription = null,
                                modifier = Modifier.detectReorder(reorderLazyListState)
                            )
                            Spacer(modifier = Modifier.width(startPadding))
                            Icon(
                                ClazzEditConstants.BLOCK_AND_ENTRY_ICON_MAP[courseBlock.editIconId]
                                    ?:  Icons.Filled.TextSnippet,
                                contentDescription = null
                            )
                        }
                    },

                    text = { Text(courseBlock.cbTitle ?: "") },
                    trailing = {
                        PopUpMenu(
                            enabled = uiState.fieldsEnabled,
                            uiState = uiState.courseBlockStateFor(courseBlock),
                            onClickHideBlockPopupMenu = onClickHideBlockPopupMenu,
                            onClickUnHideBlockPopupMenu = onClickUnHideBlockPopupMenu,
                            onClickIndentBlockPopupMenu = onClickIndentBlockPopupMenu,
                            onClickUnIndentBlockPopupMenu = onClickUnIndentBlockPopupMenu,
                            onClickDeleteBlockPopupMenu = onClickDeleteBlockPopupMenu,
                        )
                    }
                )
            }

        }

        item {
            UstadEditHeader(text = stringResource(MR.strings.schedule))
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddSchedule()
                },
                text = { Text(stringResource(MR.strings.add_a_schedule)) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "",
                    )
                }
            )
        }

        items(
            uiState.clazzSchedules,
            key = { it.scheduleUid },
        ){ schedule ->

            val fromTimeFormatted = rememberFormattedTime(timeInMs = schedule.sceduleStartTime.toInt())
            val toTimeFormatted = rememberFormattedTime(timeInMs = schedule.scheduleEndTime.toInt())
            val text = stringIdMapResource(
                map = ClazzScheduleConstants.SCHEDULE_FREQUENCY_STRING_RESOURCES,
                key = schedule.scheduleFrequency
            ) + " " + stringIdOptionListResource(
                options = ScheduleConstants.DAY_MESSAGE_IDS,
                key = schedule.scheduleDay
            ) +  " $fromTimeFormatted - $toTimeFormatted "

            ListItem(
                modifier = Modifier.clickable {
                    onClickEditSchedule(schedule)
                },
                icon = {
                    Spacer(Modifier.width(24.dp))
                },
                text = { Text(text) },
                trailing = {
                    IconButton(
                        onClick = { onClickDeleteSchedule(schedule) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(MR.strings.delete),
                        )
                    }
                }
            )
        }

        item {
            UstadEditHeader(text = stringResource(MR.strings.course_setup))
        }

        item {
            UstadClickableTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                label = { Text(stringResource(MR.strings.timezone)) },
                value = uiState.entity?.clazzTimeZone ?: "",
                onClick = onClickTimezone,
                enabled = uiState.fieldsEnabled,
                onValueChange = { }
            )
        }

        item {
            UstadSwitchField(
                label = stringResource(MR.strings.attendance),
                checked = uiState.clazzEditAttendanceChecked,
                onChange = { onCheckedAttendance(it) },
                enabled = uiState.fieldsEnabled,
                modifier = Modifier.defaultItemPadding(),
            )
        }

        item {
            UstadClickableTextField(
                value = uiState.entity?.terminology?.ctTitle ?: "",
                label = { Text(stringResource(MR.strings.terminology)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {},
                onClick = onClickTerminology,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
            )
        }
    }
}

@Composable
private fun ClazzEditBasicDetails(
    uiState: ClazzEditUiState,
    onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit = {},
    onClickSchool: () -> Unit = {},
    onClickTimezone: () -> Unit = {},
    onClickEditDescription: () -> Unit = {},
) {
    Column(
        modifier = Modifier.defaultScreenPadding()
    ) {
        UstadEditHeader(text = stringResource(MR.strings.basic_details))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.entity?.clazzName ?: "",
            label = { Text(stringResource(MR.strings.name_key )) },
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            onValueChange = {
                onClazzChanged(
                    uiState.entity?.shallowCopy {
                        clazzName = it
                    }
                )
            }
        )

        // TODO error
//        HtmlClickableTextField(
//            html = uiState.entity?.clazzDesc ?: "",
//            label = stringResource(MR.strings.description),
//            onClick = onClickEditDescription,
//            modifier = Modifier
//                .fillMaxWidth()
//                .testTag("description")
//        )

        Row {
            UstadInputFieldLayout(
                modifier = Modifier
                    .weight(1f)
                    .defaultItemPadding(end = 8.dp),
                errorText = uiState.clazzStartDateError
            ) {
                UstadDateField(
                    value = uiState.entity?.clazzStartTime ?: 0,
                    modifier = Modifier.testTag("start_date"),
                    label = { Text(stringResource(MR.strings.start_date)) } ,
                    isError = uiState.clazzStartDateError != null,
                    enabled = uiState.fieldsEnabled,
                    timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                    onValueChange = {
                        onClazzChanged(
                            uiState.entity?.shallowCopy {
                                clazzStartTime = it
                            }
                        )
                    }
                )
            }

            UstadInputFieldLayout(
                modifier = Modifier
                    .weight(1f)
                    .defaultItemPadding(start = 8.dp),
                errorText = uiState.clazzEndDateError
            ) {
                UstadDateField(
                    value = uiState.entity?.clazzEndTime ?: 0,
                    modifier = Modifier.testTag("end_date"),
                    label = { Text(stringResource(MR.strings.end_date)) },
                    isError = uiState.clazzEndDateError != null,
                    enabled = uiState.fieldsEnabled,
                    unsetDefault = UNSET_DISTANT_FUTURE,
                    timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                    onValueChange = {
                        onClazzChanged(
                            uiState.entity?.shallowCopy {
                                clazzEndTime = it
                            }
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun PopUpMenu(
    enabled: Boolean,
    uiState: ClazzEditUiState.CourseBlockUiState,
    onClickHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit,
    onClickUnHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit,
    onClickIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit,
    onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit,
    onClickDeleteBlockPopupMenu: (CourseBlockWithEntity) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(MR.strings.more_options))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if(uiState.showHide) {
                DropdownMenuItem(
                    onClick = { onClickHideBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(MR.strings.hide))
                }
            }

            if(uiState.showUnhide) {
                DropdownMenuItem(
                    onClick = { onClickUnHideBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(MR.strings.unhide))
                }
            }

            if(uiState.showIndent) {
                DropdownMenuItem(
                    onClick = { onClickIndentBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(MR.strings.indent))
                }
            }

            if(uiState.showUnindent) {
                if (uiState.showUnindent) {
                    DropdownMenuItem(
                        onClick = { onClickUnIndentBlockPopupMenu(uiState.courseBlock) }
                    ) {
                        Text(stringResource(MR.strings.unindent))
                    }
                }
            }

            DropdownMenuItem(onClick = { onClickDeleteBlockPopupMenu(uiState.courseBlock) }) {
                Text(stringResource(MR.strings.delete))
            }
        }
    }
}