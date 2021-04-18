package com.ustadmobile.core.io.ext

import android.net.Uri
import androidx.core.net.toFile

actual fun String.parseKmpUriStringToFile() = Uri.parse(this).toFile()

