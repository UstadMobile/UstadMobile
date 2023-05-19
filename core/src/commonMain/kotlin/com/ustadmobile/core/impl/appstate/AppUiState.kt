package com.ustadmobile.core.impl.appstate

/**
 * The AppUiState represents the overall app scaffolding (e.g. title bar, search, navigation, etc)
 *
 * It is emitted as a flow by UstadViewModel.
 *
 * @param hideBottomNavigation hide mobile mode bottom navigation (e.g. if in edit mode). Has no effect on desktop / web.
 */
data class AppUiState(
    val fabState: FabUiState = FabUiState(),
    val loadingState: LoadingUiState = LoadingUiState(),
    val title: String? = null,
    val navigationVisible: Boolean = true,
    val hideBottomNavigation: Boolean = false,
    val userAccountIconVisible: Boolean = true,
    val searchState: AppBarSearchUiState = AppBarSearchUiState(),
    val actionBarButtonState: ActionBarButtonUiState = ActionBarButtonUiState(),
)
