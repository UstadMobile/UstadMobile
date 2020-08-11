package com.ustadmobile.core.util

import kotlinx.io.InputStream

expect fun getAssetFromResource(path: String, context: Any): InputStream?

