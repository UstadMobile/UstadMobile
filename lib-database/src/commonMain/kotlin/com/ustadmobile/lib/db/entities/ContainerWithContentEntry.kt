package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerWithContentEntry() : Container() {

    var entryId: String? = null

    var sourceUrl: String ? = null
}
