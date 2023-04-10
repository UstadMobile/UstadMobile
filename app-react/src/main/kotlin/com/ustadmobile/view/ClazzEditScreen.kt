package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.impl.locale.entityconstants.EnrolmentPolicyConstants
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants.SCHEDULE_FREQUENCY_MESSAGE_ID_MAP
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_MONDAY
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.viewmodel.ClazzEditUiState
import com.ustadmobile.core.viewmodel.ClazzEditViewModel
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadEditHeader
import com.ustadmobile.view.components.UstadSwitchField
import com.ustadmobile.wrappers.reacteasysort.LockAxis
import com.ustadmobile.wrappers.reacteasysort.SortableItem
import com.ustadmobile.wrappers.reacteasysort.SortableList
import csstype.number
import csstype.pct
import csstype.px
import web.html.HTMLDivElement
import js.core.jso
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.Menu
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.aria.ariaLabel
import react.dom.events.MouseEvent
import react.dom.events.MouseEventHandler
import react.dom.html.ReactHTML.div
import com.ustadmobile.util.ext.onTextChange

private val COURSE_BLOCK_DRAG_CLASS = "dragging_course_block"

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

    var onClickHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickEditCourseBlock: (CourseBlockWithEntity) -> Unit

}

val ClazzEditScreenComponent2 = FC<ClazzEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

            UstadEditHeader {
                + strings[MessageID.basic_details]
            }

            TextField {
                value = props.uiState.entity?.clazzName ?: ""
                label = ReactNode(strings[MessageID.name])
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

            TextField {
                value = props.uiState.entity?.clazzDesc ?: ""
                label = ReactNode(strings[MessageID.description].addOptionalSuffix(strings))
                disabled = !props.uiState.fieldsEnabled
                id = "clazz_desc"
                onTextChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzDesc = it
                        }
                    )
                }
            }

            TextField {
                value = props.uiState.entity?.school?.schoolName ?: ""
                label = ReactNode(strings[MessageID.institution])
                disabled = !props.uiState.fieldsEnabled
                onClick = {
                    props.onClickSchool()
                }
                id = "clazz_schoolname"
            }


            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(3)

                sx {
                    width = 100.pct
                }

                UstadDateEditField {
                    timeInMillis = props.uiState.entity?.clazzStartTime ?: 0
                    timeZoneId = props.uiState.timeZone
                    label = strings[MessageID.start_date]
                    error = props.uiState.clazzStartDateError
                    enabled = props.uiState.fieldsEnabled
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

                UstadDateEditField {
                    timeInMillis = props.uiState.entity?.clazzEndTime ?: 0
                    timeZoneId = props.uiState.timeZone
                    label = strings[MessageID.end_date].addOptionalSuffix(strings)
                    error = props.uiState.clazzEndDateError
                    enabled = props.uiState.fieldsEnabled
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


            TextField {
                value = props.uiState.entity?.clazzTimeZone ?: ""
                id = "clazz_timezone"
                label = ReactNode(strings[MessageID.timezone])
                disabled = !props.uiState.fieldsEnabled
                onClick = { props.onClickTimezone() }
            }

            UstadEditHeader {
                + strings[MessageID.course_blocks]
            }

            List {
                ListItem {
                    ListItemButton {
                        onClick =  { props.onClickAddCourseBlock() }
                        id = "add_course_block"
                        ListItemText {
                            + Add.create()
                        }
                        ListItemText {
                            primary = ReactNode(strings[MessageID.add_block])
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
                + strings[MessageID.schedule]
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
                            primary = ReactNode(strings[MessageID.add_a_schedule])
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


            TextField {
                value = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                id = "clazz_calender_name"
                label = ReactNode(strings[MessageID.holiday_calendar])
                disabled = !props.uiState.fieldsEnabled
                onClick = {
                    props.onClickHolidayCalendar()
                }
            }

            UstadEditHeader {
                + strings[MessageID.course_setup]
            }

            UstadSwitchField {
                label = strings[MessageID.attendance]
                checked = props.uiState.clazzEditAttendanceChecked
                id = "clazz_attendance_switch"
                onChanged = props.onCheckedAttendanceChanged
                enabled = props.uiState.fieldsEnabled
            }

            UstadMessageIdSelectField {
                value = props.uiState.entity?.clazzEnrolmentPolicy
                    ?: EnrolmentPolicyConstants.ENROLMENT_POLICY_MESSAGE_IDS.first().value
                options = EnrolmentPolicyConstants.ENROLMENT_POLICY_MESSAGE_IDS
                label = strings[MessageID.enrolment_policy]
                id = "clazz_enrolment_policy"
                onChange = { option ->
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzEnrolmentPolicy = option.value
                        }
                    )
                }
            }

            TextField {
                value = props.uiState.entity?.terminology?.ctTitle ?: ""
                label = ReactNode(strings[MessageID.terminology])
                disabled = !props.uiState.fieldsEnabled
                id = "clazz_terminology"
                onClick = {
                    props.onClickTerminology()
                }
            }
        }
    }
}

external interface ScheduleListItemProps : Props {
    
    var schedule: Schedule
    
    var onClickEditSchedule: (Schedule) -> Unit
    
    var onClickDeleteSchedule: (Schedule) -> Unit
    
}

private val ScheduleListItem = FC<ScheduleListItemProps> { props ->

    val strings = useStringsXml()

    val fromTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.sceduleStartTime.toInt(),
    )

    val toTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.scheduleEndTime.toInt(),
    )

    val text = "${strings[SCHEDULE_FREQUENCY_MESSAGE_ID_MAP[props.schedule.scheduleFrequency] ?: 0]} " +
        " ${strings[ScheduleConstants.DAY_MESSAGE_ID_MAP[props.schedule.scheduleDay] ?: 0]  } " +
        " $fromTimeFormatted - $toTimeFormatted "

    ListItem{
        secondaryAction = IconButton.create {
            onClick = { props.onClickDeleteSchedule(props.schedule) }
            ariaLabel = strings[MessageID.delete]
            Delete { }
        }

        ListItemButton {
            onClick = { props.onClickEditSchedule(props.schedule) }
            ListItemIcon {
                UstadBlankIcon { }
            }

            ListItemText{
                primary = ReactNode(text)
            }
        }
    }
}

external interface CourseBlockListItemProps : Props{

    var courseBlock: CourseBlockWithEntity

    var uiState: ClazzEditUiState.CourseBlockUiState

    var onClickEditCourseBlock: (CourseBlockWithEntity) -> Unit

    var fieldsEnabled: Boolean

    var onClickHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

}

private val CourseBlockListItem = FC<CourseBlockListItemProps> { props ->
    SortableItem {
        div {
            val divRef : MutableRefObject<HTMLDivElement> = useRef(null)

            ListItem{
                val courseBlockEditAlpha: Double = if (props.courseBlock.cbHidden) 0.5 else 1.0
                val startPadding = (props.courseBlock.cbIndentLevel * 24).px

                val image = if(props.courseBlock.cbType == CourseBlock.BLOCK_CONTENT_TYPE)
                    props.courseBlock.entry?.contentTypeFlag
                else
                    props.courseBlock.cbType

                ListItemButton {
                    sx {
                        opacity = number(courseBlockEditAlpha)
                    }

                    onClick = {
                        //Avoid triggering the onClick listener if the dragging is in process
                        //This might not be needed
                        if(divRef.current?.classList?.contains(COURSE_BLOCK_DRAG_CLASS) != true) {
                            props.onClickEditCourseBlock(props.courseBlock)
                        }
                    }

                    ListItemIcon {
                        sx {
                            paddingLeft = startPadding
                        }
                        + (CONTENT_ENTRY_TYPE_ICON_MAP[image]?.create() ?: TextSnippet.create())
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
                    }
                }

                secondaryAction = PopUpMenu.create {
                    fieldsEnabled = props.fieldsEnabled
                    onClickHideBlockPopupMenu = props.onClickHideBlockPopupMenu
                    onClickIndentBlockPopupMenu = props.onClickIndentBlockPopupMenu
                    onClickUnIndentBlockPopupMenu = props.onClickUnIndentBlockPopupMenu
                    onClickDeleteBlockPopupMenu = props.onClickDeleteBlockPopupMenu
                    uiState = props.uiState
                }
            }
        }
    }
}

external interface PopUpMenuProps : Props {

    var fieldsEnabled: Boolean

    var onClickHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var uiState: ClazzEditUiState.CourseBlockUiState

}

private data class Point(
    val x: Double = 10.0,
    val y: Double = 10.0,
)

val PopUpMenu = FC<PopUpMenuProps> { props ->

    val strings = useStringsXml()

    var point by useState<Point>()

    val handleContextMenu = { event: MouseEvent<*, *> ->
        event.preventDefault()
        point = if (point == null) {
            Point(
                x = event.clientX - 2,
                y = event.clientY - 4,
            )
        } else {
            null
        }
    }

    val handleClose: MouseEventHandler<*> = {
        point = null
    }

    div {

        IconButton{
            disabled = !(props.fieldsEnabled)
            onClick = handleContextMenu
            ariaLabel = strings[MessageID.more_options]

            + MoreVert.create()
        }

        Menu {
            open = point != null
            onClose = handleClose

            anchorReference = PopoverReference.anchorPosition
            anchorPosition = if (point != null) {
                jso {
                    top = point!!.y
                    left = point!!.x
                }
            } else {
                undefined
            }

            if(props.uiState.showHide) {
                MenuItem {
                    onClick = {
                        props.onClickHideBlockPopupMenu(props.uiState.courseBlock)
                        point = null
                    }
                    + strings[MessageID.hide]
                }
            }

            if(props.uiState.showUnhide) {
                MenuItem {
                    onClick = {
                        props.onClickUnHideBlockPopupMenu(props.uiState.courseBlock)
                        point = null
                    }
                    + strings[MessageID.unhide]
                }
            }

            if(props.uiState.showIndent) {
                MenuItem {
                    onClick = {
                        props.onClickIndentBlockPopupMenu(props.uiState.courseBlock)
                        point = null
                    }
                    + strings[MessageID.indent]
                }
            }

            if (props.uiState.showUnindent) {
                MenuItem {
                    onClick = {
                        props.onClickUnIndentBlockPopupMenu(props.uiState.courseBlock)
                        point = null
                    }
                    + strings[MessageID.unindent]
                }
            }

            MenuItem {
                onClick = {
                    props.onClickDeleteBlockPopupMenu(props.uiState.courseBlock)
                    point = null
                }
                + strings[MessageID.delete]
            }
        }
    }
}

external interface AddCourseDialogProps: Props {
    var open: Boolean

    var onClose: ((event: dynamic, reason: String) -> Unit)?

    var onClickAddBlock: (Int) -> Unit
}

private val AddCourseBlockDialog = FC<AddCourseDialogProps> { props ->
    val strings = useStringsXml()

    Dialog {
        open = props.open

        onClose = props.onClose

        List {
            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_MODULE_TYPE)
                    }
                    ListItemIcon {
                        Folder()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.module])
                        secondary = ReactNode(strings[MessageID.course_module])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_TEXT_TYPE)
                    }
                    ListItemIcon {
                        Article()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.text])
                        secondary = ReactNode(strings[MessageID.formatted_text_to_show_to_course_participants])
                    }
                }
            }



            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_CONTENT_TYPE)
                    }
                    ListItemIcon {
                        Collections()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.content])
                        secondary = ReactNode(strings[MessageID.add_course_block_content_desc])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_ASSIGNMENT_TYPE)
                    }
                    ListItemIcon {
                        Assignment()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.assignments])
                        secondary = ReactNode(strings[MessageID.add_assignment_block_content_desc])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_DISCUSSION_TYPE)
                    }
                    ListItemIcon {
                        Forum()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.discussion_board])
                        secondary = ReactNode(strings[MessageID.add_discussion_board_desc])
                    }
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
        onCourseBlockMoved = { fromIndex, toIndex ->

        }
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