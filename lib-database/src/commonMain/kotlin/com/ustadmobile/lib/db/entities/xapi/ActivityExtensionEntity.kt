package com.ustadmobile.lib.db.entities.xapi

data class ActivityExtensionEntity(
    var aeeActivityUid: Long = 0,
    var aeeKeyHash: Long = 0,
    var aeeKey: String? = null,
    var aeeJson: String? = null,
)
