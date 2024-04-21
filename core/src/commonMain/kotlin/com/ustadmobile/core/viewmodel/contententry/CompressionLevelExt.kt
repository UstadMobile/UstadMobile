package com.ustadmobile.core.viewmodel.contententry

import com.ustadmobile.core.domain.compress.CompressionLevel
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

val CompressionLevel.stringResource: StringResource
    get() = when(this) {
        CompressionLevel.HIGHEST -> MR.strings.compression_highest
        CompressionLevel.HIGH -> MR.strings.compression_high
        CompressionLevel.MEDIUM -> MR.strings.compression_medium
        CompressionLevel.LOW -> MR.strings.compression_low
        CompressionLevel.LOWEST -> MR.strings.compression_lowest
        CompressionLevel.NONE -> MR.strings.compression_none
    }
