package com.ustadmobile.view

import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.hooks.useUstadScreens
import react.FC
import react.PropsWithChildren
import react.createContext

val UstadScreensContext = createContext<UstadScreens>()

val UstadScreensModule = FC<PropsWithChildren> { props ->
    val screens = useUstadScreens()

    UstadScreensContext(screens) {
        +props.children
    }
}
