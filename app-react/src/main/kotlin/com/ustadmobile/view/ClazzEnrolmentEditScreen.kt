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
import com.ustadmobile.mui.common.inputCursor
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import csstype.Cursor
import csstype.px
import js.core.jso
import mui.material.Container
import mui.material.TextField
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
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

            UstadMessageIdSelectField {
                id = "clazzEnrolmentRole"
                value = props.uiState.clazzEnrolment?.clazzEnrolmentRole ?: 0
                label = strings[MessageID.role]
                options = RoleConstants.ROLE_MESSAGE_IDS
                error = props.uiState.roleSelectedError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onClazzEnrolmentChanged(
                        props.uiState.clazzEnrolment?.shallowCopy {
                            clazzEnrolmentRole = it.value
                    })
                }
            }

            UstadDateField {
                id = "clazzEnrolmentDateJoined"
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
                id = "clazzEnrolmentDateLeft"
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
            }

            UstadMessageIdSelectField {
                id = "clazzEnrolmentOutcome"
                value = props.uiState.clazzEnrolment?.clazzEnrolmentOutcome ?: 0
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

            TextField {
                sx {
                    inputCursor = Cursor.pointer
                }
                id = "leavingReasonTitle"
                value = props.uiState.clazzEnrolment?.leavingReason?.leavingReasonTitle ?: ""
                label = ReactNode(strings[MessageID.leaving_reason])
                disabled = !props.uiState.leavingReasonEnabled
                onClick = { props.onClickLeavingReason() }
                inputProps = jso {
                    readOnly = true
                }
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