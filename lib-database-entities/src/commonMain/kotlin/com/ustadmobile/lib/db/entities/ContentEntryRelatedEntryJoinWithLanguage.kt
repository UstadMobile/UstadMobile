package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryRelatedEntryJoinWithLanguage() {

    var cerejContentEntryUid: Long = 0

    var cerejRelatedEntryUid: Long = 0

    var languageName: String? = null
}
