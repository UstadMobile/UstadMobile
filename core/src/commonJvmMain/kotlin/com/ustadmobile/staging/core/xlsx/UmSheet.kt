package com.ustadmobile.staging.core.xlsx


actual class UmSheet {
    actual var title: String
    internal var sheetValues: MutableList<TableValue>
    actual var sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>? = null
        internal set

    val tableValueList: List<TableValue>
        get() {
            if (sheetMap == null) {
                return sheetValues
            }
            val returnMe = ArrayList<TableValue>()
            val sheetIterator = sheetMap!!.keys.iterator()
            while (sheetIterator.hasNext()) {
                val r = sheetIterator.next()
                val coMap = sheetMap!![r]
                val colIterator = coMap!!.keys.iterator()
                while (colIterator.hasNext()) {
                    val c = colIterator.next()
                    val v = coMap[c]
                    returnMe.add(TableValue(r, c, v!!))
                }
            }
            return returnMe
        }

    actual inner class TableValue internal constructor(internal var rowIndex: Int,
                                                internal var colIndex: Int,
                                                internal var value: String)

    actual constructor(newTitle: String) {
        this.title = newTitle
        this.sheetValues = ArrayList()
        this.sheetMap = LinkedHashMap()
    }

    actual constructor(newTitle: String, sheetValues: MutableList<TableValue>,
                sheetMap: LinkedHashMap<Int, LinkedHashMap<Int, String>>) {

        this.title = newTitle
        this.sheetValues = sheetValues
        this.sheetMap = sheetMap

    }

    actual fun addValueToSheet(r: Int, c: Int, value: String) {
        val newTableValue = TableValue(r, c, value)

        //replace
        if (sheetMap!!.containsKey(r)) {
            val insideMap: LinkedHashMap<Int, String>?
            insideMap = sheetMap!![r]
            if (insideMap!!.containsKey(c)) {
                insideMap[c] = value
            } else {
                insideMap[c] = value
            }
            sheetMap!![r] = insideMap
        } else {
            val insideMap = LinkedHashMap<Int, String>()
            insideMap[c] = value
            sheetMap!![r] = insideMap
        }

        var added = false
        var index = -1

        for (everyValue in sheetValues) {
            if (everyValue.colIndex == c && everyValue.rowIndex == r) {
                index = sheetValues.indexOf(everyValue)
                added = true
                break
            }
        }

        if (index > -1) {
            sheetValues[index] = newTableValue
        }

        if (!added) {
            sheetValues.add(newTableValue)
        }

    }

    actual fun getSheetValues(): List<TableValue> {
        return sheetValues
    }
}
