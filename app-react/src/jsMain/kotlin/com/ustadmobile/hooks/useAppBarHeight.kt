package com.ustadmobile.hooks

import com.ustadmobile.MuiAppState
import com.ustadmobile.mui.components.UstadScreensContext
import react.useRequiredContext

fun useMuiAppState() : MuiAppState {
    val screensContext = useRequiredContext(UstadScreensContext)

    return screensContext.muiAppState.component1()
}
