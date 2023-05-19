package com.ustadmobile.view.clazzenrolment.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListItemUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListUiState
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.TerminologyEntry
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.px
import mui.icons.material.Edit as EditIcon
import mui.icons.material.Person as PersonIcon
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel

external interface ClazzEnrolmentListProps: Props{
    var uiState: ClazzEnrolmentListUiState
    var onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit
    var onViewProfileClick: () -> Unit
}

val ClazzEnrolmentListComponent2 = FC<ClazzEnrolmentListProps> { props ->

    val strings = useStringsXml()

    val terminologyEntriesList = useCourseTerminologyEntries(props.uiState.courseTerminology)

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadQuickActionButton {
                icon = PersonIcon.create()
                id = "profile_button"
                text = strings[MessageID.view_profile]
                onClick = { props.onViewProfileClick() }
            }

            Divider()

            Typography{
                + strings[MessageID.person_enrolment_in_class].replace("%1\$s",
                props.uiState.personName ?: "").replace("%2\$s", props.uiState.courseName ?: "")
                variant = TypographyVariant.body1
            }

            props.uiState.enrolmentList.forEach {   enrolmentItem ->
                ClazzEnrolmentListItem {
                    uiState = props.uiState.enrolmentItemUiState(enrolmentItem)
                    onClickEdit = props.onEditItemClick
                    terminologyEntries = terminologyEntriesList
                }
            }
        }
    }
}

private external interface ClazzenrolmentListItemProps: Props {

    var uiState: ClazzEnrolmentListItemUiState

    var onClickEdit: (ClazzEnrolmentWithLeavingReason) -> Unit

    var terminologyEntries: List<TerminologyEntry>

}

private val ClazzEnrolmentListItem = FC<ClazzenrolmentListItemProps> { props ->
    val strings = useStringsXml()
    val enrolment = props.uiState.enrolment

    val joinedLeftDate = useFormattedDateRange(enrolment.clazzEnrolmentDateJoined,
        enrolment.clazzEnrolmentDateLeft, props.uiState.timeZone)

    val itemPrimaryText = buildString {
        append(courseTerminologyResource(props.terminologyEntries, strings,
            ClazzEnrolmentListConstants.ROLE_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentRole] ?: 0))
        append(" - ")
        append(strings[ClazzEnrolmentListConstants.OUTCOME_TO_MESSAGE_ID_MAP[enrolment.clazzEnrolmentOutcome] ?: 0])

        if (enrolment.leavingReason != null){
            append("(${enrolment.leavingReason?.leavingReasonTitle})")
        }
    }

    ListItem {
        if(props.uiState.canEdit) {
            ListItemSecondaryAction {
                IconButton {
                    ariaLabel = strings[MessageID.edit]
                    onClick = {
                        props.onClickEdit(enrolment)
                    }
                    EditIcon()
                }
            }
        }

        ListItemText {
            primary = ReactNode(itemPrimaryText)
            secondary = ReactNode(joinedLeftDate)
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
            ),
            canEditStudentEnrolments = true,
            canEditTeacherEnrolments = true,
        )
    }
}
