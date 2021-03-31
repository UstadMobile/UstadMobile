package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PersonWithSessionDetailDisplay : StatementEntity() {

    @Embedded
    var verb: VerbEntity? = null

    var verbDisplay: String?= null

    var objectDisplay: String? = null

}