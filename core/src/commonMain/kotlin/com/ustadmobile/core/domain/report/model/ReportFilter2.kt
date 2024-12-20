package com.ustadmobile.core.domain.report.model

import kotlinx.serialization.Serializable

/** Enum representing different comparison types */
enum class Comparisons {
    EQUALS,
    NOT_EQUALS,
    GREATER,
    LESSER,
    BETWEEN,
    IN_LIST,
    NOT_IN_LIST
}

/** Sealed class representing different types of report filters */
@Serializable
sealed class ReportFilter2(
    val comparisonTypes: List<Comparisons>
) {
    @Serializable
    data class GenderFilter(
        val gender: String
    ) : ReportFilter2(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.NOT_EQUALS)
    )

    @Serializable
    data class AgeFilter(
        val age: Int
    ) : ReportFilter2(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.NOT_EQUALS, Comparisons.GREATER, Comparisons.LESSER, Comparisons.BETWEEN)
    )

    @Serializable
    data class ContentCompletionFilter(
        val completionPercentage: Int
    ) : ReportFilter2(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.GREATER, Comparisons.LESSER)
    )
}