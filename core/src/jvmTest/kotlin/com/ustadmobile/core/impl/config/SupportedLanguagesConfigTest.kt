package com.ustadmobile.core.impl.config

import com.russhwolf.settings.Settings
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class SupportedLanguagesConfigTest {

    private lateinit var mockSettings: Settings

    val defaultSupportedLocales = "ru,tg,fa,en"

    @BeforeTest
    fun setup() {
        mockSettings = mock { }
    }

    @Test
    fun givenPreferredLanguageSupported_whenSelectFirstSupportedLocale_thenLanguageReturned() {
        val preferredLocales = listOf("ar-AE", "fa-AF", "en")
        val config = SupportedLanguagesConfig(
            availableLanguagesConfig = defaultSupportedLocales,
            systemLocales = preferredLocales,
            settings = mockSettings,
        )

        val selectedLang = config.selectFirstSupportedLocale(
            preferredLocales)
        assertEquals("fa", selectedLang.langCode,
            "Selected the first supported language in list based on user preference order")
    }

    @Test
    fun givenNoPreferredLanguageSupported_whenSelectFirstSupportedLocale_thenFallbackIsReturned() {
        val preferredLocales = listOf("de-DE", "fr-FR", "pl-PL")
        val fallback = "ru"
        val config = SupportedLanguagesConfig(
            availableLanguagesConfig = defaultSupportedLocales,
            systemLocales = preferredLocales,
            fallbackLocaleCode = fallback,
            settings = mockSettings,
        )
        val selectedLang = config.selectFirstSupportedLocale(
            preferredLocales)

        assertEquals(fallback, selectedLang.langCode,
            "When no preferred language is supported, the fallback is selected")
    }

    @Test
    fun givenLanguageSettingPresent_whenDisplayedLocaleQueried_thenSettingIsReturned() {
        val userSetLanguage = "tg"
        mockSettings.stub {
            on { getStringOrNull(SupportedLanguagesConfig.PREFKEY_LOCALE) }.thenReturn(userSetLanguage)
        }

        val config = SupportedLanguagesConfig(
            systemLocales = listOf("ru"),
            settings = mockSettings,
        )

        assertEquals(userSetLanguage, config.displayedLocale)
    }

    @Test
    fun givenNoLanguageSettingPresent_whenDisplayLocaleQueried_thenFirstPreferredLangIsReturned() {
        val config = SupportedLanguagesConfig(
            systemLocales = listOf("ru", "en"),
            availableLanguagesConfig = "tg,ru,en",
            settings = mockSettings,
        )

        assertEquals("ru", config.displayedLocale)
    }

}