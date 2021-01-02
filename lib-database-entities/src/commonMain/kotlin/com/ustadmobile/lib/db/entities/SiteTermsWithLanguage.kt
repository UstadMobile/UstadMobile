package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class SiteTermsWithLanguage : SiteTerms(){

    @Embedded
    var stLanguage: Language? = null

}