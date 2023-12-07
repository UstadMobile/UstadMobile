package com.ustadmobile.core.impl.config

import com.russhwolf.settings.Settings
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlin.concurrent.Volatile

/**
 * Manages language configuration
 *
 * @param availableLanguagesConfig comma separated list of languages supported by the app.
 * Specify as follows:
 *   Android: Set via meta-data supported-languages
 *   Web: ustad-config.json (in resources) in the property com.ustadmobile.uilanguages
 *
 * @param localeSettingDelegate Used to store the user selected language. On JVM and Web, this is a
 *        simple delegate to the settings. On Android this needs to delegate to the platform specific
 *        code, because the user can also change the setting in app settings.
 *
 * @param systemLocales the system locales in order of user preference. On Android this comes from
 *        getConfiguration().getLocales(). On JVM this is a single item list, on the web this comes
 *        from navigator.languages .
 *
 * @param fallbackLocaleCode the fallback locale code that will be used if the user does not
 *        explicitly set a language and none of their preferred longuages are supported
 */
class SupportedLanguagesConfig (
    private val systemLocales: List<String>,
    private val localeSettingDelegate: LocaleSettingDelegate,
    private val availableLanguagesConfig: String = DEFAULT_SUPPORTED_LANGUAGES,
    private val fallbackLocaleCode: String = "en",
) {

    constructor(
        systemLocales: List<String>,
        settings: Settings,
        availableLanguagesConfig: String = DEFAULT_SUPPORTED_LANGUAGES,
        fallbackLocaleCode: String = "en",
    ): this(
        systemLocales = systemLocales,
        localeSettingDelegate = SettingsLocaleSettingDelegate(settings),
        availableLanguagesConfig = availableLanguagesConfig,
        fallbackLocaleCode = fallbackLocaleCode,
    )
    interface LocaleSettingDelegate {

        var localeSetting: String?

    }

    class SettingsLocaleSettingDelegate(
        private val settings: Settings
    ) : LocaleSettingDelegate {
        override var localeSetting: String?
            get() = settings.getStringOrNull(PREFKEY_LOCALE)
            set(value) {
                if(value != null) {
                    settings.putString(PREFKEY_LOCALE, value)
                }else {
                    settings.remove(PREFKEY_LOCALE)
                }
            }
    }


    val supportedUiLanguages: List<UstadMobileSystemCommon.UiLanguage> = availableLanguagesConfig
        .split(",")
        .sorted()
        .map {
            UstadMobileSystemCommon.UiLanguage(it, (UstadMobileConstants.LANGUAGE_NAMES[it] ?: it))
        }

    private val supportedLangMap: Map<String, UstadMobileSystemCommon.UiLanguage> = supportedUiLanguages
        .associateBy { it.langCode }

    /**
     * The user selected locale within the app (if any). This should be the language code as per
     * the UiLanguage object, or null to indicate that the user has not explicitly selected any
     * language
     */
    var localeSetting: String?
        get() = localeSettingDelegate.localeSetting
        set(value) {
            localeSettingDelegate.localeSetting = value
            displayedLocale = displayLocaleForLangSetting(value)
        }

    /**
     * This is stored because it will be looked up every time a string lookup is done via systemImpl
     */
    @Volatile
    var displayedLocale: String = displayLocaleForLangSetting(localeSetting)
        private set

    init {
        if(!supportedLangMap.containsKey(fallbackLocaleCode))
            throw IllegalStateException("available languages $availableLanguagesConfig does not " +
                    "include fallback: '$fallbackLocaleCode'")
    }

    private fun displayLocaleForLangSetting(setting: String?): String {
        return if(setting.isNullOrEmpty())
            selectFirstSupportedLocale().langCode
        else
            setting
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
        return supportedUiLanguagesAndSysDefault(systemImpl.getString(MR.strings.use_device_language))
    }

    /**
     * Select the locale to display based on a list of the locales preferred by the user. On Android
     * and other systems, the user can specify a list of their preferred languages in order. We will
     * select the first language in that list that is in the supported. If none is available, then we
     * will use the fallback.
     */
    fun selectFirstSupportedLocale(
        preferredLocales: List<String> = systemLocales,
    ): UstadMobileSystemCommon.UiLanguage {
        val supportedLocaleCodes = supportedUiLanguages.map {
            it.langCode
        }

        return preferredLocales.firstOrNull {
            it.substring(0, 2) in supportedLocaleCodes
        }?.let { supportedLangMap[it.substring(0, 2)]!! } ?: supportedLangMap[fallbackLocaleCode]!!
    }

    companion object {

        const val PREFKEY_LOCALE = "locale"

        /**
         * Preset locale setting (metadata key): if present, this locale will be automatically set
         * as the app locale (regardless of the device default). This can be useful where a particular
         * build is intended for an area with one language, especially if that language is not
         * supported as a locale by most operating systems.
         */
        const val METADATA_KEY_PRESET_LANG = "com.ustadmobile.presetlocale"

        /**
         * Key for setting that is used to track if the preset was applied
         */
        const val PREFKEY_ACTIONED_PRESET = "localePresetDone"

        const val DEFAULT_SUPPORTED_LANGUAGES = "en,fa,ps,ar,tg,bn,ne,my,ru"

        const val LOCALE_USE_SYSTEM = ""
    }

}
