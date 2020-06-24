package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class StatementListReport : StatementEntity() {

    @Embedded
    var person: Person? = null

    @Embedded
    var xlangMapEntry: XLangMapEntry? = null

}