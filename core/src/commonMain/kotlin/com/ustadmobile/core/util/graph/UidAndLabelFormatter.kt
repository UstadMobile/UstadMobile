package com.ustadmobile.core.util.graph

class UidAndLabelFormatter(val uidMap: Map<Long, String?>) : LabelValueFormatter {

    override fun format(option: Any): String {
        return uidMap[(option as String).toLong()] ?: "label not found: $option"
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { x -> format(x) }
    }

}