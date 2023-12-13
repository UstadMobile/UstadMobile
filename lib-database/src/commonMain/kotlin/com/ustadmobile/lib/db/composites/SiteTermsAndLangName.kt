package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.SiteTerms

/**
 * @param langDisplayName the display name (looked up via supporteduilang)
 */
data class SiteTermsAndLangName(
    val terms: SiteTerms,
    val langDisplayName: String,
)