package com.ustadmobile.core.domain.siteterms

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig

/**
 * Determine the best language to use to show site terms and conditions. The preferred list of
 * languages is the user set locale (if any), followed by the list of preferred languages as
 * per the system preferences. The use case will select the first language from the preferred list
 * in which the site terms are available.
 */
class GetLocaleForSiteTermsUseCase (
    private val supportedLangConfig: SupportedLanguagesConfig,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke() : String {
        val preferredLocales =  listOf(supportedLangConfig.displayedLocale) +
                supportedLangConfig.systemLocales
        val availableLocales = repo.siteTermsDao()
            .findAvailableSiteTermLanguages(1).filterNotNull()
        return preferredLocales.firstOrNull { it in availableLocales } ?: FALLBACK
    }

    companion object {

        const val FALLBACK = "en"
    }

}