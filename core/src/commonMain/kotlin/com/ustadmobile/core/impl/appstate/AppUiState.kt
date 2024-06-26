package com.ustadmobile.core.impl.appstate

/**
 * The AppUiState represents the overall app scaffolding (e.g. title bar, search, navigation, etc)
 *
 * It is emitted as a flow by UstadViewModel.
 *
 * @param navigationVisible if true, show main destination navigation (the default). False when the
 *        user is not able to use top destinations (e.g. login screen etc).
 * @param hideBottomNavigation hide mobile mode bottom navigation (e.g. if in edit mode). This is
 *        only for space saving purposes. E.g. navigationVisible will be true on edit screens
 *        because we don't need to block navigation, we just need to save screen space on edit mode.
 *        It has no effect on desktop or web. It also has no effect if navigationVisible is false
 *        because the navigation will be hidden.
 * @param title - the main title for the appbar. If null, then the title will not be changed. This avoids
 * flicker when switching between screens, tabs, etc.
 * @param hideSettingsIcon normally the settings icon is shown when the user is on a top level screen.
 *        This might need to be hidden when a selection is in progress etc.
 * @param leadingActionButton if not null, then this will replace the navigation icon on Desktop/Android,
 *        and will replace the shelf menu if shown on the mobile web.
 */
data class AppUiState(
    val fabState: FabUiState = FabUiState(),
    val loadingState: LoadingUiState = LoadingUiState(),
    val title: String? = null,
    val navigationVisible: Boolean = true,
    val hideBottomNavigation: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val userAccountIconVisible: Boolean = true,
    val searchState: AppBarSearchUiState = AppBarSearchUiState(),
    val actionBarButtonState: ActionBarButtonUiState = ActionBarButtonUiState(),
    val overflowItems: List<OverflowItem> = emptyList(),
    val hideAppBar: Boolean = false,
    val actionButtons: List<AppActionButton> = emptyList(),
    val leadingActionButton: AppActionButton? = null,
    val appBarColors: AppBarColors = AppBarColors.STANDARD,
)
