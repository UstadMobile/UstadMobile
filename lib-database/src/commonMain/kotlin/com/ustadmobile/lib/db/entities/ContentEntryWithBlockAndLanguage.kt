package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithBlockAndLanguage : ContentEntry() {

    @Embedded
    var language: Language? = null

    @Embedded
    var block: CourseBlock? = null

}