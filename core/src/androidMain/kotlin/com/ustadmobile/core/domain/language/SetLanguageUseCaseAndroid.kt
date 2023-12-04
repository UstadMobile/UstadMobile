package com.ustadmobile.core.domain.language

import android.app.Activity
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController

class SetLanguageUseCaseAndroid(
    private val activity: Activity,
    private val systemImpl: UstadMobileSystemImpl,
): SetLanguageUseCase {

    override fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
    ): SetLanguageUseCase.SetLangResult {
        systemImpl.setLocale(uiLang.langCode)

        ProcessPhoenix.triggerRebirth(activity)
        return SetLanguageUseCase.SetLangResult(
            waitForRestart = true
        )
    }
}