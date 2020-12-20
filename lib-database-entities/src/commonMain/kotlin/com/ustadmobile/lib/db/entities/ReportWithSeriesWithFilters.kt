package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportWithSeriesWithFilters() : Report() {

    constructor(report: Report, reportSeries: List<ReportSeries> = listOf()) : this() {
        this.reportUid = report.reportUid
        this.reportTitle = report.reportTitle
        this.reportOwnerUid = report.reportOwnerUid
        this.reportInactive = report.reportInactive
        this.fromDate = report.fromDate
        this.toDate = report.toDate
        this.xAxis = report.xAxis
        this.reportSeries = report.reportSeries
        reportSeriesWithFiltersList = reportSeries
    }

    var reportSeriesWithFiltersList = listOf<ReportSeries>()


    data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)


    fun toSql(): QueryParts {
     /*   require(xAxis != subGroup) { "XAxis Selection and subGroup selection was the same" }
        val paramList = mutableListOf<Any>()

        var sqlList = """SELECT  Person.* , XLangMapEntry.* ,StatementEntity.* 
                FROM StatementEntity 
                LEFT JOIN Person ON Person.personUid = StatementEntity.statementPersonUid 
                LEFT JOIN XLangMapEntry ON StatementEntity.statementVerbUid = XLangMapEntry.verbLangMapUid """

        var sql = "SELECT " + when (yAxis) {
            SCORE -> "AVG(StatementEntity.resultScoreScaled) AS yAxis, "
            DURATION -> "SUM(StatementEntity.resultDuration) AS yAxis, "
            AVG_DURATION -> "AVG(StatementEntity.resultDuration) AS yAxis, "
            COUNT_ACTIVITIES -> "COUNT(*) AS yAxis, "
            else -> ""
        }
        sql += groupBy(xAxis) + "AS xAxis "
        if (subGroup != 0) {
            sql += " , " + groupBy(subGroup) + "AS subgroup "
        }
        sql += "FROM StatementEntity "

        val objectsList = reportFilterList.filter { it.entityType == ReportFilter.CONTENT_FILTER }.map { it.entityUid }
        val whoFilterList = reportFilterList.filter { it.entityType == ReportFilter.PERSON_FILTER }.map { it.entityUid }
        val didFilterList = reportFilterList.filter { it.entityType == ReportFilter.VERB_FILTER }.map { it.entityUid }


        if (xAxis == GENDER || subGroup == GENDER) {
            sql += "LEFT JOIN PERSON ON Person.personUid = StatementEntity.statementPersonUid "
        }
        if (objectsList.isNotEmpty() || whoFilterList.isNotEmpty() || didFilterList.isNotEmpty() || (toDate > 0L && fromDate > 0L)) {
            var where = "WHERE "
            sql += where
            sqlList += where

            val whereList = mutableListOf<String>()
            if (objectsList.isNotEmpty()) {
                whereList.add("(StatementEntity.xObjectUid IN (?) OR " +
                        "EXISTS(SELECT contextXObjectStatementJoinUid FROM ContextXObjectStatementJoin " +
                        "WHERE contextStatementUid = StatementEntity.statementUid AND contextXObjectUid IN (?))) ")
                paramList.addAll(listOf<Any>(objectsList, objectsList))
            }
            if (whoFilterList.isNotEmpty()) {
                whereList.add("StatementEntity.statementPersonUid IN (?) ")
                paramList.addAll(listOf<Any>(whoFilterList))
            }
            if (didFilterList.isNotEmpty()) {
                whereList.add("StatementEntity.statementVerbUid IN (?) ")
                paramList.addAll(listOf<Any>(didFilterList))
            }
            if (toDate > 0L && fromDate > 0L) {
                whereList.add("(StatementEntity.timestamp >= ? AND StatementEntity.timestamp <= ?) ")
                paramList.add(fromDate)
                paramList.add(toDate)
            }
            var whereListStr = whereList.joinToString("AND ")
            sql += whereListStr
            sqlList += whereListStr

        }
        sql += "GROUP BY xAxis"
        if (subGroup != 0) {
            sql += ", subgroup"
        }
*/
        return QueryParts("", "", arrayOf(""))
    }

    private fun groupBy(value: Int): String {
        return when (value) {
            DAY -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            WEEK -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') "
            MONTH -> "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            CONTENT_ENTRY -> "StatementEntity.xObjectUid "
            //LOCATION -> "Location.title"
            GENDER -> "Person.gender "
            else -> ""
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ReportWithSeriesWithFilters

        return true
    }



    companion object{



    }
}