package com.ustadmobile.sharedse.io

actual fun FileSe.renameFile(file: FileSe): Boolean {
    return this.renameTo(file)
}