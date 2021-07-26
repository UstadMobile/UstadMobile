package com.ustadmobile.redux

import com.ustadmobile.util.BrowserTabTracker

data class ReduxAppState(var appTheme: ReduxThemeState? = ReduxThemeState(),
                         var appDi: ReduxDiState = ReduxDiState(),
                         var appToolbar: ReduxToolbarState = ReduxToolbarState(),
                         var navStack: ReduxNavStackState = BrowserTabTracker.navStackState,
                         var appSnackBar: ReduxSnackBarState = ReduxSnackBarState(),
                         var serialization: ReduxSerializationState = ReduxSerializationState()
)