package com.ustadmobile.core.util

import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

actual fun createSymLink(oldPath: String, targetPath: String){
    Files.createSymbolicLink(Paths.get(targetPath), Paths.get(oldPath))
}