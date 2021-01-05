package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.ACTIVITIES_RECORDED
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.AVERAGE_DURATION
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.AVERAGE_SESSION_PER_CONTENT
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_SESSIONS
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_STUDENTS_COMPLETED
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.PERCENT_STUDENTS_COMPLETED
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_DURATION

data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)

fun ReportSeries.toSql(report: Report, accountPersonUid: Long): QueryParts {

    val paramList = mutableListOf<Any>()

    var sql = "SELECT " + when (reportSeriesDataSet) {
        TOTAL_DURATION -> "SUM(StatementEntity.resultDuration) AS yAxis, "
        AVERAGE_DURATION -> "AVG(StatementEntity.resultDuration) AS yAxis, "
        NUMBER_SESSIONS -> "COUNT(DISTINCT StatementEntity.contextRegistration) As yAxis, "
        ACTIVITIES_RECORDED -> "COUNT(StatementEntity.statementId) AS yAxis, "
        AVERAGE_SESSION_PER_CONTENT -> "COUNT(DISTINCT StatementEntity.contextRegistration) As yAxis, "
        PERCENT_STUDENTS_COMPLETED -> """((CAST(COUNT(DISTINCT CASE WHEN 
            StatementEntity.resultCompletion THEN StatementEntity.statementPersonUid ELSE NULL END) 
            AS REAL) / COUNT(StatementEntity.resultCompletion)) * 100) as yAxis, """.trimMargin()
        NUMBER_STUDENTS_COMPLETED -> """COUNT(DISTINCT CASE WHEN StatementEntity.resultCompletion 
                THEN StatementEntity.statementPersonUid ELSE NULL END) AS yAxis, """.trimMargin()
        else -> ""
    }

    val personPermission = """${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} 
        ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
         LEFT JOIN StatementEntity ON StatementEntity.statementPersonUid = Person.personUid """.replace(":accountPersonUid","?")
    paramList.add(accountPersonUid)
    paramList.add(accountPersonUid)


    var sqlList = """SELECT  Person.* , XLangMapEntry.* ,StatementEntity.* 
                $personPermission 
                LEFT JOIN XLangMapEntry ON XLangMapEntry.statementLangMapUid = 
                (SELECT statementLangMapUid FROM XLangMapEntry 
                WHERE statementVerbUid = StatementEntity.statementVerbUid LIMIT 1)"""


    sql += groupBy(report.xAxis) + "AS xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , " + groupBy(reportSeriesSubGroup) + "AS subgroup "
    }

    sql += personPermission

    if(report.xAxis == Report.CLASS || reportSeriesSubGroup == Report.CLASS){
        sql += "LEFT JOIN ClazzMember ON StatementEntity.statementPersonUid = ClazzMember.clazzMemberPersonUid "
        sql += "LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid "
    }

    val where = "WHERE PersonGroupMember.groupMemberPersonUid = ? "
    sql += where
    sqlList += where
    paramList.add(accountPersonUid)

    if((report.toDate > 0 || report.fromDate > 0) || reportSeriesFilters?.isNotEmpty() == true){

        val whereList = mutableListOf<String>()
        reportSeriesFilters?.forEach { filter ->

            when(filter.reportFilterField){

                ReportFilter.FIELD_PERSON_AGE -> {

                    val age = filter.reportFilterValue?.toInt() ?: 1
                    val now = DateTime.now()
                    val dateTimeAge = now - age.years
                    when(filter.reportFilterCondition){

                        ReportFilter.CONDITION_GREATER_THAN ->{
                            whereList.add("Person.dateOfBirth >= ${dateTimeAge.dateDayStart.unixMillisLong} ")
                        }
                        ReportFilter.CONDITION_LESS_THAN ->{
                            whereList.add("Person.dateOfBirth <= ${dateTimeAge.dateDayStart.unixMillisLong} ")
                        }

                    }
                }
                ReportFilter.FIELD_PERSON_GENDER ->{

                    when(filter.reportFilterCondition){

                        ReportFilter.CONDITION_IS -> {

                            when(filter.reportFilterDropDownValue){
                                Person.GENDER_UNSET -> {
                                    whereList.add("Person.gender = ${Person.GENDER_UNSET} ")
                                }
                                Person.GENDER_MALE -> {
                                    whereList.add("Person.gender = ${Person.GENDER_MALE} ")
                                }
                                Person.GENDER_FEMALE -> {
                                    whereList.add("Person.gender = ${Person.GENDER_FEMALE} ")
                                }
                                Person.GENDER_OTHER ->{
                                    whereList.add("Person.gender = ${Person.GENDER_OTHER} ")
                                }
                            }
                        }
                        ReportFilter.CONDITION_IS_NOT ->{
                            when(filter.reportFilterDropDownValue){
                                Person.GENDER_UNSET -> {
                                    whereList.add("Person.gender != ${Person.GENDER_UNSET} ")
                                }
                                Person.GENDER_MALE -> {
                                    whereList.add("Person.gender != ${Person.GENDER_MALE} ")
                                }
                                Person.GENDER_FEMALE -> {
                                    whereList.add("Person.gender != ${Person.GENDER_FEMALE} ")
                                }
                                Person.GENDER_OTHER ->{
                                    whereList.add("Person.gender != ${Person.GENDER_OTHER} ")
                                }
                            }
                        }

                    }
                }
            }
        }
        if (report.toDate > 0L && report.fromDate > 0L) {
            whereList.add("(StatementEntity.timestamp >= ? AND StatementEntity.timestamp <= ?) ")
            paramList.add(report.fromDate)
            paramList.add(report.toDate)
        }
        val whereListStr = " AND " + whereList.joinToString(" AND ")
        sql += whereListStr
        sqlList += whereListStr

    }


    sql += " GROUP BY xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , subgroup "
    }

    sqlList += " GROUP BY StatementEntity.statementUid ORDER BY StatementEntity.timestamp DESC"


    return QueryParts(sql, sqlList, paramList.toTypedArray())
}

private fun groupBy(value: Int): String {
    return when (value) {
        Report.DAY -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
        Report.WEEK -> "strftime('%d %m %Y', StatementEntity.timestamp/1000, 'unixepoch', 'weekday 6', '-6 day') "
        Report.MONTH -> "strftime('%m %Y', StatementEntity.timestamp/1000, 'unixepoch') "
        Report.CONTENT_ENTRY -> "StatementEntity.statementContentEntryUid "
        Report.GENDER -> "Person.gender "
        Report.CLASS -> "Clazz.clazzUid "
        else -> ""
    }
}