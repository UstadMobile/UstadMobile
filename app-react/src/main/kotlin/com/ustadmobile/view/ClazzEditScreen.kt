package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.EnrolmentPolicyConstants
import com.ustadmobile.core.viewmodel.ClazzEditUiState
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.ext.addOptionalSuffix
import csstype.AlignItems
import csstype.px
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*

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

            SwitchRow {
                text = strings[MessageID.attendance]
                checked = props.uiState.clazzEditAttendanceChecked
                onChange = { props.onCheckedAttendance(it) }
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

external interface SwitchRowProps : Props {

    var text: String

    var checked: Boolean

    var onChange: (Boolean) -> Unit

}

private val SwitchRow = FC<SwitchRowProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(20.px)
        sx {
            alignItems = AlignItems.center
        }

        Typography {
            + props.text
        }

        Switch {
            checked= props.checked
            onChange = { _, it ->
                props.onChange(it)
            }
        }
    }
}

val ClazzEditScreenPreview = FC<Props> {

    val uiStateVar : ClazzEditUiState by useState {
        ClazzEditUiState()
    }

    ClazzEditScreenComponent2 {
        uiState = uiStateVar
    }
}