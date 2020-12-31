package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class WorkspaceTermsWithLanguage : WorkspaceTerms(){

    @Embedded
    var wtLanguage: Language? = null

}