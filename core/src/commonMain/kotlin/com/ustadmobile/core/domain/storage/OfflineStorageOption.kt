package com.ustadmobile.core.domain.storage

import dev.icerock.moko.resources.StringResource


data class OfflineStorageOption(
    val label: StringResource,
    val path: String,
)
