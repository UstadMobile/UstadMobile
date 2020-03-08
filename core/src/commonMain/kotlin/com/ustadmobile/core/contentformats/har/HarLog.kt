package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
data class HarLog(var entries: List<HarEntry>)
