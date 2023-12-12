package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.lib.db.entities.SiteTerms
import org.kodein.di.DI

data class SiteTermsDetailUiState(

    val siteTerms: SiteTerms? = null,

)

class SiteTermsDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<SiteTerms>(di, savedStateHandle, DEST_NAME) {


    companion object {

        const val DEST_NAME = "Terms"

    }
}
