package com.ustadmobile.core.domain.language

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import web.location.location

class SetLanguageUseCaseJs(
    private val languagesConfig: SupportedLanguagesConfig,
): SetLanguageUseCase {
    override fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
    ) : SetLanguageUseCase.SetLangResult {
        val currentDisplayLang = languagesConfig.displayedLocale
        languagesConfig.localeSetting = uiLang.langCode
        val newDisplayLang = languagesConfig.displayedLocale

        if(currentDisplayLang != newDisplayLang) {
            location.reload()
        }

        return SetLanguageUseCase.SetLangResult(false)
    }
}