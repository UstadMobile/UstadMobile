package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.EnrolmentPolicyConstants
import com.ustadmobile.core.viewmodel.ClazzEditUiState
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.alpha
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadSwitchField
import csstype.opacity
import kotlinx.css.blackAlpha
import kotlinx.css.whiteAlpha
import kotlinx.js.jso
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.icons.material.MoreVert
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.material.Stack
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.events.MouseEvent
import react.dom.events.MouseEventHandler
import react.dom.html.ReactHTML

external interface ClazzEditScreenProps : Props {

    var uiState: ClazzEditUiState

    var onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit

    var onClickSchool: () -> Unit

    var onClickTimezone: () -> Unit

    var onClickAddCourseBlock: () -> Unit

    var onClickAddSchedule: () -> Unit

    var onClickEditSchedule: (Schedule) -> Unit

    var onClickDeleteSchedule: (Schedule) -> Unit

    var onClickHolidayCalendar: () -> Unit

    var onCheckedAttendance: (Boolean) -> Unit

    var onClickTerminology: () -> Unit

    var onClickHideBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockWithEntity?) -> Unit

    var onClickEditCourse: (CourseBlockWithEntity) -> Unit

}

val ClazzEditScreenComponent2 = FC<ClazzEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

            Typography {
                variant = TypographyVariant.h6

                + strings[MessageID.basic_details]
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzName ?: ""
                label = strings[MessageID.name]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzName = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzDesc ?: ""
                label = strings[MessageID.description].addOptionalSuffix(strings)
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzDesc = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.school?.schoolName ?: ""
                label = strings[MessageID.institution]
                enabled = props.uiState.fieldsEnabled
                onClick = props.onClickSchool
                onChange = {}
            }

            UstadDateEditField {
                timeInMillis = props.uiState.entity?.clazzStartTime ?: 0
                label = strings[MessageID.start_date]
                error = props.uiState.clazzStartDateError
                enabled = props.uiState.fieldsEnabled
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
                label = strings[MessageID.end_date].addOptionalSuffix(strings)
                error = props.uiState.clazzEndDateError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzEndTime = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzTimeZone ?: ""
                label = strings[MessageID.timezone]
                enabled = props.uiState.fieldsEnabled
                onClick = { props.onClickTimezone() }
            }

            Typography {
                + strings[MessageID.course_blocks]
            }

            Button {
                onClick = { props.onClickAddCourseBlock }
                variant = ButtonVariant.text
                startIcon = Add.create()

                + strings[MessageID.add_block].uppercase()
            }

            CourseBlockList {
                uiState = props.uiState
                onClickEditCourse = props.onClickEditCourse
            }

            Typography {
                + strings[MessageID.schedule]
            }

            Button {
                onClick = { props.onClickAddSchedule }
                variant = ButtonVariant.text
                startIcon = Add.create()

                + strings[MessageID.add_a_schedule].uppercase()
            }

            ClazzSchedulesList {
                uiState = props.uiState
                onClickEditSchedule = props.onClickEditSchedule
                onClickDeleteSchedule = props.onClickDeleteSchedule
            }


            UstadTextEditField {
                value = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                label = strings[MessageID.holiday_calendar]
                enabled = props.uiState.fieldsEnabled
                onChange = {}
                onClick = props.onClickHolidayCalendar
            }

            Typography {
                + strings[MessageID.course_setup]
            }

            UstadSwitchField {
                label = strings[MessageID.attendance]
                checked = props.uiState.clazzEditAttendanceChecked
                onChanged = { props.onCheckedAttendance(it) }
                enabled = props.uiState.fieldsEnabled
            }

            UstadMessageIdDropDownField {
                value = props.uiState.entity?.clazzEnrolmentPolicy ?: 0
                options = EnrolmentPolicyConstants.ENROLMENT_POLICY_MESSAGE_IDS
                label = strings[MessageID.enrolment_policy]
                id = (props.uiState.entity?.clazzEnrolmentPolicy ?: 0).toString()
                onChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzEnrolmentPolicy = it?.value ?: 0
                        })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.terminology?.ctTitle ?: ""
                label = strings[MessageID.terminology]
                enabled = props.uiState.fieldsEnabled
                onChange = {}
                onClick = props.onClickTerminology
            }
        }
    }
}

val ClazzSchedulesList = FC<ClazzEditScreenProps> { props ->

    List{
        props.uiState.clazzSchedules.forEach { schedule ->
            ListItem{
                onClick = { props.onClickEditSchedule(schedule) }
                secondaryAction = IconButton.create {
                    onClick = { props.onClickDeleteSchedule(schedule) }
                    Delete {}
                }
                ListItemText{
                    primary = ReactNode("Line one")
                    secondary = ReactNode("Line Two")
                }
            }
        }
    }
}

private val CourseBlockList = FC<ClazzEditScreenProps> { props ->

    List {

        props.uiState.courseBlockList.forEach { courseBlock ->

            ListItem{
                whiteAlpha(0.5)
                onClick = { props.onClickEditCourse(courseBlock) }

                ListItemIcon {
                    + mui.icons.material.Menu.create()
                }

                ListItemText {
                    primary = ReactNode(courseBlock.cbTitle ?: "")
                }

                secondaryAction = PopUpMenu.create {}
            }
        }
    }
//    val courseBlockEditAlpha: Float = if (courseBlock.cbHidden) 0.5f else 1f
//    items (
//        items = uiState.courseBlockList
//    ) {
//        ListItem(
//            modifier = Modifier.alpha(courseBlockEditAlpha),
//        )
//    }
}

private data class Point(
    val x: Double = 10.0,
    val y: Double = 10.0,
)

val PopUpMenu = FC<ClazzEditScreenProps> { props ->
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

    ReactHTML.div {

        IconButton{
            onClick = handleContextMenu
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

            MenuItem {
                onClick = {
                    props.onClickHideBlockPopupMenu
                    point = null
                }
                + "Hide"
            }
            MenuItem {
                onClick = {
                    props.onClickIndentBlockPopupMenu
                    point = null
                }
                + "Indent"
            }
            MenuItem {
                onClick = {
                    props.onClickUnIndentBlockPopupMenu
                    point = null
                }
                + "UnIndent"
            }
            MenuItem {
                onClick = {
                    props.onClickDeleteBlockPopupMenu
                    point = null
                }
                + "Delete"
            }
        }
    }
}

val ClazzEditScreenPreview = FC<Props> {

    val uiStateVar : ClazzEditUiState by useState {
        ClazzEditUiState(
            entity = ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {

            },
            clazzSchedules = listOf(
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                    scheduleFrequency = MessageID.yearly
                    scheduleDay = MessageID.sunday
                }
            ),
            courseBlockList = listOf(
                CourseBlockWithEntity().apply {
                    cbTitle = "First"
                },
                CourseBlockWithEntity().apply {
                    cbTitle = "Second"
                },
                CourseBlockWithEntity().apply {
                    cbTitle = "Third"
                },
            ),
        )
    }

    ClazzEditScreenComponent2 {
        uiState = uiStateVar
    }
}