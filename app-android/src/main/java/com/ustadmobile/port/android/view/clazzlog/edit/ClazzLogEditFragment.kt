package com.ustadmobile.port.android.view.clazzlog.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditUiState
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadDateTimeField
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.core.R as CR

class ClazzLogEditFragment: UstadBaseMvvmFragment() {


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

            }
        }
    }
}

@Composable
fun ClazzLogEditScreen(viewModel: ClazzLogEditViewModel){
    val uiState by viewModel.uiState.collectAsState(ClazzLogEditUiState())
    ClazzLogEditScreen(
        uiState = uiState,
        onChangeClazzLog = viewModel::onEntityChanged,
    )
}

@Composable
private fun ClazzLogEditScreen(
    uiState: ClazzLogEditUiState = ClazzLogEditUiState(),
    onChangeClazzLog: (ClazzLog?) -> Unit = {},
) {
    UstadInputFieldLayout(
        modifier = Modifier.fillMaxWidth().defaultItemPadding(),
        errorText = uiState.dateError
    ) {
        UstadDateTimeField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("log_datetime"),
            value = uiState.clazzLog?.logDate ?: 0L,
            dateLabel = { Text(stringResource(id = CR.string.date)) },
            timeLabel = { Text(stringResource(CR.string.time)) },
            timeZoneId = uiState.timeZone,
            onValueChange = {
                onChangeClazzLog(uiState.clazzLog?.shallowCopy {
                    logDate = it
                })
            },
        )
    }

}

@Composable
@Preview
fun ClazzLogEditScreenPreview() {
    MdcTheme {
        ClazzLogEditScreen()
    }
}
