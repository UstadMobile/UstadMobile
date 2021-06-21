package com.ustadmobile.redux

data class ReduxAppState(var appTheme: ReduxThemeState? = ReduxThemeState(),
                         var appDi: ReduxDiState = ReduxDiState(),
                         var appFab: ReduxFabState = ReduxFabState()
)