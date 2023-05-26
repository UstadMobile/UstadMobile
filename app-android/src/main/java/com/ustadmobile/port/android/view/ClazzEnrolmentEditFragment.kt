package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.locale.entityconstants.OutcomeConstants
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditUiState
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.view.composable.UstadDateField
import com.ustadmobile.port.android.view.composable.UstadExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadInputFieldLayout
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField


class ClazzEnrolmentEditFragment: UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzEnrolmentEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzEnrolmentEditScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ClazzEnrolmentEditScreen(
    uiState: ClazzEnrolmentEditUiState = ClazzEnrolmentEditUiState(),
    onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit = {},
) {

    val terminologyEntries = rememberCourseTerminologyEntries(uiState.courseTerminology)

    Column(
        modifier = Modifier
            .padding(16.dp)
    )  {
        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.roleSelectedError,
        ) {
            UstadExposedDropDownMenuField(
                value = uiState.clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT,
                modifier = Modifier.testTag("enrolment_role").fillMaxWidth(),
                label = stringResource(R.string.role),
                itemText = {
                    val messageId = if(it == ClazzEnrolment.ROLE_STUDENT) {
                        MessageID.student
                    }else {
                        MessageID.teacher
                    }

                    courseTerminologyEntryResource(
                        terminologyEntries = terminologyEntries,
                        messageId = messageId
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
                label = { Text(stringResource(id = R.string.start_date)) },
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
                label = { Text(stringResource(id = R.string.end_date)) },
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
            value = uiState.clazzEnrolment?.clazzEnrolmentOutcome ?: ClazzEnrolment.OUTCOME_IN_PROGRESS,
            label = stringResource(R.string.outcome),
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

@Composable
fun ClazzEnrolmentEditScreen(
    viewModel: ClazzEnrolmentEditViewModel
){
    val uiState by viewModel.uiState.collectAsState(ClazzEnrolmentEditUiState())

    ClazzEnrolmentEditScreen(
        uiState = uiState,
        onClazzEnrolmentChanged = viewModel::onEntityChanged
    )
}

@Composable
@Preview
fun ClazzEnrolmentEditScreenPreview() {
    val uiState = ClazzEnrolmentEditUiState(
        clazzEnrolment = ClazzEnrolmentWithLeavingReason().apply {
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_GRADUATED
        },
    )

    MdcTheme {
        ClazzEnrolmentEditScreen(uiState)
    }
}