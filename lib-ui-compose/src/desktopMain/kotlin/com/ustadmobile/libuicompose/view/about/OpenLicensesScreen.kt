package com.ustadmobile.libuicompose.view.about

import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.about.OpenLicensesViewModel

@Composable
actual fun OpenLicensesScreen(
    viewModel: OpenLicensesViewModel,
) {
    //Do nothing - on JVM/Desktop showing the licenses is handled by LaunchOpenLicensesUseCaseJvm
    //to show in browser.
}
