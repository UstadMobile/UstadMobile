package com.ustadmobile.core.domain.report.utils

import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.SeriesType

/**
 * Not all GraphSeries will include all XAxis values e.g. where XAxis is Gender or class based, and
 * a particular series has no data for that xAxis value.
 *
 * GraphSeries are only guaranteed to have all xAxis values when being run by date - see
 * RunReportUseCase#fillIfNeeded
 *
 *
 */
fun List<GraphSeries>.getDistinctSortedXValues(): List<Any> =
    flatMap { it.data.map { row -> row.xAxis } }
        .distinct()
        .sorted()

/**
 * Creates a map of x-value to its index position
 */
fun List<Any>.toIndexMap(): Map<Any, Int> =
    withIndex().associate { (index, xValue) -> xValue to index }

/**
 * Gets all distinct subgroups from the series data, treating null as empty string
 */
fun List<GraphSeries>.getDistinctSubgroups(): List<String> =
    flatMap { series ->
        series.data.map { dataPoint ->
            dataPoint.subgroup ?: ""
        }
    }.distinct()

/**
 * Gets the maximum Y-axis value from all series data points
 */
fun List<GraphSeries>.getMaxYValue(): Double =
    flatMap { it.data.map { row -> row.yAxis } }
        .maxOrNull() ?: 0.0

fun List<GraphSeries>.groupSeriesWithSubgroups(seriesType: SeriesType): Map<String, List<String>> {
    return this.filter { it.type == seriesType }
        .groupBy { it.name }
        .mapValues { entry ->
            entry.value.flatMap { it.data }
                .mapNotNull { it.subgroup }
                .filter { it.isNotEmpty() }
                .distinct()
        }
        .filterValues { it.isNotEmpty() }
}