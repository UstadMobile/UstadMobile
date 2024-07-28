package com.ustadmobile.libuicompose.view.clazz.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadBlockIcon
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadClickableTextField
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadImageSelectButton
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.components.UstadSwitchField
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.compose.stringIdOptionListResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClazzEditScreen(viewModel: ClazzEditViewModel) {

    val uiState: ClazzEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = ClazzEditUiState(), context = Dispatchers.Main.immediate
    )

    var newCourseBlockSheetVisible by remember {
        mutableStateOf(false)
    }

    fun Modifier.addCourseBlockClickable(
        blockType: Int
    ) : Modifier = clickable {
        newCourseBlockSheetVisible = false
        viewModel.onAddCourseBlock(blockType)
    }

    if(newCourseBlockSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                newCourseBlockSheetVisible = false
            },
        ){
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                text = stringResource(MR.strings.add_block)
            )

            HorizontalDivider(thickness = 1.dp)

            Column(
                Modifier.verticalScroll(
                    state = rememberScrollState()
                ).fillMaxSize()
            ) {
                UstadBottomSheetOption(
                    modifier = Modifier.addCourseBlockClickable(CourseBlock.BLOCK_MODULE_TYPE),
                    headlineContent = { Text(stringResource(MR.strings.module)) },
                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                    secondaryContent = { Text(stringResource(MR.strings.course_module)) },
                )
                UstadBottomSheetOption(
                    modifier = Modifier.addCourseBlockClickable(CourseBlock.BLOCK_TEXT_TYPE),
                    headlineContent = { Text(stringResource(MR.strings.text)) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null) },
                    secondaryContent = { Text(stringResource(MR.strings.formatted_text_to_show_to_course_participants ))},
                )
                UstadBottomSheetOption(
                    modifier = Modifier.addCourseBlockClickable(CourseBlock.BLOCK_CONTENT_TYPE),
                    headlineContent = { Text(stringResource(MR.strings.content)) },
                    leadingContent = { Icon(Icons.Default.Collections, contentDescription = null) },
                    secondaryContent = { Text(stringResource(MR.strings.add_course_block_content_desc)) },
                )
                UstadBottomSheetOption(
                    modifier = Modifier.addCourseBlockClickable(CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                    headlineContent = { Text(stringResource(MR.strings.assignment)) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)},
                    secondaryContent = { Text(stringResource(MR.strings.add_assignment_block_content_desc))}
                )
                UstadBottomSheetOption(
                    modifier = Modifier.addCourseBlockClickable(CourseBlock.BLOCK_DISCUSSION_TYPE),
                    headlineContent = { Text(stringResource(MR.strings.discussion_board)) },
                    leadingContent = { Icon(Icons.Default.Forum, contentDescription = null) },
                    secondaryContent = { Text(stringResource(MR.strings.add_discussion_board_desc)) }
                )
            }

        }
    }

    ClazzEditScreen(
        uiState = uiState,
        onClazzChanged = viewModel::onEntityChanged,
        onClickTimezone = viewModel::onClickTimezone,
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
            newCourseBlockSheetVisible = true
        },
        onClickTerminology = viewModel::onClickTerminology,
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

@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState = ClazzEditUiState(),
    onClazzChanged: (ClazzWithHolidayCalendarAndAndTerminology?) -> Unit = {},
    onMoveCourseBlock: (from: ItemPosition, to: ItemPosition) -> Unit = {_, _ -> },
    onClickEditDescription: () -> Unit = {},
    onClickTimezone: () -> Unit = {},
    onClickEditCourseBlock: (CourseBlockAndEditEntities) -> Unit = {},
    onClickAddCourseBlock: () -> Unit = {},
    onClickAddSchedule: () -> Unit = {},
    onClickEditSchedule: (Schedule) -> Unit = {},
    onClickDeleteSchedule: (Schedule) -> Unit = {},
    onCheckedAttendance: (Boolean) -> Unit = {},
    onClickTerminology: () -> Unit = {},
    onClickHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit = {},
    onClickUnHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit = {},
    onClickIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit = {},
    onClickUnIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit = {},
    onClickDeleteBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit = {},
) {

    //The number of items in the LazyColumn before the start of CourseBlocks
    //There is: basic details, course block headers, add course block button
    val courseBlockIndexOffset = 3

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

    UstadLazyColumn(
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
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onClickAddCourseBlock()
                    }.testTag("add_block_button"),
                    headlineContent = { Text(stringResource(MR.strings.add_block)) },
                )
            }

        }

        items (
            items = uiState.courseBlockList,
            key = {  CourseBlockKey(it.courseBlock.cbUid) }
        ) { block ->
            val courseBlockEditAlpha: Float = if (block.courseBlock.cbHidden) 0.5F else 1F
            val startPadding = ((block.courseBlock.cbIndentLevel * 24) + 8).dp
            ReorderableItem(
                state = reorderLazyListState,
                key = CourseBlockKey(block.courseBlock.cbUid)
            ) { dragging ->
                ListItem(
                    modifier = Modifier
                        .clickable {
                            if (!dragging)
                                onClickEditCourseBlock(block)
                        }
                        .alpha(courseBlockEditAlpha),
                    leadingContent ={
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Reorder,
                                contentDescription = null,
                                modifier = Modifier.detectReorder(reorderLazyListState)
                            )
                            Spacer(modifier = Modifier.width(startPadding))
                            UstadBlockIcon(
                                title = block.courseBlock.cbTitle ?: "",
                                courseBlock = block.courseBlock,
                                contentEntry = block.contentEntry,
                                pictureUri = block.courseBlockPicture?.cbpPictureUri,
                            )
                        }
                    },

                    headlineContent = { Text(block.courseBlock.cbTitle ?: "") },
                    trailingContent = {
                        PopUpMenu(
                            enabled = uiState.fieldsEnabled,
                            uiState = uiState.courseBlockStateFor(block),
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
                }.testTag("add_a_schedule"),
                headlineContent = { Text(stringResource(MR.strings.add_a_schedule)) },
                leadingContent ={
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
                leadingContent ={
                    Spacer(Modifier.width(24.dp))
                },
                headlineContent = { Text(text) },
                trailingContent = {
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
                    .testTag("timezone")
                    .fillMaxWidth()
                    .defaultItemPadding(),
                label = { Text(stringResource(MR.strings.timezone)) },
                value = uiState.entity?.clazzTimeZone ?: "",
                onClick = onClickTimezone,
                enabled = uiState.fieldsEnabled,
                singleLine = true,
                onValueChange = { }
            )
        }

        item {
            UstadSwitchField(
                label = stringResource(MR.strings.attendance),
                checked = uiState.clazzEditAttendanceChecked,
                onChange = { onCheckedAttendance(it) },
                enabled = uiState.fieldsEnabled,
                modifier = Modifier.defaultItemPadding().testTag("attendance"),
            )
        }

        item {
            UstadClickableTextField(
                value = uiState.entity?.terminology?.ctTitle ?: "",
                label = { Text(stringResource(MR.strings.terminology)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {},
                onClick = onClickTerminology,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("terminology")
                    .defaultItemPadding(),
            )
        }
    }
}

@Composable
private fun ClazzEditBasicDetails(
    uiState: ClazzEditUiState,
    onClazzChanged: (ClazzWithHolidayCalendarAndAndTerminology?) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onClickTimezone: () -> Unit = {},
    //Reserved for use when HTML editing is added to compose
    onClickEditDescription: () -> Unit = {},
) {
    Column(
        modifier = Modifier.defaultScreenPadding()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            UstadImageSelectButton(
                imageUri = uiState.entity?.coursePicture?.coursePictureUri,
                onImageUriChanged = { imageUri ->
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            coursePicture = coursePicture?.copy(
                                coursePictureUri = imageUri,
                            )
                        }
                    )
                },
                modifier = Modifier.size(60.dp).testTag("course_picture_button"),
            )
        }

        UstadEditHeader(text = stringResource(MR.strings.basic_details))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("name")
                .defaultItemPadding(),
            value = uiState.entity?.clazzName ?: "",
            label = { Text(stringResource(MR.strings.name_key ) + "*") },
            enabled = uiState.fieldsEnabled,
            isError = uiState.clazzNameError != null,
            singleLine = true,
            supportingText = {
                 Text(uiState.clazzNameError ?: stringResource(MR.strings.required))
            },
            onValueChange = {
                onClazzChanged(
                    uiState.entity?.shallowCopy {
                        clazzName = it
                    }
                )
            }
        )


        UstadRichTextEdit(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("description"),
            html = uiState.entity?.clazzDesc ?: "",
            onClickToEditInNewScreen = onClickEditDescription,
            editInNewScreenLabel = stringResource(MR.strings.description),
            placeholderText = stringResource(MR.strings.description),
            onHtmlChange = {
                uiState.entity?.also { entity ->
                    onClazzChanged(
                        entity.shallowCopy {
                            clazzDesc = it
                        }
                    )
                }
            },
        )

        Row {
            UstadDateField(
                value = uiState.entity?.clazzStartTime ?: 0,
                modifier = Modifier.testTag("start_date").weight(1f)
                    .defaultItemPadding(end = 8.dp),
                label = { Text(stringResource(MR.strings.start_date) + "*") } ,
                isError = uiState.clazzStartDateError != null,
                enabled = uiState.fieldsEnabled,
                timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzStartTime = it
                        }
                    )
                },
                supportingText = {
                    Text(uiState.clazzStartDateError ?: stringResource(MR.strings.required))
                }
            )

            UstadDateField(
                value = uiState.entity?.clazzEndTime ?: 0,
                modifier = Modifier
                    .weight(1f)
                    .defaultItemPadding(start = 8.dp)
                    .testTag("end_date"),
                label = { Text(stringResource(MR.strings.end_date)) },
                isError = uiState.clazzEndDateError != null,
                enabled = uiState.fieldsEnabled,
                unsetDefault = UNSET_DISTANT_FUTURE,
                timeZoneId = uiState.entity?.clazzTimeZone ?: "UTC",
                supportingText = {
                    Text(uiState.clazzEndDateError ?: "")
                },
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


@Composable
private fun PopUpMenu(
    enabled: Boolean,
    uiState: ClazzEditUiState.CourseBlockUiState,
    onClickHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit,
    onClickUnHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit,
    onClickIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit,
    onClickUnIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit,
    onClickDeleteBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit,
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
                    onClick = {
                        expanded = false
                        onClickHideBlockPopupMenu(uiState.block)
                    },
                    text = {Text(stringResource(MR.strings.hide)) }
                )
            }

            if(uiState.showUnhide) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onClickUnHideBlockPopupMenu(uiState.block)
                    },
                    text = { Text(stringResource(MR.strings.unhide)) }
                )
            }

            if(uiState.showIndent) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onClickIndentBlockPopupMenu(uiState.block)
                    },
                    text = { Text(stringResource(MR.strings.indent)) }
                )
            }

            if(uiState.showUnindent) {
                if (uiState.showUnindent) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onClickUnIndentBlockPopupMenu(uiState.block)
                        },
                        text = { Text(stringResource(MR.strings.unindent)) }
                    )
                }
            }

            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onClickDeleteBlockPopupMenu(uiState.block)
                },
                text = {  Text(stringResource(MR.strings.delete)) }
            )
        }
    }
}