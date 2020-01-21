package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithPersonPicture : Person() {
    var personPictureUid: Long = 0


}
