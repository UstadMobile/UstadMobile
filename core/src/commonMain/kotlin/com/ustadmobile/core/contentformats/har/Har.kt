package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
data class Har(var log: HarLog)