package com.ustadmobile.core.model

import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

data class BitmaskFlag(val flagVal: Long, val stringResource: StringResource, var enabled: Boolean = false)
