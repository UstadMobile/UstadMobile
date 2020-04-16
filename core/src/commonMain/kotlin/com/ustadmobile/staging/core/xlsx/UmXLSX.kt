package com.ustadmobile.staging.core.xlsx


/**
 * COMMON MAIN
 */
expect class UmXLSX {


    internal var title: String
    internal var filePath: String
    internal var workingPath: String

    constructor(title: String, filePath: String, workingPath: String)

    fun addSheet(newSheet: UmSheet)

    fun createXLSX()

}
