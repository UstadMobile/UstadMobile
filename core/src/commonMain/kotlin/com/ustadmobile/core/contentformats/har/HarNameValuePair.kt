package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class HarNameValuePair(val name: String, val value: String)