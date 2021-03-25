package com.ustadmobile.core.util

import java.io.InputStream

expect fun getAssetFromResource(path: String, context: Any): InputStream?
