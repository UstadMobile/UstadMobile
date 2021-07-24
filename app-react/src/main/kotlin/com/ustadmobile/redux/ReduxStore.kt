package com.ustadmobile.redux

import com.ustadmobile.util.BrowserTabTracker
import redux.RAction

data class ReduxStore(var appState: ReduxAppState = BrowserTabTracker.appState): RAction