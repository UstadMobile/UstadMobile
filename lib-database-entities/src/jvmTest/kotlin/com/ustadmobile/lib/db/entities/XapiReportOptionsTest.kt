package com.ustadmobile.lib.db.entities

import org.junit.Assert
import org.junit.Test

//This test is broken and it will be obsolete with the updated reporting screen
class XapiReportOptionsTest {

    //@Test
    fun testSqlWithAVGScoreWithDayAsXaxisAndWeekAsSubGroup() {

        val report = Report().apply {
            chartType = 0
            yAxis = Report.SCORE
            xAxis = Report.DAY
            subGroup = Report.WEEK
        }


        val reportFilter = ReportWithFilters(report, listOf())

        Assert.assertEquals("SELECT AVG(StatementEntity.resultScoreScaled) AS yAxis, " +
                "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') " +
                "AS xAxis, strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') AS subgroup " +
                "FROM StatementEntity GROUP BY xAxis, subgroup", reportFilter.toSql().sqlStr)

    }


    //@Test
    fun testSqlWithSUMDurationWithMonthAsXaxisAndContentEntryAsSubGroup() {

        val report = Report().apply {
            chartType = 0
            yAxis = Report.DURATION
            xAxis = Report.MONTH
            subGroup = Report.CONTENT_ENTRY
        }


        val reportFilter = ReportWithFilters(report, listOf())

        Assert.assertEquals("SELECT SUM(StatementEntity.resultDuration) AS yAxis, " +
                "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity GROUP BY xAxis, subgroup", reportFilter.toSql().sqlStr)

    }

    //@Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroup() {


        val report = Report().apply {
            chartType = 0
            yAxis = Report.COUNT_ACTIVITIES
            xAxis = Report.GENDER
            subGroup = Report.CONTENT_ENTRY
        }


        val reportFilter = ReportWithFilters(report, listOf())

        Assert.assertEquals("SELECT COUNT(*) AS yAxis, " +
                "Person.gender " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid GROUP BY xAxis, subgroup", reportFilter.toSql().sqlStr)

    }

    //@Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroupWithWhoListAndVerbList() {


        val report = ReportWithFilters().apply {
            chartType = 0
            yAxis = Report.COUNT_ACTIVITIES
            xAxis = Report.GENDER
            subGroup = Report.CONTENT_ENTRY
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityType = ReportFilter.PERSON_FILTER
                        entityUid = 1
                    }, ReportFilter().apply {
                entityType = ReportFilter.PERSON_FILTER
                entityUid = 2
            }, ReportFilter().apply {
                entityType = ReportFilter.VERB_FILTER
                entityUid = 1
            }, ReportFilter().apply {
                entityType = ReportFilter.VERB_FILTER
                entityUid = 2
            })
        }

        Assert.assertEquals("SELECT COUNT(*) AS yAxis, " +
                "Person.gender " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid " +
                "WHERE StatementEntity.personUid IN (?) AND StatementEntity.verbUid IN (?) GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }

    //@Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroupAndAllFilters() {


        val report = ReportWithFilters().apply {
            chartType = 0
            yAxis = Report.COUNT_ACTIVITIES
            xAxis = Report.GENDER
            subGroup = Report.CONTENT_ENTRY
            toDate = 1L
            fromDate = 1L
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityType = ReportFilter.PERSON_FILTER
                        entityUid = 1
                    }, ReportFilter().apply {
                entityType = ReportFilter.PERSON_FILTER
                entityUid = 2
            }, ReportFilter().apply {
                entityType = ReportFilter.VERB_FILTER
                entityUid = 1
            }, ReportFilter().apply {
                entityType = ReportFilter.VERB_FILTER
                entityUid = 2
            }, ReportFilter().apply {
                entityType = ReportFilter.CONTENT_FILTER
                entityUid = 1
            }, ReportFilter().apply {
                entityType = ReportFilter.CONTENT_FILTER
                entityUid = 3
            })
        }

        Assert.assertEquals("SELECT COUNT(*) AS yAxis, " +
                "Person.gender " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid " +
                "WHERE (StatementEntity.xObjectUid IN (?) OR EXISTS(SELECT contextXObjectStatementJoinUid FROM ContextXObjectStatementJoin " +
                "WHERE contextStatementUid = StatementEntity.statementUid AND contextXObjectUid IN (?))) AND " +
                "StatementEntity.personUid IN (?) AND StatementEntity.verbUid IN (?) AND " +
                "(StatementEntity.timestamp >= ? AND StatementEntity.timestamp <= ?) " +
                "GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }


}