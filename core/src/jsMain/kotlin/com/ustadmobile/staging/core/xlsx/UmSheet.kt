package com.ustadmobile.staging.core.xlsx


/**
 *
 * JS
 */

actual class UmSheet {

    actual var title: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual var sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual inner class TableValue{}

    actual constructor(newTitle: String) {
        TODO("not implemented")
        //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor(newTitle: String, sheetValues: MutableList<TableValue>,
                sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>){
        TODO("not implemented")
    }

    actual fun addValueToSheet(r: Int, c: Int, value: String) {
        TODO("not implemented")
    }

    actual fun getSheetValues(): List<TableValue> {
        TODO("not implemented")
    }



}
