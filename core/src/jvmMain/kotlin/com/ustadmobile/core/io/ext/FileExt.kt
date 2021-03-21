package com.ustadmobile.core.io.ext

import java.io.File

actual fun File.toKmpUriString() = this.toURI().toString()
