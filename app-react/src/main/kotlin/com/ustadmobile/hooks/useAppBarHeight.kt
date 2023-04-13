package com.ustadmobile.hooks

import com.ustadmobile.MuiAppState
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.mui.components.UstadScreensContext
import react.useRequiredContext

fun useMuiAppState() : MuiAppState {
    val screensContext = useRequiredContext(UstadScreensContext)
    val muiState : MuiAppState by screensContext.muiAppState.collectAsState(
        initialState = screensContext.muiAppState.value
    )

    return muiState
}
