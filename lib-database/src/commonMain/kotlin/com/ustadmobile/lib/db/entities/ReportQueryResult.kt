package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Report queries can be large, complex, and run over millions of rows. They may also have to be run
 * on the server side when a client does not have a full sync of all the relevant data.
 *
 * The ReportQueryResult entity provides a way to cache results client side and server side, as well
 * as way to serialize results when they are transferred between the server and client.
 *
 * There is one ReportQueryResult per xAxis/subgroup combination query result as per
 * GenerateReportQueriesUseCase.
 *
 * @param rqrUid auto generated UID
 * @param rqrReportUid foreign key for Report.reportUid (there is a one:many relationship between
 *        Report and ReportQueryResult)
 * @param rqrLastModified when the result was actually generated
 * @param rqrLastValidated
 * @param rqrReportSeriesUid series uid as per ReportOptions2.reportSeriesUid
 * @param rqrXAxis XAxis value
 * @param rqrYAxis YAxis value
 * @param rqrSubgroup subgroup (if any, otherwise empty string)
 * @param rqrTimeZone the timezone for which this result is valid. The start/end period of the
 *        query, and potentially xAxis/subgrouping depends on the timezone. Running the same report
 *        options with a different timezone can produce different results. This is the TimeZone.id .
 */
@Entity(
    indices = arrayOf(
        Index(
            value = arrayOf("rqrReportUid", "rqrTimeZone"),
            name = "idx_reportqueryresult_rqrreportuid_rqrtimezone",
            unique = false
        )
    )
)
@Serializable
data class ReportQueryResult(
    @PrimaryKey(autoGenerate = true)
    var rqrUid: Long = 0,
    var rqrReportUid: Long = 0,
    var rqrLastModified: Long = 0,
    var rqrLastValidated: Long = 0,
    var rqrReportSeriesUid: Int = 0,
    var rqrXAxis: String = "",
    var rqrYAxis: Double = 0.0,
    var rqrSubgroup: String = "",
    var rqrTimeZone: String = "",
)
