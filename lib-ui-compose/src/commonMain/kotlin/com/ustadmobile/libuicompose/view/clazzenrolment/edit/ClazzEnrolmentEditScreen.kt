package com.ustadmobile.libuicompose.view.clazzenrolment.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.entityconstants.OutcomeConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ClazzEnrolmentEditScreen(
    viewModel: ClazzEnrolmentEditViewModel
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzEnrolmentEditUiState(), Dispatchers.Main.immediate)

    ClazzEnrolmentEditScreen(
        uiState = uiState,
        onClazzEnrolmentChanged = viewModel::onEntityChanged
    )
}

@Composable
fun ClazzEnrolmentEditScreen(
    uiState: ClazzEnrolmentEditUiState = ClazzEnrolmentEditUiState(),
    onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit = {},
) {

    val terminologyEntries = rememberCourseTerminologyEntries(uiState.courseTerminology)

    Column(
        modifier = Modifier
    )  {

        UstadExposedDropDownMenuField(
            value = uiState.clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT,
            modifier = Modifier.testTag("enrolment_role").defaultItemPadding().fillMaxWidth(),
            label = stringResource(MR.strings.role) + "*",
            itemText = {
                val messageId = if(it == ClazzEnrolment.ROLE_STUDENT) {
                    MR.strings.student
                }else {
                    MR.strings.teacher
                }

                courseTerminologyEntryResource(
                    terminologyEntries = terminologyEntries,
                    stringResource = messageId
                )
            },
            options = uiState.roleOptions,
            onOptionSelected = {
                onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy {
                    clazzEnrolmentRole = it
                })
            },
            supportingText = {
                Text(uiState.roleSelectedError ?: stringResource(MR.strings.required))
            }
        )

        Spacer(modifier = Modifier.width(15.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            UstadDateField(
                modifier = Modifier.weight(0.5f)
                    .defaultItemPadding(end = 8.dp)
                    .testTag("start_date")
                    .fillMaxWidth(),
                value = uiState.clazzEnrolment?.clazzEnrolmentDateJoined ?: 0,
                label = { Text(stringResource(MR.strings.start_date) + "*") },
                enabled = uiState.fieldsEnabled,
                isError = uiState.startDateError != null,
                timeZoneId = uiState.clazzEnrolment?.timeZone ?: "UTC",
                onValueChange = {
                    onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy{
                        clazzEnrolmentDateJoined = it
                    })
                },
                supportingText = {
                    Text(uiState.startDateError ?: stringResource(MR.strings.required))
                }
            )

            UstadInputFieldLayout(
                modifier = Modifier.weight(0.5f).defaultItemPadding(start = 8.dp),
                errorText = uiState.endDateError,
            ) {
                UstadDateField(
                    modifier = Modifier.testTag("end_date").fillMaxWidth(),
                    value = uiState.clazzEnrolment?.clazzEnrolmentDateLeft ?: 0,
                    label = { Text(stringResource(MR.strings.end_date)) },
                    enabled = uiState.fieldsEnabled,
                    isError = uiState.endDateError != null,
                    timeZoneId = uiState.clazzEnrolment?.timeZone ?: "UTC",
                    onValueChange = {
                        onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy{
                            clazzEnrolmentDateLeft = it
                        })
                    }
                )
            }
        }

        if(uiState.outcomeVisible) {
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier.testTag("clazzEnrolmentOutcome").fillMaxWidth().defaultItemPadding(),
                value = uiState.clazzEnrolment?.clazzEnrolmentOutcome ?: ClazzEnrolment.OUTCOME_IN_PROGRESS,
                label = stringResource(MR.strings.outcome),
                options = OutcomeConstants.OUTCOME_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy{
                        clazzEnrolmentOutcome = it.value
                    })
                },
            )
        }

    }
}
