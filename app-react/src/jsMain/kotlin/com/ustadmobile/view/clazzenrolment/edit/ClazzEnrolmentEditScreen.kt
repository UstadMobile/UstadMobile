package com.ustadmobile.view.clazzenrolment.edit

import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.entityconstants.OutcomeConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSelectField
import web.cssom.px
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface ClazzEnrolmentEditScreenProps : Props {

    var uiState: ClazzEnrolmentEditUiState

    var onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit

}

val ClazzEnrolmentEditScreenComponent = FC<ClazzEnrolmentEditScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    val terminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(20.px)

            UstadSelectField<Int> {
                id = "enrolment_role"
                value = props.uiState.clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT
                label = strings[MR.strings.role] + "*"
                options = props.uiState.roleOptions
                error = props.uiState.roleSelectedError != null
                helperText = ReactNode(props.uiState.roleSelectedError ?: strings[MR.strings.required])
                enabled = props.uiState.fieldsEnabled
                itemValue = { "$it" }
                itemLabel = {
                    val messageId = when(it) {
                        ClazzEnrolment.ROLE_TEACHER -> MR.strings.teacher
                        else -> MR.strings.student
                    }

                    ReactNode(courseTerminologyResource(terminologyEntries, strings, messageId))
                }
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentRole = it
                        }
                    )
                }
            }

            UstadDateField {
                id = "date_joined"
                timeInMillis = props.uiState.clazzEnrolment?.clazzEnrolmentDateJoined ?: 0
                label = ReactNode(strings[MR.strings.start_date] + "*")
                disabled = !props.uiState.fieldsEnabled
                error = props.uiState.startDateError != null
                helperText = ReactNode(props.uiState.startDateError ?: strings[MR.strings.required])
                timeZoneId = props.uiState.clazzEnrolment?.timeZone ?: "UTC"
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentDateJoined = it
                    })
                }
            }

            UstadDateField {
                id = "date_left"
                timeInMillis = props.uiState.clazzEnrolment?.clazzEnrolmentDateLeft ?: 0
                label = ReactNode(strings[MR.strings.end_date])
                disabled = !props.uiState.fieldsEnabled
                error = props.uiState.endDateError != null
                helperText = props.uiState.endDateError?.let { ReactNode(it) }
                timeZoneId = props.uiState.clazzEnrolment?.timeZone ?: "UTC"
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentDateLeft = it
                    })
                }
                unsetDefault = UNSET_DISTANT_FUTURE
            }


            if(props.uiState.outcomeVisible) {
                UstadMessageIdSelectField {
                    id = "outcome"
                    value = props.uiState.clazzEnrolment?.clazzEnrolmentOutcome
                        ?: ClazzEnrolment.OUTCOME_IN_PROGRESS
                    label = strings[MR.strings.outcome]
                    options = OutcomeConstants.OUTCOME_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onClazzEnrolmentChanged(
                            props.uiState.clazzEnrolment?.shallowCopy {
                                clazzEnrolmentOutcome = it.value
                            })
                    }
                }
            }

        }
    }
}

val ClazzEnrolmentEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzEnrolmentEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzEnrolmentEditUiState())

    ClazzEnrolmentEditScreenComponent {
        uiState = uiStateVal
        onClazzEnrolmentChanged = viewModel::onEntityChanged
    }
}
