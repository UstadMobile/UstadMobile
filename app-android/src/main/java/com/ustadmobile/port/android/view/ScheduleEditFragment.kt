package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.viewmodel.ScheduleEditUiState
import com.ustadmobile.core.viewmodel.ScheduleEditViewModel
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTimeEditTextField

class ScheduleEditFragment: UstadBaseMvvmFragment() {

    private val viewModel: ScheduleEditViewModel by ustadViewModels(::ScheduleEditViewModel)

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
                    ScheduleEditScreen(viewModel)
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
            label = stringResource(R.string.day),
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

            UstadTimeEditTextField(
                modifier = Modifier.weight(0.5F),
                value = (uiState.entity?.sceduleStartTime ?: 0).toInt(),
                label = stringResource(id = R.string.from),
                error = uiState.fromTimeError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onScheduleChanged(uiState.entity?.shallowCopy{
                        sceduleStartTime = it.toLong()
                    })
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            UstadTimeEditTextField(
                modifier = Modifier.weight(0.5F),
                value = (uiState.entity?.scheduleEndTime ?: 0).toInt(),
                label = stringResource(id = R.string.to),
                error = uiState.toTimeError,
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
