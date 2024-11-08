package com.ustadmobile.core.domain.socialwarning

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class DismissSocialWarningUseCase(
    private val settings: Settings
) {
    companion object {
        private const val SOCIAL_WARNING_DISMISSED_PREFIX = "dismissed-social-warning-"
    }

    operator fun invoke(username: String) {
        val key = SOCIAL_WARNING_DISMISSED_PREFIX + (username ?: "guest")
        settings[key] = true
    }
}