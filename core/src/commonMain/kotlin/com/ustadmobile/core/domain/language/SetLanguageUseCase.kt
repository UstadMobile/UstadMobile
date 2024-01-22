package com.ustadmobile.core.domain.language

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadNavController

interface SetLanguageUseCase {

    data class SetLangResult(
        val waitForRestart: Boolean
    )

    operator fun invoke(
        uiLang: UstadMobileSystemCommon.UiLanguage,
        currentDestination: String,
        navController: UstadNavController,
        navArgs: Map<String, String> = emptyMap(),
    ): SetLangResult

}
