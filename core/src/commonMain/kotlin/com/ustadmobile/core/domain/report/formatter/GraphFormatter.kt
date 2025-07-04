package com.ustadmobile.core.domain.report.formatter

/**
 * @param T the type of value e.g. Double for numerical axis, String for category/subgroups, etc.
 */
interface GraphFormatter<T> {

    /**
     * Some graph libraries accept a formatter function directly for labels, others require a
     * list of numbers which the library will then handle directly.
     *
     * For example, when graphing duration, the raw underlying data is typically in milliseconds,
     * but we need to show this to the user in hours/days etc.
     *
     * This is where the adjust function comes in: GraphFormatters have access to the entire result
     * data set, so they can determine maximum/minimum values, and what a sensible unit would be.
     *
     * The adjust function can be either by applied to all data before it is passed to a graph
     * library, or it can be used in an axis formatter function.
     *
     * Where a value does not need adjusted, it can simply be returned as-is (e.g. on X-Axis or
     * subgroups by course, gender, etc).
     *
     * @param value the value to adjust
     */
    fun adjust(value: T): T

    /**
     * Format a value for display.
     *
     * @param value the value to format
     */
    fun format(value: T): String

}
