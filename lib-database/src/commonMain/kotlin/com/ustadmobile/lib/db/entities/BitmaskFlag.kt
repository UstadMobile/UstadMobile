package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class BitmaskFlag(val flagVal: Long, val messageId: Int, var enabled: Boolean = false)
