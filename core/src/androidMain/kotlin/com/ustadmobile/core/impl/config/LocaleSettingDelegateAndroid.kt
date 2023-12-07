package com.ustadmobile.core.impl.config

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ustadmobile.core.impl.UstadMobileSystemCommon

class LocaleSettingDelegateAndroid: SupportedLanguagesConfig.LocaleSettingDelegate {

    override var localeSetting: String?
        get() = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
        set(value) {
            val localeList = if(value == UstadMobileSystemCommon.LOCALE_USE_SYSTEM) {
                LocaleListCompat.getAdjustedDefault()
            }else {
                LocaleListCompat.forLanguageTags(value)
            }

            AppCompatDelegate.setApplicationLocales(localeList)
        }
}