package com.ustadmobile.port.android.view.schedule.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditUiState
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTimeField
import com.ustadmobile.core.R as CR

class ScheduleEditFragment: UstadBaseMvvmFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

                }
            }
        }
    }

}

@Composable
private fun ScheduleEditScreen(
    uiState: ScheduleEditUiState = ScheduleEditUiState(),
    onScheduleChanged: (Schedule?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.scheduleDay ?: 0,
            label = stringResource(CR.string.day),
            options = ScheduleConstants.DAY_MESSAGE_IDS,
            onOptionSelected = {
                onScheduleChanged(uiState.entity?.shallowCopy{
                    scheduleDay = it.value
                })
            },
            enabled = uiState.fieldsEnabled,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            UstadInputFieldLayout(
                modifier = Modifier.weight(0.5F),
                errorText = uiState.fromTimeError
            ) {
                UstadTimeField(
                    modifier = Modifier.fillMaxWidth(),
                    value = (uiState.entity?.sceduleStartTime ?: 0).toInt(),
                    label = {
                        Text(stringResource(id = CR.string.from))
                    },
                    isError = uiState.fromTimeError != null,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onScheduleChanged(uiState.entity?.shallowCopy{
                            sceduleStartTime = it.toLong()
                        })
                    }
                )
            }


            Spacer(modifier = Modifier.width(10.dp))

            UstadInputFieldLayout(
                modifier = Modifier.weight(0.5F),
                errorText = uiState.toTimeError,
            ) {
                UstadTimeField(
                    modifier = Modifier.fillMaxWidth(),
                    value = (uiState.entity?.scheduleEndTime ?: 0).toInt(),
                    label = {
                        Text(stringResource(id = CR.string.to_key))
                    },
                    isError = uiState.toTimeError != null,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onScheduleChanged(uiState.entity?.shallowCopy{
                            scheduleEndTime = it.toLong()
                        })
                    }
                )
            }

        }
    }
}

@Composable
fun ScheduleEditScreen(
    viewModel: ScheduleEditViewModel
){
    val uiState: ScheduleEditUiState by viewModel.uiState.collectAsState(initial = ScheduleEditUiState())

    ScheduleEditScreen(
        uiState = uiState,
        onScheduleChanged = viewModel::onEntityChanged
    )
}

@Composable
@Preview
fun ScheduleEditScreenPreview() {
    MdcTheme {
        ScheduleEditScreen()
    }
}
