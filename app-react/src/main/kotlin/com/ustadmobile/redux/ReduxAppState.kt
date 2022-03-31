package com.ustadmobile.redux

import com.ustadmobile.util.BrowserTabTracker

data class ReduxAppState(
    var appTheme: ReduxThemeState? = ReduxThemeState(),
    var di: ReduxDiState = ReduxDiState(),
    var db: ReduxDbState = ReduxDbState(),
    var appToolbar: ReduxToolbarState = ReduxToolbarState(),
    var navStack: ReduxNavStackState = BrowserTabTracker.navStackState,
    var appSnackBar: ReduxSnackBarState = ReduxSnackBarState()
)