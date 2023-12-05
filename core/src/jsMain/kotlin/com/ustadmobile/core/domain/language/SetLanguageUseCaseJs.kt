package com.ustadmobile.core.domain.language

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import web.location.location

class SetLanguageUseCaseJs(
    private val systemImpl: UstadMobileSystemImpl,
): SetLanguageUseCase {
    override fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
    ) : SetLanguageUseCase.SetLangResult {
        val currentDisplayLang = systemImpl.getDisplayedLocale()
        systemImpl.setLocale(uiLang.langCode)
        val newDisplayLang = systemImpl.getDisplayedLocale()
        if(currentDisplayLang != newDisplayLang) {
            location.reload()
        }

        return SetLanguageUseCase.SetLangResult(false)
    }
}