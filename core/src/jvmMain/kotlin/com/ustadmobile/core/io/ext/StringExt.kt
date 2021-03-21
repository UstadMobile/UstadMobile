package com.ustadmobile.core.io.ext

import java.net.URI
import java.nio.file.Paths

actual fun String.parseKmpUriStringToFile() = Paths.get(URI(this)).toFile()
