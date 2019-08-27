package com.ustadmobile.core.controller

import org.junit.Assert
import org.junit.Test

class XapiReportOptionsTest {

    @Test
    fun testSqlWithAVGScoreWithDayAsXaxisAndWeekAsSubGroup() {

        var report = XapiReportOptions(0, XapiReportOptions.SCORE,
                XapiReportOptions.DAY, XapiReportOptions.WEEK)

        Assert.assertEquals("SELECT AVG(StatementEntity.resultScoreScaled) AS yAxis, " +
                "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') " +
                "AS xAxis, strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') AS subgroup " +
                "FROM StatementEntity GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }


    @Test
    fun testSqlWithSUMDurationWithMonthAsXaxisAndContentEntryAsSubGroup() {

        var report = XapiReportOptions(0, XapiReportOptions.DURATION,
                XapiReportOptions.MONTH, XapiReportOptions.CONTENT_ENTRY)

        Assert.assertEquals("SELECT SUM(StatementEntity.resultDuration) AS yAxis, " +
                "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }

    @Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroup() {

        var report = XapiReportOptions(0, XapiReportOptions.COUNT_ACTIVITIES,
                XapiReportOptions.GENDER, XapiReportOptions.CONTENT_ENTRY)

        Assert.assertEquals("SELECT COUNT(*) AS yAxis, " +
                "Person.gender " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }

    @Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroupWithWhoListAndVerbList() {

        var report = XapiReportOptions(0, XapiReportOptions.COUNT_ACTIVITIES,
                XapiReportOptions.GENDER, XapiReportOptions.CONTENT_ENTRY, mutableListOf(1,2), mutableListOf(1,2))

        Assert.assertEquals("SELECT COUNT(*) AS yAxis, " +
                "Person.gender " +
                "AS xAxis, StatementEntity.xObjectUid AS subgroup " +
                "FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid " +
                "WHERE StatementEntity.personUid IN (?) AND StatementEntity.verbUid IN (?) GROUP BY xAxis, subgroup", report.toSql().sqlStr)

    }

    @Test
    fun testSqlWithCOUNTAcitivitesWithGenderAsXaxisAndContentEntryAsSubGroupAndAllFilters() {

        var report = XapiReportOptions(0, XapiReportOptions.COUNT_ACTIVITIES,
                XapiReportOptions.GENDER, XapiReportOptions.CONTENT_ENTRY,
                mutableListOf(1,2), mutableListOf(1,2), mutableListOf(1,2), mutableListOf(1,3),
                1L, 1L, mutableListOf(1,3))

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