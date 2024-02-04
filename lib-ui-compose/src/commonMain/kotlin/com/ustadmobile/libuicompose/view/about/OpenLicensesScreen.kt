package com.ustadmobile.libuicompose.view.about

import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.about.OpenLicensesViewModel

@Composable
expect fun OpenLicensesScreen(
    viewModel: OpenLicensesViewModel,
)
