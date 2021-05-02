package com.ustadmobile.core.io.ext

import androidx.core.net.toUri
import java.io.File

actual fun File.toKmpUriString() = this.toUri().toString()
