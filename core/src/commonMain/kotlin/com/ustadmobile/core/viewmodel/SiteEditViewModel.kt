package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage

data class SiteEditUiState(
    val site: Site? = null,
    val siteTerms: List<SiteTermsWithLanguage> = emptyList(),
    val fieldsEnabled: Boolean = true,
    val siteNameError: String? = null
)

class SiteEditViewModel  {

    companion object {

        const val DEST_NAME = "SiteEdit"

    }

}