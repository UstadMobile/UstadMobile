package com.ustadmobile.libuicompose.view.clazzenrolment.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzEnrolmentEditScreenForViewModel(
    viewModel: ClazzEnrolmentEditViewModel
){
    val uiState by viewModel.uiState.collectAsState(ClazzEnrolmentEditUiState())

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
            .defaultScreenPadding()
    )  {
        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.roleSelectedError,
        ) {
            UstadExposedDropDownMenuField(
                value = uiState.clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT,
                modifier = Modifier.testTag("enrolment_role").fillMaxWidth(),
                label = stringResource(MR.strings.role),
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
            )
        }


        Spacer(modifier = Modifier.width(15.dp))

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.startDateError
        ) {
            UstadDateField(
                modifier = Modifier.testTag("start_date").fillMaxWidth(),
                value = uiState.clazzEnrolment?.clazzEnrolmentDateJoined ?: 0,
                label = { Text(stringResource(MR.strings.start_date)) },
                enabled = uiState.fieldsEnabled,
                isError = uiState.startDateError != null,
                timeZoneId = uiState.clazzEnrolment?.timeZone ?: "UTC",
                onValueChange = {
                    onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy{
                        clazzEnrolmentDateJoined = it
                    })
                }
            )
        }


        Spacer(modifier = Modifier.width(15.dp))

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
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


        Spacer(modifier = Modifier.width(15.dp))

        UstadMessageIdOptionExposedDropDownMenuField(
            modifier = Modifier.testTag("clazzEnrolmentOutcome").fillMaxWidth(),
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

        Spacer(modifier = Modifier.width(15.dp))
    }
}
