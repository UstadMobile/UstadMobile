package com.ustadmobile.view.clazzenrolment.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListItemUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useFormattedDateRange
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.impl.locale.mapLookup
import com.ustadmobile.mui.components.UstadQuickActionButton
import web.cssom.px
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

    val strings = useStringProvider()

    val terminologyEntriesList = useCourseTerminologyEntries(props.uiState.courseTerminology)

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadQuickActionButton {
                icon = PersonIcon.create()
                id = "profile_button"
                text = strings[MR.strings.view_profile]
                onClick = { props.onViewProfileClick() }
            }

            Divider()

            Typography{
                + strings[MR.strings.person_enrolment_in_class].replace("%1\$s",
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
    val strings = useStringProvider()
    val enrolment = props.uiState.enrolment

    val joinedLeftDate = useFormattedDateRange(enrolment.clazzEnrolmentDateJoined,
        enrolment.clazzEnrolmentDateLeft, props.uiState.timeZone)

    val itemPrimaryText = buildString {
        append(courseTerminologyResource(props.terminologyEntries, strings,
            ClazzEnrolmentListConstants.ROLE_TO_STRING_RESOURCE_MAP[enrolment.clazzEnrolmentRole]))
        append(" - ")
        append(strings.mapLookup(enrolment.clazzEnrolmentOutcome,
            ClazzEnrolmentListConstants.OUTCOME_TO_STRING_RESOURCE_MAP))

        if (enrolment.leavingReason != null){
            append("(${enrolment.leavingReason?.leavingReasonTitle})")
        }
    }

    ListItem {
        if(props.uiState.canEdit) {
            ListItemSecondaryAction {
                IconButton {
                    ariaLabel = strings[MR.strings.edit]
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

val ClazzEnrolmentListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzEnrolmentListViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(ClazzEnrolmentListUiState())
    ClazzEnrolmentListComponent2 {
        uiState = uiStateVal
        onEditItemClick = viewModel::onClickEditEnrolment
        onViewProfileClick = viewModel::onClickViewProfile
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
