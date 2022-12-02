package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.ClazzEditUiState
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadTextEditField
import mui.icons.material.Add
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.create
import react.useState

external interface ClazzEditScreenProps : Props {

    var uiState: ClazzEditUiState

    var onClazzChanged: (ClazzWithHolidayCalendarAndSchoolAndTerminology?) -> Unit

    var onClickSchool: () -> Unit

    var onClickTimezone: () -> Unit

    var onClickAddCourseBlock: () -> Unit

    var onClickEditCourseBlock: () -> Unit

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
                        })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzDesc ?: ""
                label = strings[MessageID.description]
                error = props.uiState.clazzDescError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzChanged(
                        props.uiState.entity?.shallowCopy {
                            clazzDesc = it
                        })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.school?.schoolName ?: ""
                label = strings[MessageID.institution]
                onChange = {
                    props.onClickSchool()
                }
                error = props.uiState.schoo
                enabled = props.uiState.fieldsEnabled
            }

            UstadDateEditField {
                timeInMillis = props.uiState.entity?.clazzStartTime ?: 0
                label = strings[MessageID.start_date]
                onChange = {

                }
                error = props.uiState.clazzStartDateError
                enabled = props.uiState.fieldsEnabled
            }

            UstadDateEditField {
                timeInMillis = props.uiState.entity?.clazzEndTime ?: 0
                label = strings[MessageID.end_date]
                onChange = {

                }
                error = props.uiState.clazzEndDateError
                enabled = props.uiState.fieldsEnabled
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzTimeZone ?: ""
                label = strings[MessageID.timezone]
                onChange = {
                    props.onClickTimezone()
                }
                error = props.uiState.institutionError
                enabled = props.uiState.fieldsEnabled
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
                onClick = { props.onClickAddCourseBlock }
                variant = ButtonVariant.text
                startIcon = Add.create()

                + strings[MessageID.add_a_schedule].uppercase()
            }

            Typography {
                + strings[MessageID.course_setup]
            }

            Switch {
                disabled = !props.uiState.fieldsEnabled
                checked = props.uiState.clazzEditAttendanceChecked

                + strings[MessageID.attendance]
            }

            UstadTextEditField {
                value = props.uiState.entity?.clazzEnrolmentPolicy ?: 0
                label = strings[MessageID.enrolment_policy]
                onChange = {
                    props.onClickTimezone()
                }
                error = props.uiState.institutionError
                enabled = props.uiState.fieldsEnabled
            }

            UstadTextEditField {
                value = props.uiState.entity?.terminology?.ctTitle ?: ""
                label = strings[MessageID.terminology]
                onChange = {
                    props.onClickTerminology()
                }
                error = props.uiState.institutionError
                enabled = props.uiState.fieldsEnabled
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