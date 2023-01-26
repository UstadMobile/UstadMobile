package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.viewmodel.ClazzEnrolmentListUiState
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.px
import mui.icons.material.Edit
import mui.icons.material.Person
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface ClazzEnrolmentListProps: Props{
    var uiState: ClazzEnrolmentListUiState
    var onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit
    var onViewProfileClick: () -> Unit
}

val ClazzEnrolmentListComponent2 = FC<ClazzEnrolmentListProps> {    props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadQuickActionButton{
                icon = Person.create()
                text = strings[MessageID.view_profile]
                onClick = { props.onViewProfileClick }
            }

            Divider()

            Typography{
                + strings[MessageID.person_enrolment_in_class].replace("%1\$s",
                props.uiState.personName ?: "").replace("%2\$s", props.uiState.courseName ?: "")
                variant = TypographyVariant.body1
            }

            props.uiState.enrolmentList.forEach {   enrolment ->

                var joinedLeftDate = useFormattedDateRange(enrolment.clazzEnrolmentDateJoined, enrolment.clazzEnrolmentDateLeft, "UTC")
                var itemPrimaryText = "${strings[ClazzEnrolmentListConstants.ROLE_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentRole] ?: 0]} - ${strings[ClazzEnrolmentListConstants.OUTCOME_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentOutcome] ?: 0]}"

                if (enrolment.leavingReason != null){
                    itemPrimaryText = "$itemPrimaryText (${enrolment.leavingReason?.leavingReasonTitle})"
                }

                ListItem{

                    ListItemSecondaryAction{
                        IconButton{
                            onClick = {
                                props.onEditItemClick(enrolment)
                            }
                            Edit()
                        }
                    }

                    ListItemText{
                        primary = ReactNode(itemPrimaryText)
                        secondary = ReactNode(joinedLeftDate)
                    }
                }
            }
        }
    }
}

val ClazzEnrolmentListPreview = FC<Props> {
    ClazzEnrolmentListComponent2{
        uiState = ClazzEnrolmentListUiState(
            personName = "Ahmad",
            courseName = "Mathematics",
            enrolmentList = listOf(
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349880298
                    clazzEnrolmentDateLeft = 509823093
                    clazzEnrolmentUid = 7
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 201
                },
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349887338
                    clazzEnrolmentDateLeft = 409937093
                    clazzEnrolmentUid = 8
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 203
                    leavingReason = LeavingReason().apply {
                        leavingReasonTitle = "transportation problem"
                    }
                }
            )
        )
    }
}