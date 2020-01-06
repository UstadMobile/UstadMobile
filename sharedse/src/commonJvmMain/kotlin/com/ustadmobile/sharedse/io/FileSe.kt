package com.ustadmobile.sharedse.io

actual typealias FileSe = java.io.File

actual fun FileSe.renameFile(file: FileSe): Boolean {
    return this.renameTo(file)
}
