package com.ustadmobile.hooks

import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.view.PersonDetailScreen
import react.useMemo

fun useUstadScreens(): UstadScreens {
    return useMemo {
        setOf(
            UstadScreen(PersonDetailView.VIEW_NAME, "Person Detail", PersonDetailScreen)
        )
    }
}
