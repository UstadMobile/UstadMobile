package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.lib.db.composites.StatementReportRow
import kotlinx.serialization.Serializable

/**
 * Use case to accept request to run a report and generate report results (that can then be presented
 * as a graph). This could be implemented via:
 *
 * a) running the report directly on the database
 * b) sending the report options to the server
 * c) using a cached report result
 */
interface RunReportUseCase {

    /**
     * Data class representing report results in a format that can be turned into a graph
     *
     * @param timestamp when the report was actually run (in ms since epoch)
     * @param request the run report request that generated the result (including report options)
     * @param results data points that can be used to draw a graph. This is a list of statementreportrow
     *        lists, where the first list is in the same order as request.options.series (e.g. for each series
     *        there is a list of the results). The order of statementreportrows depends on the the
     *        xAxis (e.g. whether it is based on dates or not).
     */
    @Serializable
    data class RunReportResult(
        val timestamp: Long,
        val request: RunReportRequest,
        val results: List<List<StatementReportRow>>
    )

    /**
     * Data class representing a request to run a report.
     *
     * @param reportOptions ReportOptions as selected by user (xAxis, yAxis, time range, etc)
     * @param accountPersonUid personUid of the person running the query. This will affect the result
     *        as the data that can be accessed depends on the permissions available to the person.
     * @param cacheControl cache control - would be used to set must-revalidate, only-if-cached, etc.
     */
    @Serializable
    data class RunReportRequest(
        val reportOptions: ReportOptions2,
        val accountPersonUid: Long,
        val cacheControl: String? = null,
    )

    /**
     * Run the report as per options provided and return the result.
     *
     * @param request the report request to run
     * @return result containing data that can be graphed
     */
    suspend operator fun invoke(
        request: RunReportRequest
    ): RunReportResult

}