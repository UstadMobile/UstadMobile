package com.ustadmobile.core.matomo

import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class MatomoAnalytics(private val tracker: Tracker) : AnalyticsTracker {
    override fun trackScreen(path: String, title: String) {
        TrackHelper.track().screen(path).title(title).with(tracker)
    }

    override fun trackEvent(category: String, action: String, name: String?, value: Float?) {
        TrackHelper.track().event(category, action).name(name).value(value).with(tracker)
    }
}
