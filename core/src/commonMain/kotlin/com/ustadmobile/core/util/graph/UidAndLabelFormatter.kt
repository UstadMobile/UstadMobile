package com.ustadmobile.core.util.graph

import kotlin.math.min

class UidAndLabelFormatter(val uidMap: Map<Long, String?>) : LabelValueFormatter {

    override fun format(option: Any): String {
        return uidMap[(option as String).toLong()] ?: "label not found: $option"
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { x ->
            val str = format(x)
            val min = min(15, str.length)
            var newStr = str.substring(0, min)
            if (min == 15 && str.length != 15) {
                newStr += "..."
            }
            newStr
        }
    }

}