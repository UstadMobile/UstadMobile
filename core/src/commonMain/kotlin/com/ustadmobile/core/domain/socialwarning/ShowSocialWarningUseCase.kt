package com.ustadmobile.core.domain.socialwarning

import com.russhwolf.settings.Settings

class ShowSocialWarningUseCase(
    private val settings: Settings
) {
    companion object {
        private const val SOCIAL_WARNING_DISMISSED_PREFIX = "dismissed-social-warning-"
    }

    operator fun invoke(username: String): Boolean {
        val key = SOCIAL_WARNING_DISMISSED_PREFIX + (username ?: "guest")
        return !settings.getBoolean(key, false) // Return true if warning should be shown
    }
}