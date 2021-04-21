package com.ustadmobile.core.util.graph

import com.ustadmobile.core.util.ext.truncate

class CodeLabelFormatter: LabelValueFormatter {

    var map: Map<String, String>? = null

    override fun format(option: Any): String {
        return map?.get((option as String)) ?: "label not found: $option"
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { x ->
            format(x).truncate()
        }
    }
}