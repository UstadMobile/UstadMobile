package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportWithFilters() : Report() {

    constructor(report: Report, filterList: List<ReportFilter>) : this() {
        this.reportUid = report.reportUid
        this.reportTitle = report.reportTitle
        this.reportOwnerUid = report.reportOwnerUid
        this.reportInactive = report.reportInactive
        this.fromDate = report.fromDate
        this.toDate = report.toDate
        this.chartType = report.chartType
        this.yAxis = report.yAxis
        this.xAxis = report.xAxis
        this.subGroup = report.subGroup
        this.reportFilterList = filterList
    }

    var reportFilterList: List<ReportFilter> = listOf()


    data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)


    fun toSql(): QueryParts {
        require(xAxis != subGroup) { "XAxis Selection and subGroup selection was the same" }
        val paramList = mutableListOf<Any>()

        var sqlList = "SELECT (Person.firstNames || ' ' || Person.lastName) AS name, " +
                "XLangMapEntry.valueLangMap AS verb, " +
                "StatementEntity.resultSuccess AS result, " +
                "StatementEntity.timestamp As whenDate " +
                "FROM StatementEntity " +
                "LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid " +
                "LEFT JOIN XLangMapEntry ON StatementEntity.verbUid = XLangMapEntry.verbLangMapUid "

        var sql = "SELECT " + when (yAxis) {
            XapiReportOptions.SCORE -> "AVG(StatementEntity.resultScoreScaled) AS yAxis, "
            XapiReportOptions.DURATION -> "SUM(StatementEntity.resultDuration) AS yAxis, "
            XapiReportOptions.AVG_DURATION -> "AVG(StatementEntity.resultDuration) AS yAxis, "
            XapiReportOptions.COUNT_ACTIVITIES -> "COUNT(*) AS yAxis, "
            else -> ""
        }
        sql += groupBy(xAxis) + "AS xAxis, "
        sql += groupBy(subGroup) + "AS subgroup "
        sql += "FROM StatementEntity "

        val objectsList = reportFilterList.filter { it.entityType == ReportFilter.CONTENT_FILTER }.map { it.entityUid }
        val whoFilterList = reportFilterList.filter { it.entityType == ReportFilter.PERSON_FILTER }.map { it.entityUid }
        val didFilterList = reportFilterList.filter { it.entityType == ReportFilter.VERB_FILTER }.map { it.entityUid }


        if (xAxis == XapiReportOptions.GENDER || subGroup == XapiReportOptions.GENDER) {
            sql += "LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid "
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
                whereList.add("StatementEntity.personUid IN (?) ")
                paramList.addAll(listOf<Any>(whoFilterList))
            }
            if (didFilterList.isNotEmpty()) {
                whereList.add("StatementEntity.verbUid IN (?) ")
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
        sql += "GROUP BY xAxis, subgroup"
        return QueryParts(sql, sqlList, paramList.toTypedArray())
    }

    private fun groupBy(value: Int): String {
        return when (value) {
            XapiReportOptions.DAY -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            XapiReportOptions.WEEK -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') "
            XapiReportOptions.MONTH -> "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            XapiReportOptions.CONTENT_ENTRY -> "StatementEntity.xObjectUid "
            //LOCATION -> "Location.title"
            XapiReportOptions.GENDER -> "Person.gender "
            else -> ""
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ReportWithFilters

        if (reportFilterList != other.reportFilterList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + reportFilterList.hashCode()
        return result
    }
}