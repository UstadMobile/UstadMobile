package com.ustadmobile.view.clazzenrolment.edit

import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
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
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSelectField
import csstype.px
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ClazzEnrolmentEditScreenProps : Props {

    var uiState: ClazzEnrolmentEditUiState

    var onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit

}

val ClazzEnrolmentEditScreenComponent = FC<ClazzEnrolmentEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    val terminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(20.px)

            UstadSelectField<Int> {
                id = "enrolment_role"
                value = props.uiState.clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT
                label = strings[MessageID.role]
                options = props.uiState.roleOptions
                error = props.uiState.roleSelectedError
                enabled = props.uiState.fieldsEnabled
                itemValue = { "$it" }
                itemLabel = {
                    val messageId = when(it) {
                        ClazzEnrolment.ROLE_TEACHER -> MessageID.teacher
                        else -> MessageID.student
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
                label = ReactNode(strings[MessageID.start_date])
                disabled = !props.uiState.fieldsEnabled
                error = props.uiState.startDateError != null
                helperText = props.uiState.startDateError?.let { ReactNode(it) }
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
                label = ReactNode(strings[MessageID.end_date])
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


            UstadMessageIdSelectField {
                id = "outcome"
                value = props.uiState.clazzEnrolment?.clazzEnrolmentOutcome
                    ?: ClazzEnrolment.OUTCOME_IN_PROGRESS
                label = strings[MessageID.outcome]
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
