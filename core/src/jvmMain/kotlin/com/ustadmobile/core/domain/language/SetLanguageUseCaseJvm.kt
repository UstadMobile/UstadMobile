package com.ustadmobile.core.domain.language

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.util.systemTimeInMillis
import java.util.Locale

class SetLanguageUseCaseJvm(
    private val supportedLangConfig: SupportedLanguagesConfig,
): SetLanguageUseCase {

    override fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
    ) : SetLanguageUseCase.SetLangResult{
        //Change the supported lang config
        supportedLangConfig.localeSetting = uiLang.langCode

        if(uiLang.langCode == UstadMobileSystemCommon.LOCALE_USE_SYSTEM) {
            Locale.setDefault(REAL_SYSTEM_DEFAULT)
        }else {
            Locale.setDefault(Locale(uiLang.langCode))
        }

        //We need to navigate to force everything to update
        navController.navigate(
            viewName = currentDestination,
            args = mapOf("invalidated" to systemTimeInMillis().toString()),
            UstadMobileSystemCommon.UstadGoOptions(clearStack = true)

        )
        return SetLanguageUseCase.SetLangResult(false)
    }

    companion object {

        //This is exactly designed to remember what the locale was on startup as per the system
        @Suppress("ConstantLocale")
        val REAL_SYSTEM_DEFAULT = Locale.getDefault()

        fun init() {
            println("real default = $REAL_SYSTEM_DEFAULT")
        }

    }
}