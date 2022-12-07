package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.OutcomeConstants
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.viewmodel.ClazzEnrolmentEditUiState
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface ClazzEnrolmentEditScreenProps : Props {

    var uiState: ClazzEnrolmentEditUiState

    var onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit

    var onClickLeavingReason: () -> Unit

}

val ClazzEnrolmentEditScreenComponent2 = FC<ClazzEnrolmentEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(20.px)

            UstadMessageIdDropDownField {
                value = props.uiState.clazzEnrolment?.clazzEnrolmentRole ?: 0
                label = strings[MessageID.role]
                options = RoleConstants.ROLE_MESSAGE_IDS
                error = props.uiState.roleSelectedError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentRole = it?.value ?: 0
                    })
                }
            }

            UstadDateEditField {
                timeInMillis = props.uiState.clazzEnrolment?.clazzEnrolmentDateJoined ?: 0
                label = strings[MessageID.start_date]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.startDateError
                timeZoneId = props.uiState.clazzEnrolment?.timeZone ?: "UTC"
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentDateJoined = it
                    })
                }
            }

            UstadDateEditField {
                timeInMillis = props.uiState.clazzEnrolment?.clazzEnrolmentDateLeft ?: 0
                label = strings[MessageID.end_date]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.endDateError
                timeZoneId = props.uiState.clazzEnrolment?.timeZone ?: "UTC"
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentDateLeft = it
                    })
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.clazzEnrolment?.clazzEnrolmentOutcome ?: 0
                label = strings[MessageID.outcome]
                options = OutcomeConstants.OUTCOME_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentOutcome = it?.value ?: 0
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.clazzEnrolment?.leavingReason?.leavingReasonTitle ?: ""
                label = strings[MessageID.leaving_reason]
                onChange = {}
                enabled = props.uiState.leavingReasonEnabled
                onClick = props.onClickLeavingReason
            }
        }
    }
}

val ClazzEnrolmentEditScreenPreview = FC<Props> {

    val uiStateVar : ClazzEnrolmentEditUiState by useState {
        ClazzEnrolmentEditUiState(
            clazzEnrolment = ClazzEnrolmentWithLeavingReason().apply {
                clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_GRADUATED
            },
        )
    }

    ClazzEnrolmentEditScreenComponent2 {
        uiState = uiStateVar
    }
}