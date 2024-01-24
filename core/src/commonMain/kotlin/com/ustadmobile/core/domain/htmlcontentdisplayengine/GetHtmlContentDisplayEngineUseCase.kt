package com.ustadmobile.core.domain.htmlcontentdisplayengine

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.ustadmobile.core.domain.htmlcontentdisplayengine.HtmlContentDisplayEngineOption.Companion.SETTING_KEY_HTML_CONTENT_DISPLAY_ENGINE

class GetHtmlContentDisplayEngineUseCase(
    private val settings: Settings,
    private val getOptionsUseCase: GetHtmlContentDisplayEngineOptionsUseCase,
) {

    operator fun invoke(): HtmlContentDisplayEngineOption {
        val settingInt = settings[SETTING_KEY_HTML_CONTENT_DISPLAY_ENGINE, 0]
        return getOptionsUseCase().firstOrNull {
            it.code == settingInt
        } ?: getOptionsUseCase().first()
    }

}