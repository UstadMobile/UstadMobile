package com.ustadmobile.sharedse.io

import com.ustadmobile.core.impl.UMLog
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual fun FileSe.renameFile(file: FileSe): Boolean {
    try {
        Files.move(this.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    } catch (e: IOException) {
        UMLog.l(UMLog.DEBUG, 0, e.message)
        return false
    }
    return true
}