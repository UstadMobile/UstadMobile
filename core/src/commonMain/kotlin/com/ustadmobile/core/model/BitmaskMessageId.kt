package com.ustadmobile.core.model

import com.ustadmobile.core.util.ext.hasFlag
import dev.icerock.moko.resources.StringResource

data class BitmaskMessageId(val flagVal: Long, val stringResource: StringResource) {
    fun toBitmaskFlag(value: Long) = BitmaskFlag(flagVal, stringResource, value.hasFlag(flagVal))
}
