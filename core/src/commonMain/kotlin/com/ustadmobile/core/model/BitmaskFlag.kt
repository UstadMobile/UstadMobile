package com.ustadmobile.core.model

import kotlinx.serialization.Serializable

@Serializable
data class BitmaskFlag(val flagVal: Long, val messageId: Int, var enabled: Boolean = false)
