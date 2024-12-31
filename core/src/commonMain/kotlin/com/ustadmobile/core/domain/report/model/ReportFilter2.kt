package com.ustadmobile.core.domain.report.model

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

/** Enum representing different comparison types */
enum class Comparisons(val value: Int) {
    EQUALS(1),
    NOT_EQUALS(2),
    GREATER(3),
    LESSER(4),
    BETWEEN(5),
    IN_LIST(6),
    NOT_IN_LIST(7);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            EQUALS to MR.strings.equals,
            NOT_EQUALS to MR.strings.not_equals,
            GREATER to MR.strings.greater,
            LESSER to MR.strings.lesser,
            BETWEEN to MR.strings.between,
            IN_LIST to MR.strings.in_list,
            NOT_IN_LIST to MR.strings.not_in_list
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForComparison(comparison: Comparisons): StringResource {
            return stringResourceMap[comparison] ?: MR.strings.equals
        }
    }
}


/** Sealed class representing different types of report filters */
@Serializable
sealed class ReportFilter2(
    val comparisonTypes: List<Comparisons>,
    val reportFilterSeriesUid: Int = 0
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
        comparisonTypes = listOf(
            Comparisons.EQUALS,
            Comparisons.NOT_EQUALS,
            Comparisons.GREATER,
            Comparisons.LESSER,
            Comparisons.BETWEEN
        )
    )

    @Serializable
    data class ContentCompletionFilter(
        val completionPercentage: Int
    ) : ReportFilter2(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.GREATER, Comparisons.LESSER)
    )
}

@Serializable
data class ReportFilter3(
    var reportFilterUid: Int = 0,

    var reportFilterSeriesUid: Int = 0,

    var reportFilterField: FilterType? = null,

    var reportFilterCondition: Comparisons? = null,

    var reportFilterValue: String? = ""
)

fun getComparisonSymbol(comparison: Comparisons): String {
    return when (comparison) {
        Comparisons.EQUALS -> "="
        Comparisons.NOT_EQUALS -> "!="
        Comparisons.GREATER -> ">"
        Comparisons.LESSER -> "<"
        Comparisons.BETWEEN -> "BETWEEN"
        Comparisons.IN_LIST -> "IN"
        Comparisons.NOT_IN_LIST -> "NOT IN"
    }
}
