package com.ustadmobile.port.android.view.clazz.edit

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.editIconId
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.compose.stringIdMapResource
import com.ustadmobile.port.android.util.compose.stringIdOptionListResource
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.*
import org.burnoutcrew.reorderable.*
import java.util.*
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.contententry.list.ContentEntryList2Fragment
import com.ustadmobile.port.android.view.TitleDescBottomSheetOption
import com.ustadmobile.port.android.view.TitleDescBottomSheetOptionFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazz.edit.ClazzEditFragment.Companion.ADD_COURSE_BLOCK_OPTIONS
import com.ustadmobile.port.android.view.clazz.edit.ClazzEditFragment.Companion.BLOCK_AND_ENTRY_ICON_MAP
import kotlinx.parcelize.Parcelize
import com.ustadmobile.core.R as CR

class ClazzEditFragment : UstadBaseMvvmFragment() {

    private var bottomSheetOptionList: List<TitleDescBottomSheetOption> = listOf()

    private val viewModel: ClazzEditViewModel by ustadViewModels(::ClazzEditViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzEditScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetOptionList = listOf(
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.module),
                        requireContext().getString(CR.string.course_module),
                        CourseBlock.BLOCK_MODULE_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.text),
                        requireContext().getString(CR.string.formatted_text_to_show_to_course_participants),
                        CourseBlock.BLOCK_TEXT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.content),
                        requireContext().getString(CR.string.add_course_block_content_desc),
                        CourseBlock.BLOCK_CONTENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.clazz_assignment),
                        requireContext().getString(CR.string.add_assignment_block_content_desc),
                        CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.discussion_board),
                        requireContext().getString(CR.string.add_discussion_board_desc),
                        CourseBlock.BLOCK_DISCUSSION_TYPE),
        )

    }


    companion object {

        @JvmField
        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

        @JvmField
        val BLOCK_AND_ENTRY_ICON_MAP = BLOCK_ICON_MAP + ContentEntryList2Fragment.CONTENT_ENTRY_TYPE_ICON_MAP

        val ADD_COURSE_BLOCK_OPTIONS: (Context) ->  List<TitleDescBottomSheetOption> = { context ->
            listOf(
                TitleDescBottomSheetOption(
                    context.getString(CR.string.module),
                    context.getString(CR.string.course_module),
                    CourseBlock.BLOCK_MODULE_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.text),
                    context.getString(CR.string.formatted_text_to_show_to_course_participants),
                    CourseBlock.BLOCK_TEXT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.content),
                    context.getString(CR.string.add_course_block_content_desc),
                    CourseBlock.BLOCK_CONTENT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.assignments),
                    context.getString(CR.string.add_assignment_block_content_desc),
                    CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.discussion_board),
                    context.getString(CR.string.add_discussion_board_desc),
                    CourseBlock.BLOCK_DISCUSSION_TYPE),
            )
        }

    }


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
private fun ClazzEditScreen(
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
            //validate the result
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
                UstadEditHeader(stringResource(CR.string.course_blocks))
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
                    text = { Text(stringResource(id = CR.string.add_block)) },
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
                                painterResource(
                                    id = BLOCK_AND_ENTRY_ICON_MAP[courseBlock.editIconId]
                                        ?: R.drawable.text_doc_24px),
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
            UstadEditHeader(text = stringResource(id = CR.string.schedule))
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddSchedule()
                },
                text = { Text(stringResource(id = CR.string.add_a_schedule)) },
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
                            contentDescription = stringResource(CR.string.delete),
                        )
                    }
                }
            )
        }

        item {
            UstadEditHeader(text = stringResource(id = CR.string.course_setup))
        }

        item {
            UstadClickableTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                label = { Text(stringResource(id = CR.string.timezone)) },
                value = uiState.entity?.clazzTimeZone ?: "",
                onClick = onClickTimezone,
                enabled = uiState.fieldsEnabled,
                onValueChange = { }
            )
        }

        item {
            UstadSwitchField(
                label = stringResource(id = CR.string.attendance),
                checked = uiState.clazzEditAttendanceChecked,
                onChange = { onCheckedAttendance(it) },
                enabled = uiState.fieldsEnabled,
                modifier = Modifier.defaultItemPadding(),
            )
        }

        item {
            UstadClickableTextField(
                value = uiState.entity?.terminology?.ctTitle ?: "",
                label = { Text(stringResource(id = CR.string.terminology)) },
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
        UstadEditHeader(text = stringResource(id = CR.string.basic_details))

        UstadInputFieldLayout (
            errorText = uiState.clazzNameError,
            captionIfNoError = {
                Text(stringResource(CR.string.required))
            }
        ){
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                value = uiState.entity?.clazzName ?: "",
                label = { Text(stringResource( CR.string.name_key )) },
                enabled = uiState.fieldsEnabled,
                singleLine = true,
                isError = uiState.clazzNameError != null,
                onValueChange = {
                    onClazzChanged(
                        uiState.entity?.shallowCopy {
                            clazzName = it
                        }
                    )
                }
            )
        }

        HtmlClickableTextField(
            html = uiState.entity?.clazzDesc ?: "",
            label = stringResource(CR.string.description),
            onClick = onClickEditDescription,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("description")
        )

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
                    label = { Text(stringResource(id = CR.string.start_date)) } ,
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
                    label = { Text(stringResource(id = CR.string.end_date).addOptionalSuffix()) },
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
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(CR.string.more_options))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if(uiState.showHide) {
                DropdownMenuItem(
                    onClick = { onClickHideBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(id = CR.string.hide))
                }
            }

            if(uiState.showUnhide) {
                DropdownMenuItem(
                    onClick = { onClickUnHideBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(id = CR.string.unhide))
                }
            }

            if(uiState.showIndent) {
                DropdownMenuItem(
                    onClick = { onClickIndentBlockPopupMenu(uiState.courseBlock) }
                ) {
                    Text(stringResource(id = CR.string.indent))
                }
            }

            if(uiState.showUnindent) {
                if (uiState.showUnindent) {
                    DropdownMenuItem(
                        onClick = { onClickUnIndentBlockPopupMenu(uiState.courseBlock) }
                    ) {
                        Text(stringResource(id = CR.string.unindent))
                    }
                }
            }

            DropdownMenuItem(onClick = { onClickDeleteBlockPopupMenu(uiState.courseBlock) }) {
                Text(stringResource(id = CR.string.delete))
            }
        }
    }
}

@Composable
fun ClazzEditScreen(viewModel: ClazzEditViewModel) {

    val uiState: ClazzEditUiState by viewModel.uiState.collectAsState(initial = ClazzEditUiState())

    val context = LocalContext.current

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
            val sheet = TitleDescBottomSheetOptionFragment(
                optionsList = ADD_COURSE_BLOCK_OPTIONS(context),
                onOptionSelected = { option ->
                    viewModel.onAddCourseBlock(option.optionCode)
                }
            )

            sheet.show(context.getContextSupportFragmentManager(), sheet.tag)
        }
    )

}

@Composable
@Preview
fun ClazzEditScreenPreview() {
    var uiState: ClazzEditUiState by remember {
        mutableStateOf(
            ClazzEditUiState(
            entity = ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {

            },
            clazzSchedules = listOf(
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                    scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                    scheduleDay = Schedule.DAY_SUNDAY
                }
            ),
            courseBlockList = listOf(
                CourseBlockWithEntity().apply {
                    cbUid = 1000
                    cbTitle = "Module"
                    cbHidden = true
                    cbType = CourseBlock.BLOCK_MODULE_TYPE
                    cbIndentLevel = 0
                },
                CourseBlockWithEntity().apply {
                    cbUid = 1001
                    cbTitle = "Content"
                    cbHidden = false
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    entry = ContentEntry().apply {
                        contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    }
                    cbIndentLevel = 1
                },
                CourseBlockWithEntity().apply {
                    cbUid = 1002
                    cbTitle = "Assignment"
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbHidden = false
                    cbIndentLevel = 1
                },
            ),
        )
        )
    }


    ClazzEditScreen(
        uiState = uiState,
        onMoveCourseBlock = { fromIndex, toIndex ->
            uiState = uiState.copy(
                courseBlockList = uiState.courseBlockList.toMutableList().apply {
                    val swapFromIndex = indexOfFirst { it.cbUid == fromIndex.key }
                    val swapToIndex = indexOfFirst { it.cbUid == toIndex.key }
                    Collections.swap(this, swapFromIndex, swapToIndex)
                }.toList()
            )
        }
    )


}