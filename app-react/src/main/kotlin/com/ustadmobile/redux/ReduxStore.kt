package com.ustadmobile.redux

import redux.RAction

data class ReduxStore(var appState: ReduxAppState = ReduxAppState()): RAction