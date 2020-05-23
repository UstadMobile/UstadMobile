package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithLanguage: ContentEntry() {

    @Embedded
    var language: Language? = null
}