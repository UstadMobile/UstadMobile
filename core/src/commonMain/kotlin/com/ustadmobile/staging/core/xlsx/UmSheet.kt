package com.ustadmobile.staging.core.xlsx


/**
 * COMMON MAIN
 */
expect class UmSheet {

    var title: String

    var sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>?
        internal set


    inner class TableValue{}

    constructor(newTitle: String)

    constructor(newTitle: String, sheetValues: MutableList<TableValue>,
                sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>)

    fun addValueToSheet(r: Int, c: Int, value: String)

    fun getSheetValues(): List<TableValue>

}
