package com.ustadmobile.core.domain.report.model

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

/** Enum representing different comparison types */
enum class Comparisons(
    override val label: StringResource,
    val symbol: String
) : OptionWithLabelStringResource {
    EQUALS(MR.strings.equals, "="),
    NOT_EQUALS(MR.strings.not_equals, "!="),
    GREATER(MR.strings.greater, ">"),
    LESSER(MR.strings.lesser, "<"),
    GREATER_OR_EQUAL(MR.strings.greater_or_equal, ">="),
    LESSER_OR_EQUAL(MR.strings.lesser_or_equal, "<="),
}

/** Sealed class representing different types of report filters */
@Serializable
sealed class ReportConditionFilterOptions(
    val comparisonTypes: List<Comparisons>,
) {
    @Serializable
    class GenderConditionFilter : ReportConditionFilterOptions(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.NOT_EQUALS)
    )

    @Serializable
    class AgeConditionFilter : ReportConditionFilterOptions(
        comparisonTypes = listOf(
            Comparisons.EQUALS,
            Comparisons.NOT_EQUALS,
            Comparisons.GREATER,
            Comparisons.LESSER,
            Comparisons.GREATER_OR_EQUAL,
            Comparisons.LESSER_OR_EQUAL,
        )
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


interface OptionWithLabelStringResource {
    val label: StringResource
}
