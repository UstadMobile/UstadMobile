package com.ustadmobile.libuicompose.view.clazzenrolment.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.impl.locale.entityconstants.ClazzEnrolmentListConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListItemUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.libuicompose.util.rememberFormattedDateRange
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzEnrolmentListScreen(
    viewModel: ClazzEnrolmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzEnrolmentListUiState())
    ClazzEnrolmentListScreen(
        uiState = uiState,
        onEditItemClick = viewModel::onClickEditEnrolment,
        onViewProfileClick = viewModel::onClickViewProfile,
    )
}

@Composable
fun ClazzEnrolmentListScreen(
    uiState: ClazzEnrolmentListUiState,
    onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit = {},
    onViewProfileClick: () -> Unit = {}
){
    val courseTerminologyEntries = rememberCourseTerminologyEntries(uiState.courseTerminology)

    UstadLazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ){

        item{
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.view_profile),
                imageVector = Icons.Filled.Person,
                onClick = onViewProfileClick
            )
        }

        item {
            Divider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        }

        item{
            Text(
                text = stringResource(MR.strings.person_enrolment_in_class,
                    uiState.personName ?: "", uiState.courseName ?: ""),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }

        items(
            uiState.enrolmentList,
            key = {
                it.clazzEnrolmentUid
            }
        ){  enrolment ->
            ClazzEnrolmentListItem(
                uiState = uiState.enrolmentItemUiState(enrolment),
                onEditItemClick = onEditItemClick,
                terminologyEntries = courseTerminologyEntries,
            )
        }
    }
}

@Composable
private fun ClazzEnrolmentListItem(
    uiState: ClazzEnrolmentListItemUiState,
    onEditItemClick: (ClazzEnrolmentWithLeavingReason) -> Unit,
    terminologyEntries: List<TerminologyEntry>,
) {
    val enrolment = uiState.enrolment

    val joinedLeftDate = rememberFormattedDateRange(
        startTimeInMillis = enrolment.clazzEnrolmentDateJoined,
        endTimeInMillis = enrolment.clazzEnrolmentDateLeft,
        timeZoneId = uiState.timeZone,
    )

    val itemPrimaryText = buildString {
        val roleMessageId = ClazzEnrolmentListConstants
            .ROLE_TO_STRING_RESOURCE_MAP[enrolment.clazzEnrolmentRole]
            ?: MR.strings.student
        val outcomeMessageId = ClazzEnrolmentListConstants
            .OUTCOME_TO_STRING_RESOURCE_MAP[enrolment.clazzEnrolmentOutcome] ?: MR.strings.in_progress

        append(courseTerminologyEntryResource(terminologyEntries, roleMessageId))
        append(" - ")
        append(stringResource(resource = outcomeMessageId))
        if (enrolment.leavingReason != null){
            append(" (")
            append(enrolment.leavingReason?.leavingReasonTitle ?: "")
            append(")")
        }
    }

    ListItem(
        headlineContent = { Text(text = itemPrimaryText) },
        supportingContent = { Text(text = joinedLeftDate)},
        trailingContent = {
            if(uiState.canEdit) {
                IconButton(
                    onClick = {
                        onEditItemClick(enrolment)
                    }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(MR.strings.edit)
                    )
                }
            }
        }
    )
}
