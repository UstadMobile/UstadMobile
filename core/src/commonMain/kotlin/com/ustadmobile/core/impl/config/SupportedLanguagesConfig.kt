package com.ustadmobile.core.impl.config

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl

/**
 * Represents the languages that will be shown to the user to select from. This is accessed via the
 * DI.
 *
 * @param availableLanguagesConfig comma separated list of languages. Specify as follows:
 *   Android: Set via meta-data supported-languages
 *   Web: languages.json
 */
data class SupportedLanguagesConfig (
    val availableLanguagesConfig: String = "en,fa,ps,ar,tg,bn,ne,my",
) {

    val supportedUiLanguages: List<UstadMobileSystemCommon.UiLanguage>
        get() = availableLanguagesConfig.split(",").sorted().map {
            UstadMobileSystemCommon.UiLanguage(it, (UstadMobileConstants.LANGUAGE_NAMES[it] ?: it))
        }


    fun supportedUiLanguagesAndSysDefault(
        useDeviceLangDisplay: String,
    ) : List<UstadMobileSystemCommon.UiLanguage>{
        return listOf(
            UstadMobileSystemCommon.UiLanguage(UstadMobileSystemCommon.LOCALE_USE_SYSTEM,
                useDeviceLangDisplay)
        ) + supportedUiLanguages
    }

    fun supportedUiLanguagesAndSysDefault(
        systemImpl: UstadMobileSystemImpl
    ) : List<UstadMobileSystemCommon.UiLanguage>{
        return supportedUiLanguagesAndSysDefault(systemImpl.getString(MessageID.use_device_language))
    }

}
