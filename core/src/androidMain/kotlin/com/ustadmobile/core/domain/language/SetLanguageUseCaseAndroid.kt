package com.ustadmobile.core.domain.language

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadNavController

/**
 * When the locale is changed, the onSetAppState used by the composable to allow the viewmodel to
 * set the title, fab state etc completely stops working for no apparent reason (there is no such
 * issue on the desktop version).
 *
 * The only way to avoid the problem is to completely restart the process. UstadApp monitors for
 * the locale change (because onLocaleChange DOESNT WORK). The event is collected via a channel by
 * AppActivity. Yuck. Thanks, Google.
 */
class SetLanguageUseCaseAndroid(
    private val languagesConfig: SupportedLanguagesConfig,
): SetLanguageUseCase {

    override fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
        navArgs: Map<String, String>,
    ): SetLanguageUseCase.SetLangResult {
        //languagesConfig uses a delegate on Android that will use Android's per-app locale setting
        languagesConfig.localeSetting = uiLang.langCode

        return SetLanguageUseCase.SetLangResult(
            waitForRestart = true
        )
    }
}