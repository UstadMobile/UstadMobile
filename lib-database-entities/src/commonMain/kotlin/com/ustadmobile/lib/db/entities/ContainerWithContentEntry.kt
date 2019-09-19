package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerWithContentEntry() : Container() {

    var entryId = ""

    var sourceUrl = ""
}
