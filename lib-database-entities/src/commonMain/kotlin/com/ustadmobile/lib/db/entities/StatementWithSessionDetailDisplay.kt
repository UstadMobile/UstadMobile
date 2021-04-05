package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class StatementWithSessionDetailDisplay : StatementEntity() {

    @Embedded
    var verb: VerbEntity? = null

    var verbDisplay: String?= null

    var objectDisplay: String? = null

}