package com.ustadmobile.core.util

import android.system.Os

actual fun createSymLink(oldPath: String, targetPath: String){
    Os.symlink(oldPath, targetPath)
}