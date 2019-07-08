package com.ustadmobile.core.controller

data class XapiReportOptions(var chartType: Int, var yAxis: Int,
                             var xAxis: Int, var subGroup: Int,
                             var whoFilterList: List<Long> = mutableListOf(),
                             var didFilterList: List<Long> = mutableListOf(),
                             var objectsList: List<Long> = mutableListOf(),
                             var entriesList: List<Long> = mutableListOf(),
                             var toDate: Long = 0, var fromDate: Long = 0,
                             var locationsList: List<Long> = mutableListOf()) {


    data class QueryParts(val sqlStr: String, val queryParams: List<Any>)

    fun toSql(): QueryParts {
        if (xAxis == subGroup) {
            throw IllegalArgumentException("XAxis Selection and subGroup selection was the same")
        }
        val paramList = mutableListOf<Any>()
        var sql = "SELECT " + when (yAxis) {
            XapiReportOptionsPresenter.SCORE -> "AVG(StatementEntity.resultScoreScaled), "
            XapiReportOptionsPresenter.DURATION -> "SUM(StatementEntity.resultDuration), "
            XapiReportOptionsPresenter.COUNT_ACTIVITIES -> "COUNT(StatementEntity.*), "
            else -> ""
        }
        sql += groupBy(xAxis) + " AS xAxis, "
        sql += groupBy(subGroup) + "AS subgroup "
        sql += "FROM StatementEntity "
        if (xAxis == XapiReportOptionsPresenter.GENDER || subGroup == XapiReportOptionsPresenter.GENDER) {
            sql += "LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid "
        }
        sql += "WHERE "
        if (objectsList.isNotEmpty()) {
            sql += "(StatementEntity.xObjectUid IN (?) OR " +
                    "EXISTS(SELECT contextXObjectStatementJoinUid FROM ContextXObjectStatementJoin " +
                    "WHERE contextStatementUid = StatementEntity.statementUid AND contextXObjectUid IN (?)) "
            paramList.addAll(listOf<Any>(objectsList, objectsList))
        }

        if (whoFilterList.isNotEmpty()) {
            sql += "StatementEntity.personUid IN (?) "
            paramList.addAll(listOf<Any>(whoFilterList))
        }
        if (didFilterList.isNotEmpty()) {
            sql += "AND StatementEntity.verbUid IN (?) "
            paramList.addAll(listOf<Any>(didFilterList))
        }
        if (toDate < 0 && fromDate < 0) {
            sql += "AND (StatementEntity.timestamp > ? AND StatementEntity.timestamp < ?) "
            paramList.add(fromDate)
            paramList.add(toDate)
        }
        sql += "GROUP BY xAxis, subgroup"
        return QueryParts(sql, paramList.toList())
    }

    private fun groupBy(value: Int): String {
        return when (value) {
            XapiReportOptionsPresenter.DAY -> "strftime('%Y-%m-%d', StatementEntity.timestamp/1000, 'unixepoch')"
            XapiReportOptionsPresenter.WEEK -> "strftime('%Y-%m-%d', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day')"
            XapiReportOptionsPresenter.MONTH -> "strftime('%Y-%m', StatementEntity.timestamp/1000, 'unixepoch')"
            XapiReportOptionsPresenter.CONTENT_ENTRY -> "XObjectEntity.xObjectUid"
            //LOCATION -> "Location.title"
            XapiReportOptionsPresenter.GENDER -> "Person.gender"
            else -> ""
        }
    }
}