package com.ustadmobile.core.domain.htmlcontentdisplayengine

import com.russhwolf.settings.Settings
import com.ustadmobile.core.domain.htmlcontentdisplayengine.HtmlContentDisplayEngineOption.Companion.SETTING_KEY_HTML_CONTENT_DISPLAY_ENGINE

class SetHtmlContentDisplayEngineUseCase(
    private val settings: Settings
) {

    operator fun invoke(htmlContentDisplayEngine: HtmlContentDisplayEngineOption) {
        settings.putInt(SETTING_KEY_HTML_CONTENT_DISPLAY_ENGINE, htmlContentDisplayEngine.code)
    }
}