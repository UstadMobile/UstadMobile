package com.ustadmobile.util.matomo

import react.FC
import react.Props
import react.router.useLocation
import react.useEffect

external fun trackPageView(screenName: String)

val MatomoTracker = FC<Props> {
    val location = useLocation()

    useEffect(listOf(location.pathname)) {
        val screenName = location.pathname
        console.log("Tracking screen: $screenName")
        trackPageView(screenName)
    }
}
