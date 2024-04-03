package com.ustadmobile.libuicompose.util.ext

import com.ustadmobile.core.domain.compress.CompressionLevel
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

val CompressionLevel.stringResource: StringResource
    get() = when(this) {
        CompressionLevel.HIGH -> MR.strings.compression_high
        CompressionLevel.MEDIUM -> MR.strings.compression_medium
        CompressionLevel.LOW -> MR.strings.compression_low
        CompressionLevel.NONE -> MR.strings.compression_none
    }
