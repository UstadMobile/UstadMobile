package com.ustadmobile.hooks

import com.ustadmobile.mui.components.UstadScreenTabsStateContext
import react.useContext

fun useTabHeight() : Int {
    val context = useContext(UstadScreenTabsStateContext)

    return context?.component1()?.height ?: 0
}

fun useTabAndAppBarHeight(): Int {
    val tabHeight = useTabHeight()
    val muiState = useMuiAppState()
    return tabHeight + muiState.appBarHeight
}

