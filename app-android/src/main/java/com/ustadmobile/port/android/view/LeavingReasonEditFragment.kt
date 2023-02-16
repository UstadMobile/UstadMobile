package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
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
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.LeavingReasonEditUiState
import com.ustadmobile.core.viewmodel.LeavingReasonEditViewModel
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadTextEditField


interface LeavingReasonEditFragmentEventHandler {

}

class LeavingReasonEditFragment: UstadBaseMvvmFragment(), LeavingReasonEditFragmentEventHandler {

    private val viewModel: LeavingReasonEditViewModel by viewModels {
        UstadViewModelProviderFactory(di, this, arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    LeavingReasonEditScreen(viewModel)
                }
            }
        }
    }


}
@Composable
fun LeavingReasonEditScreen(
    uiState: LeavingReasonEditUiState,
    onLeavingReasonChanged: (LeavingReason?) -> Unit = {},
){
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UstadTextEditField(
            value = uiState.leavingReason?.leavingReasonTitle ?: "",
            label = stringResource(id = R.string.description),
            error = uiState.reasonTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onLeavingReasonChanged(uiState.leavingReason?.shallowCopy{
                    leavingReasonTitle = it
                })
            }
        )
    }
}

@Composable
fun LeavingReasonEditScreen(
    viewModel: LeavingReasonEditViewModel
) {
    val uiState: LeavingReasonEditUiState by viewModel.uiState.collectAsState(
        initial = LeavingReasonEditUiState()
    )

    LeavingReasonEditScreen(
        uiState = uiState,
        onLeavingReasonChanged = viewModel::onEntityChanged,
    )
}

@Composable
@Preview
fun LeavingReasonEditPreview(){
    LeavingReasonEditScreen(
        uiState = LeavingReasonEditUiState(
            leavingReason = LeavingReason().apply {
                leavingReasonTitle = "Leaving because of something..."
            }
        )
    )
}