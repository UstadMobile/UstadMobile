package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage

data class SiteDetailUiState(

    val site: Site? = null,
    val siteTerms: List<SiteTermsWithLanguage> = emptyList()

)

class SiteDetailViewModel  {

    companion object {

        const val DEST_NAME = "SiteDetail"
    }
}




