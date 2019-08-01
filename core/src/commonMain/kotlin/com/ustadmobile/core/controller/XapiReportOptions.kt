package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import kotlinx.serialization.Serializable

@Serializable
data class XapiReportOptions(var chartType: Int = BAR_CHART, var yAxis: Int = SCORE,
                             var xAxis: Int = DAY, var subGroup: Int = DAY,
                             var whoFilterList: List<Long> = mutableListOf(),
                             var didFilterList: List<Long> = mutableListOf(),
                             var objectsList: List<Long> = mutableListOf(),
                             var entriesList: List<Long> = mutableListOf(),
                             var fromDate: Long = 0L, var toDate: Long = 0L,
                             var locationsList: List<Long> = mutableListOf(),
                             var reportTitle: String = "") {


    data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)

    fun toSql(): QueryParts {
        if (xAxis == subGroup) {
            throw IllegalArgumentException("XAxis Selection and subGroup selection was the same")
        }
        val paramList = mutableListOf<Any>()

        var sqlList = "SELECT (Person.firstNames || ' ' || Person.lastName) AS name, " +
                "XLangMapEntry.valueLangMap AS verb, " +
                "StatementEntity.resultSuccess AS result, " +
                "StatementEntity.timestamp As whenDate " +
                "FROM StatementEntity " +
                "LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid " +
                "LEFT JOIN XLangMapEntry ON StatementEntity.verbUid = XLangMapEntry.verbLangMapUid "

        var sql = "SELECT " + when (yAxis) {
            SCORE -> "AVG(StatementEntity.resultScoreScaled) AS yAxis, "
            DURATION -> "SUM(StatementEntity.resultDuration) AS yAxis, "
            AVG_DURATION -> "AVG(StatementEntity.resultDuration) AS yAxis, "
            COUNT_ACTIVITIES -> "COUNT(*) AS yAxis, "
            else -> ""
        }
        sql += groupBy(xAxis) + "AS xAxis, "
        sql += groupBy(subGroup) + "AS subgroup "
        sql += "FROM StatementEntity "



        if (xAxis == GENDER || subGroup == GENDER) {
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
            DAY -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            WEEK -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') "
            MONTH -> "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
            CONTENT_ENTRY -> "StatementEntity.xObjectUid "
            //LOCATION -> "Location.title"
            GENDER -> "Person.gender "
            else -> ""
        }
    }

    companion object {

        const val BAR_CHART = MessageID.bar_chart

        const val LINE_GRAPH = MessageID.line_graph

        val listOfGraphs = arrayOf(BAR_CHART, LINE_GRAPH)

        const val SCORE = MessageID.score

        const val DURATION = MessageID.total_duration

        const val AVG_DURATION = MessageID.average_duration

        const val COUNT_ACTIVITIES = MessageID.count_activity

        val yAxisList = arrayOf(SCORE, DURATION, AVG_DURATION, COUNT_ACTIVITIES)

        const val DAY = MessageID.xapi_day

        const val WEEK = MessageID.xapi_week

        const val MONTH = MessageID.xapi_month

        const val CONTENT_ENTRY = MessageID.xapi_content_entry

        //TODO to be put back when varuna merges his branch
        // private const val LOCATION = MessageID.xapi_location

        const val GENDER = MessageID.xapi_gender

        val xAxisList = arrayOf(DAY, WEEK, MONTH, CONTENT_ENTRY, /*LOCATION, */ GENDER)

    }

}