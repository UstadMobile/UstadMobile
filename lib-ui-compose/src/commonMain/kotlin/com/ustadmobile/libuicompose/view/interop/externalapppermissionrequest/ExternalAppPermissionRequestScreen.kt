package com.ustadmobile.libuicompose.view.interop.externalapppermissionrequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.interop.externalapppermissionrequest.ExternalAppPermissionRequestUiState
import com.ustadmobile.core.viewmodel.interop.externalapppermissionrequest.ExternalAppPermissionRequestViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.view.interop.InteropIconComponent
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ExternalAppPermissionRequestScreen(
    viewModel: ExternalAppPermissionRequestViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        ExternalAppPermissionRequestUiState()
    )

    ExternalAppPermissionRequestScreen(
        uiState = uiState,
        onClickAccept = viewModel::onClickAccept,
        onClickCancel = viewModel::onClickDecline,
    )
}
@Composable
fun ExternalAppPermissionRequestScreen(
    uiState: ExternalAppPermissionRequestUiState,
    onClickAccept: () -> Unit = { },
    onClickCancel: () -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top)
    ) {
        Spacer(Modifier.height(16.dp))

        uiState.icon?.also { icon ->
            InteropIconComponent(icon)
        }

        Text(uiState.appName)

        Text(
            text = stringResource(MR.strings.this_app_will_receive),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
        )

        Button(
            onClick = onClickAccept,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
        ) {
            Text(stringResource(MR.strings.accept))
        }

        OutlinedButton(
            onClick = onClickCancel,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
        ) {
            Text(stringResource(MR.strings.cancel))
        }
    }

}
