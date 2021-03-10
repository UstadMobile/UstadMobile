package com.ustadmobile.core.util.graph

interface LabelValueFormatter {

    fun format(option: Any): String

    fun formatAsList(option: List<Any>): List<String>

}