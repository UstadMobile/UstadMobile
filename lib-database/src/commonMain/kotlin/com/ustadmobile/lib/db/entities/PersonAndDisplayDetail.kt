package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndDisplayDetail(
    @Embedded
    var person: Person? = null,

    @Embedded
    var parentJoin: PersonParentJoin? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var personPictureTransferJobItem: TransferJobItem? = null
)