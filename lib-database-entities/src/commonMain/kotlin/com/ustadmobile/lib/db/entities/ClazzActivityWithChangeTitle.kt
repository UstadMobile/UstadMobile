package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzActivityWithChangeTitle : ClazzActivity() {

    var changeTitle: String? = null
}
