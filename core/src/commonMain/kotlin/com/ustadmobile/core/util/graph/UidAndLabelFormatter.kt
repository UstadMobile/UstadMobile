package com.ustadmobile.core.util.graph

import com.ustadmobile.lib.db.entities.UidAndLabel

class UidAndLabelFormatter(val list: List<UidAndLabel>) : LabelValueFormatter {

    override fun format(option: Any): String {
        return list.firstOrNull { it.uid == (option as String).toLong() }?.labelName ?: "label not found: $option"
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { x -> list.firstOrNull { it.uid == (x as String).toLong() }?.labelName ?: "label not found $x" }
    }

}