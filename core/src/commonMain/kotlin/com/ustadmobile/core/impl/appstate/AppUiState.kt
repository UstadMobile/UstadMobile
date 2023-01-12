package com.ustadmobile.core.impl.appstate

/**
 * The AppUiState represents the overall app scaffolding (e.g. title bar, search, navigation, etc)
 *
 * It is emitted as a flow by UstadViewModel.
 */
data class AppUiState(
    val fabState: FabUiState = FabUiState(),
    val loadingState: LoadingUiState = LoadingUiState(),
    val title: String? = null,
    val navigationVisible: Boolean = true,
) {
}