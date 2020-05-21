package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithLanguage: ContentEntry {

    constructor()

    @Embedded
    var language: Language? = null

    constructor(entry: ContentEntry){
        this.title = entry.title
        this.description = entry.description
        this.author = entry.author
        this.licenseType = entry.licenseType
        this.contentFlags = entry.contentFlags
        this.leaf = entry.leaf
    }
}