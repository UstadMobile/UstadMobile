package com.ustadmobile.core.matomo

interface AnalyticsTracker {
    fun trackScreen(path: String, title: String)
    fun trackEvent(category: String, action: String, name: String? = null, value: Float? = null)
}