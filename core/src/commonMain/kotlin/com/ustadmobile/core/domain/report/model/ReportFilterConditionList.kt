package com.ustadmobile.core.domain.report.model

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

/** Enum representing different comparison types */
enum class Comparisons(
    val value: Int,
    val stringResource: StringResource,
    val symbol: String
) {
    EQUALS(1, MR.strings.equals, "="),
    NOT_EQUALS(2, MR.strings.not_equals, "!="),
    GREATER(3, MR.strings.greater, ">"),
    LESSER(4, MR.strings.lesser, "<"),
    BETWEEN(5, MR.strings.between, "BETWEEN");
}


/** Sealed class representing different types of report filters */
@Serializable
sealed class ReportFilterConditionList(
    val comparisonTypes: List<Comparisons>,
) {
    @Serializable
     class GenderFilter : ReportFilterConditionList(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.NOT_EQUALS)
    )

    @Serializable
     class AgeFilter : ReportFilterConditionList(
        comparisonTypes = listOf(
            Comparisons.EQUALS,
            Comparisons.NOT_EQUALS,
            Comparisons.GREATER,
            Comparisons.LESSER,
            Comparisons.BETWEEN
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
