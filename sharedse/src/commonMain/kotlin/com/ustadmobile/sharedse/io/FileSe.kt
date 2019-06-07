package com.ustadmobile.sharedse.io

expect open class FileSe {

    constructor(path: String)

    constructor(base: FileSe, relPath: String)

    fun lastModified(): Long

    fun exists(): Boolean

    fun length(): Long

    fun renameTo(otherFiel: FileSe): Boolean

}


