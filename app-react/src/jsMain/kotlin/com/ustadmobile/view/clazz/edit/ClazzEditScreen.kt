package com.ustadmobile.view.clazz.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.input
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.view.components.UstadEditHeader
import com.ustadmobile.view.components.UstadSwitchField
import com.ustadmobile.wrappers.reacteasysort.LockAxis
import com.ustadmobile.wrappers.reacteasysort.SortableList
import web.cssom.pct
import mui.icons.material.Add as AddIcon
import mui.material.*
import mui.material.List
import mui.system.responsive
import mui.system.sx
import react.*
import com.ustadmobile.util.ext.onTextChange
import web.cssom.Cursor
import js.objects.jso
import com.ustadmobile.mui.common.inputCursor
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.view.components.UstadImageSelectButton
import com.ustadmobile.wrappers.quill.ReactQuill

const val COURSE_BLOCK_DRAG_CLASS = "dragging_course_block"

external interface ClazzEditScreenProps : Props {

    var uiState: ClazzEditUiState

    var onClazzChanged: (ClazzWithHolidayCalendarAndAndTerminology?) -> Unit

    var onCourseBlockMoved: (from: Int, to: Int) -> Unit

    var onClickTimezone: () -> Unit

    var onClickAddCourseBlock: () -> Unit

    var onClickAddSchedule: () -> Unit

    var onClickEditSchedule: (Schedule) -> Unit

    var onClickDeleteSchedule: (Schedule) -> Unit

    var onClickHolidayCalendar: () -> Unit

    var onCheckedAttendanceChanged: (Boolean) -> Unit

    var onClickTerminology: () -> Unit

    var onClickHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickEditCourseBlock: (CourseBlockAndEditEntities) -> Unit

}

val ClazzEditScreenComponent2 = FC<ClazzEditScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)

            UstadImageSelectButton {
                imageUri = props.uiState.entity?.coursePicture?.coursePictureUri
                onImageUriChanged = { imageUri ->
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            coursePicture = props.uiState.entity?.coursePicture?.copy(
                                coursePictureUri = imageUri
                            )
                        }
                    )
                }
            }

            UstadEditHeader {
                + strings[MR.strings.basic_details]
            }

            TextField {
                value = props.uiState.entity?.clazzName ?: ""
                label = ReactNode(strings[MR.strings.name_key] + "*")
                id = "clazz_name"
                disabled = !props.uiState.fieldsEnabled
                error = props.uiState.clazzNameError != null
                helperText = ReactNode(props.uiState.clazzNameError ?: strings[MR.strings.required])
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
                readOnly = !props.uiState.fieldsEnabled
                placeholder = strings[MR.strings.description]
                onChange = {
                    props.uiState.entity?.also { entity ->
                        props.onClazzChanged(
                            entity.shallowCopy {
                                clazzDesc = it
                            }
                        )
                    }
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
                    label = ReactNode(strings[MR.strings.start_date] + "*")
                    error = props.uiState.clazzStartDateError != null
                    helperText = ReactNode(props.uiState.clazzStartDateError?: strings[MR.strings.required])
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
                    label = ReactNode(strings[MR.strings.end_date])
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
                        disabled = !props.uiState.fieldsEnabled

                        ListItemIcon {
                            AddIcon()
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
                            block = courseBlockItem
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
                        disabled = !props.uiState.fieldsEnabled
                        onClick = {
                            props.onClickAddSchedule()
                        }

                        ListItemIcon {
                            AddIcon()
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

            UstadTextField {
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

            UstadTextField {
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

