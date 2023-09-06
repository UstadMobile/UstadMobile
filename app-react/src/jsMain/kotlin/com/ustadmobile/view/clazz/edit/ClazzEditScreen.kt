package com.ustadmobile.view.clazz.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_MONDAY
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.input
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadEditHeader
import com.ustadmobile.view.components.UstadSwitchField
import com.ustadmobile.wrappers.reacteasysort.LockAxis
import com.ustadmobile.wrappers.reacteasysort.SortableList
import web.cssom.pct
import mui.icons.material.Add
import mui.material.*
import mui.material.List
import mui.system.responsive
import mui.system.sx
import react.*
import com.ustadmobile.util.ext.onTextChange
import web.cssom.Cursor
import js.core.jso
import com.ustadmobile.mui.common.inputCursor
import com.ustadmobile.wrappers.quill.ReactQuill

const val COURSE_BLOCK_DRAG_CLASS = "dragging_course_block"

external interface ClazzEditScreenProps : Props {

    var uiState: ClazzEditUiState

    var onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit

    var onCourseBlockMoved: (from: Int, to: Int) -> Unit

    var onClickSchool: () -> Unit

    var onClickTimezone: () -> Unit

    var onClickAddCourseBlock: () -> Unit

    var onClickAddSchedule: () -> Unit

    var onClickEditSchedule: (Schedule) -> Unit

    var onClickDeleteSchedule: (Schedule) -> Unit

    var onClickHolidayCalendar: () -> Unit

    var onCheckedAttendanceChanged: (Boolean) -> Unit

    var onClickTerminology: () -> Unit

    var onClickHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockWithEntity) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockWithEntity) -> Unit

    var onClickEditCourseBlock: (CourseBlockWithEntity) -> Unit

}

val ClazzEditScreenComponent2 = FC<ClazzEditScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    Container {
        Stack {
            spacing = responsive(2)

            UstadEditHeader {
                + strings[MR.strings.basic_details]
            }

            TextField {
                value = props.uiState.entity?.clazzName ?: ""
                label = ReactNode(strings[MR.strings.name_key])
                id = "clazz_name"
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzName = it
                        }
                    )
                }
            }

            ReactQuill {
                value = props.uiState.entity?.clazzDesc ?: ""
                id = "clazz_desc"
                placeholder = strings[MR.strings.description]
                onChange = {
                    props.onClazzChanged(props.uiState.entity?.shallowCopy {
                        clazzDesc = it
                    })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(3)

                sx {
                    width = 100.pct
                }

                UstadDateField {
                    timeInMillis = props.uiState.entity?.clazzStartTime ?: 0
                    timeZoneId = props.uiState.timeZone
                    label = ReactNode(strings[MR.strings.start_date])
                    error = props.uiState.clazzStartDateError != null
                    helperText = props.uiState.clazzStartDateError?.let { ReactNode(it) }
                    disabled = !props.uiState.fieldsEnabled
                    fullWidth = true
                    id = "clazz_start_time"
                    onChange = {
                        props.onClazzChanged(
                            props.uiState.entity?.shallowCopy {
                                clazzStartTime = it
                            }
                        )
                    }
                }

                UstadDateField {
                    timeInMillis = props.uiState.entity?.clazzEndTime ?: 0
                    timeZoneId = props.uiState.timeZone
                    label = ReactNode(strings[MR.strings.end_date].addOptionalSuffix(strings))
                    error = props.uiState.clazzEndDateError != null
                    helperText = props.uiState.clazzEndDateError?.let { ReactNode(it) }
                    disabled = !props.uiState.fieldsEnabled
                    unsetDefault = Long.MAX_VALUE
                    fullWidth = true
                    id = "clazz_end_time"
                    onChange = {
                        props.onClazzChanged(
                            props.uiState.entity?.shallowCopy {
                                clazzEndTime = it
                            }
                        )
                    }
                }

            }

            UstadEditHeader {
                + strings[MR.strings.course_blocks]
            }

            List {
                ListItem {
                    key = "0"
                    ListItemButton {
                        onClick =  { props.onClickAddCourseBlock() }
                        id = "add_course_block"
                        ListItemIcon {
                            + Add.create()
                        }
                        ListItemText {
                            primary = ReactNode(strings[MR.strings.add_block])
                        }
                    }
                }

                SortableList {
                    draggedItemClassName = COURSE_BLOCK_DRAG_CLASS
                    lockAxis = LockAxis.y

                    onSortEnd = { oldIndex, newIndex ->
                        props.onCourseBlockMoved(oldIndex, newIndex)
                    }

                    props.uiState.courseBlockList.forEach { courseBlockItem ->
                        CourseBlockListItem {
                            courseBlock = courseBlockItem
                            fieldsEnabled = props.uiState.fieldsEnabled
                            onClickEditCourseBlock = props.onClickEditCourseBlock
                            onClickHideBlockPopupMenu = props.onClickHideBlockPopupMenu
                            onClickUnHideBlockPopupMenu = props.onClickUnHideBlockPopupMenu
                            onClickIndentBlockPopupMenu = props.onClickIndentBlockPopupMenu
                            onClickUnIndentBlockPopupMenu = props.onClickUnIndentBlockPopupMenu
                            onClickDeleteBlockPopupMenu = props.onClickDeleteBlockPopupMenu
                            uiState = props.uiState.courseBlockStateFor(courseBlockItem)
                        }
                    }
                }
            }

            UstadEditHeader {
                + strings[MR.strings.schedule]
            }

            List{

                ListItem {
                    key = "0"
                    ListItemButton {
                        id = "add_schedule_button"
                        onClick = {
                            props.onClickAddSchedule()
                        }

                        ListItemIcon {
                            + Add.create()
                        }

                        ListItemText {
                            primary = ReactNode(strings[MR.strings.add_a_schedule])
                        }
                    }

                }

                props.uiState.clazzSchedules.forEach { scheduleItem ->
                    ScheduleListItem {
                        schedule = scheduleItem
                        key = "${scheduleItem.scheduleUid}"
                        onClickEditSchedule = props.onClickEditSchedule
                        onClickDeleteSchedule = props.onClickDeleteSchedule
                    }
                }
            }

            UstadEditHeader {
                + strings[MR.strings.course_setup]
            }

            TextField {
                sx {
                    input {
                        cursor = Cursor.pointer
                    }
                }
                value = props.uiState.entity?.clazzTimeZone ?: ""
                id = "clazz_timezone"
                label = ReactNode(strings[MR.strings.timezone])
                disabled = !props.uiState.fieldsEnabled
                onClick = { props.onClickTimezone() }
                inputProps = jso {
                    readOnly = true
                }
            }

            UstadSwitchField {
                label = strings[MR.strings.attendance]
                checked = props.uiState.clazzEditAttendanceChecked
                id = "clazz_attendance_switch"
                onChanged = props.onCheckedAttendanceChanged
                enabled = props.uiState.fieldsEnabled
            }

            TextField {
                sx {
                    inputCursor = Cursor.pointer
                }
                value = props.uiState.entity?.terminology?.ctTitle ?: ""
                label = ReactNode(strings[MR.strings.terminology])
                disabled = !props.uiState.fieldsEnabled
                id = "clazz_terminology"
                onClick = {
                    props.onClickTerminology()
                }
                inputProps = jso {
                    readOnly = true
                }
            }
        }
    }
}


val ClazzEditScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(ClazzEditUiState())

    var addCourseBlockDialogVisible by useState { false }

    ClazzEditScreenComponent2 {
        uiState = uiStateVar
        onClazzChanged = viewModel::onEntityChanged
        onCheckedAttendanceChanged = viewModel::onCheckedAttendanceChanged
        onClickAddSchedule = viewModel::onClickAddSchedule
        onClickEditSchedule = viewModel::onClickEditSchedule
        onClickDeleteSchedule = viewModel::onClickDeleteSchedule
        onCourseBlockMoved = viewModel::onCourseBlockMoved
        onClickTimezone = viewModel::onClickTimezone
        onClickHolidayCalendar = viewModel::onClickHolidayCalendar
        onClickTerminology = viewModel::onClickTerminology
        onClickHideBlockPopupMenu = viewModel::onClickHideBlockPopupMenu
        onClickUnHideBlockPopupMenu = viewModel::onClickUnHideBlockPopupMenu
        onClickIndentBlockPopupMenu = viewModel::onClickIndentBlockPopupMenu
        onClickUnIndentBlockPopupMenu = viewModel::onClickUnIndentBlockPopupMenu
        onClickEditCourseBlock = viewModel::onClickEditCourseBlock
        onClickDeleteBlockPopupMenu = viewModel::onClickDeleteCourseBlock
        onClickAddCourseBlock = {
            addCourseBlockDialogVisible = true
        }
    }

    AddCourseBlockDialog {
        open = addCourseBlockDialogVisible
        onClose = { _, _ ->
            addCourseBlockDialogVisible = false
        }
        onClickAddBlock = viewModel::onAddCourseBlock
    }

}


val ClazzEditScreenPreview = FC<Props> {

    var uiStateVar : ClazzEditUiState by useState {
        ClazzEditUiState(
            entity = ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {

            },
            clazzSchedules = listOf(
                Schedule().apply {
                    sceduleStartTime = ((13 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
                    scheduleEndTime = ((15 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
                    scheduleFrequency = SCHEDULE_FREQUENCY_WEEKLY
                    scheduleDay = DAY_MONDAY
                }
            ),
            courseBlockList = listOf(
                CourseBlockWithEntity().apply {
                    cbTitle = "Module"
                    cbHidden = true
                    cbType = CourseBlock.BLOCK_MODULE_TYPE
                    cbIndentLevel = 0
                },
                CourseBlockWithEntity().apply {
                    cbTitle = "Content"
                    cbHidden = false
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    entry = ContentEntry().apply {
                        contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    }
                    cbIndentLevel = 1
                },
                CourseBlockWithEntity().apply {
                    cbTitle = "Assignment"
                    cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                    cbHidden = false
                    cbIndentLevel = 1
                },
            ),
            fieldsEnabled = true
        )
    }

    ClazzEditScreenComponent2 {
        uiState = uiStateVar

        onCourseBlockMoved = {fromIndex, toIndex ->
            uiStateVar = uiStateVar.copy(
                courseBlockList = uiStateVar.courseBlockList.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }.toList()
            )
        }
    }

}