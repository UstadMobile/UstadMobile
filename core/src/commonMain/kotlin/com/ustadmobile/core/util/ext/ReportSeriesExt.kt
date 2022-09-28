package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.ATTENDANCE_QUERY
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.INTERACTIONS_RECORDED
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.AVERAGE_DURATION
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_SESSIONS
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.AVERAGE_USAGE_TIME_PER_USER
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_ACTIVE_USERS
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_OF_STUDENTS_COMPLETED_CONTENT
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_UNIQUE_STUDENTS_ATTENDING
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.PERCENTAGE_STUDENTS_ATTENDED
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.PERCENTAGE_STUDENTS_ATTENDED_OR_LATE
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.PERCENT_OF_STUDENTS_COMPLETED_CONTENT
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.STATEMENT_QUERY
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_ABSENCES
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_ATTENDANCE
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_CLASSES
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_DURATION
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_LATES

data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)

fun ReportSeries.toSql(report: Report, accountPersonUid: Long, dbType: Int): QueryParts {

    val paramList = mutableListOf<Any>()

    var sql = "SELECT " + when (reportSeriesYAxis) {
        TOTAL_DURATION -> "SUM(ResultSource.resultDuration) AS yAxis, "
        AVERAGE_DURATION -> """SUM(ResultSource.resultDuration) / COUNT(DISTINCT 
            ResultSource.contextRegistration) AS yAxis, """.trimMargin()
        NUMBER_SESSIONS -> "COUNT(DISTINCT ResultSource.contextRegistration) As yAxis, "
        INTERACTIONS_RECORDED -> "COUNT(ResultSource.statementId) AS yAxis, "
        NUMBER_ACTIVE_USERS -> """COUNT(DISTINCT ResultSource.statementPersonUid) As yAxis, """
        AVERAGE_USAGE_TIME_PER_USER -> """SUM(ResultSource.resultDuration) / COUNT(DISTINCT 
            ResultSource.statementPersonUid) As yAxis, """.trimMargin()
        TOTAL_ATTENDANCE -> """COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_ATTENDED 
            THEN ResultSource.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        TOTAL_ABSENCES -> """COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_ABSENT 
            THEN ResultSource.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        TOTAL_LATES -> """COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_PARTIAL 
            THEN ResultSource.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        PERCENTAGE_STUDENTS_ATTENDED -> """((CAST(COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_ATTENDED 
            THEN ResultSource.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / MAX(COUNT(DISTINCT ResultSource.clazzLogAttendanceRecordUid),1)) * 100)  as yAxis, """.trimMargin()
        PERCENTAGE_STUDENTS_ATTENDED_OR_LATE -> """((CAST(COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_ATTENDED 
            OR ResultSource.attendanceStatus = $STATUS_PARTIAL 
            THEN ResultSource.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / MAX(COUNT(DISTINCT ResultSource.clazzLogAttendanceRecordUid),1)) * 100) as yAxis, """.trimMargin()
        TOTAL_CLASSES -> """COUNT(DISTINCT ResultSource.clazzLogAttendanceRecordClazzLogUid) As yAxis, """
        NUMBER_UNIQUE_STUDENTS_ATTENDING -> """COUNT(DISTINCT CASE WHEN 
            ResultSource.attendanceStatus = $STATUS_ATTENDED THEN
            ResultSource.clazzLogAttendanceRecordPersonUid ELSE NULL END) As yAxis, """.trimMargin()
        NUMBER_OF_STUDENTS_COMPLETED_CONTENT -> """COUNT(DISTINCT CASE WHEN (ResultSource.resultCompletion 
            AND ResultSource.contentEntryRoot AND ResultSource.statementVerbUid = ${VerbEntity.VERB_COMPLETED_UID})
            THEN ResultSource.statementPersonUid ELSE NULL END) as yAxis, """.trimMargin()
        PERCENT_OF_STUDENTS_COMPLETED_CONTENT -> """((CAST(COUNT(DISTINCT CASE WHEN 
            (ResultSource.resultCompletion AND ResultSource.contentEntryRoot 
            AND ResultSource.statementVerbUid = ${VerbEntity.VERB_COMPLETED_UID})
            THEN ResultSource.statementPersonUid ELSE NULL END) 
            AS REAL) / MAX(COUNT(DISTINCT ResultSource.statementPersonUid),1)) * 100) as yAxis, """
        else -> ""
    }



    var queryType = 0
    when (reportSeriesYAxis) {
        TOTAL_ATTENDANCE, TOTAL_ABSENCES, TOTAL_LATES, TOTAL_CLASSES, PERCENTAGE_STUDENTS_ATTENDED,
        PERCENTAGE_STUDENTS_ATTENDED_OR_LATE, NUMBER_UNIQUE_STUDENTS_ATTENDING -> {
            queryType = ATTENDANCE_QUERY
        }
        TOTAL_DURATION, AVERAGE_DURATION, INTERACTIONS_RECORDED, NUMBER_ACTIVE_USERS,
        AVERAGE_USAGE_TIME_PER_USER, NUMBER_OF_STUDENTS_COMPLETED_CONTENT,
        PERCENT_OF_STUDENTS_COMPLETED_CONTENT, NUMBER_SESSIONS -> {
            queryType = STATEMENT_QUERY
        }
    }

    sql += groupBy(report.xAxis, queryType, dbType) + "AS xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , " + groupBy(reportSeriesSubGroup, queryType, dbType) + "AS subgroup "
    }else {
        sql += ", '' AS subgroup "
    }

    sql += "FROM (SELECT "

    val filterFieldList = reportSeriesFilters?.map { it.reportFilterField }

    val hasFilterEnrolment = filterFieldList?.any {
        it == ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME ||
                it == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON
    } ?: false

    val hasFilterStatement = filterFieldList?.any {
        it == ReportFilter.FIELD_CONTENT_PROGRESS || it == ReportFilter.FIELD_CONTENT_ENTRY ||
                it == ReportFilter.FIELD_CONTENT_COMPLETION
    } ?: false

    val addEnrolmentJoin = report.xAxis == Report.ENROLMENT_OUTCOME || reportSeriesSubGroup == Report.ENROLMENT_OUTCOME
            || report.xAxis == Report.ENROLMENT_LEAVING_REASON || reportSeriesSubGroup == Report.ENROLMENT_LEAVING_REASON
            || report.xAxis == Report.CLASS || reportSeriesSubGroup == Report.CLASS || hasFilterEnrolment

    val addClassJoin = report.xAxis == Report.CLASS || reportSeriesSubGroup == Report.CLASS

    val addEntryJoin =  hasFilterStatement || report.xAxis == Report.CONTENT_ENTRY ||
            reportSeriesSubGroup == Report.CONTENT_ENTRY

    if(addEnrolmentJoin){
        sql += "ClazzEnrolment.clazzEnrolmentOutcome, ClazzEnrolment.clazzEnrolmentLeavingReasonUid, "
        if(addClassJoin){
            sql += "Clazz.clazzUid, "
        }
    }

    if(addEntryJoin && queryType != STATEMENT_QUERY){
        sql += "StatementEntity.*, "
    }

    when(queryType){
        ATTENDANCE_QUERY -> {
            sql += """
               ClazzLogAttendanceRecord.*, ClazzLog.logDate , Person.* 
            """.trimIndent()
        }
        STATEMENT_QUERY -> {
            sql += """
                StatementEntity.* , Person.* 
            """.trimMargin()
        }
    }


    val personPermission = """
        FROM PersonGroupMember
            ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} 
                ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
         """

    var sqlList = """SELECT  Person.* , XLangMapEntry.* ,StatementEntity.* 
                $personPermission LEFT JOIN StatementEntity ON 
                StatementEntity.statementPersonUid = Person.personUid 
                LEFT JOIN XLangMapEntry ON XLangMapEntry.statementLangMapUid = 
                (SELECT statementLangMapUid FROM XLangMapEntry 
                WHERE statementVerbUid = StatementEntity.statementVerbUid LIMIT 1) """

    sql += personPermission


    if(queryType == ATTENDANCE_QUERY){
        sql += """LEFT JOIN ClazzLogAttendanceRecord ON
            Person.personUid  = ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid 
            LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid """.trimMargin()
    }

    if (addEntryJoin || queryType == STATEMENT_QUERY) {
        sql += """LEFT JOIN StatementEntity ON Person.personUid = StatementEntity.statementPersonUid """
    }

    if (addEnrolmentJoin) {

        val joinEnrolment = """LEFT JOIN ClazzEnrolment ON 
                    Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid """.trimMargin()
        sql += joinEnrolment
        if (hasFilterEnrolment) {
            sqlList += joinEnrolment
        }

        if (addClassJoin) {

            sql += if (queryType == ATTENDANCE_QUERY) {
                "LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid "
            } else {
                "LEFT JOIN Clazz ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid "
            }
        }
    }


    val where = " WHERE PersonGroupMember.groupMemberPersonUid = ? "
    sql += where
    sqlList += where
    paramList.add(accountPersonUid)

    if (report.reportDateRangeSelection != 0 || reportSeriesFilters?.isNotEmpty() == true) {

        val whereList = mutableListOf<String>()
        reportSeriesFilters?.forEach { filter ->

            when (filter.reportFilterField) {

                ReportFilter.FIELD_PERSON_AGE -> {

                    var filterString = "Person.dateOfBirth "
                    val age = filter.reportFilterValue?.toInt() ?: 13
                    val betweenAgeX = filter.reportFilterValueBetweenX?.toInt() ?: 13
                    val betweenAgeY = filter.reportFilterValueBetweenY?.toInt() ?: 18
                    val now = DateTime.now()
                    val dateTimeAgeNow = now - age.years
                    val dateTimeAgeX = now - betweenAgeX.years
                    val dateTimeAgeY = now - betweenAgeY.years
                    filterString += handleCondition(filter.reportFilterCondition)
                    when (filter.reportFilterCondition) {
                        ReportFilter.CONDITION_GREATER_THAN -> filterString += "${dateTimeAgeNow.dateDayStart.unixMillisLong} "
                        ReportFilter.CONDITION_LESS_THAN -> filterString += "${dateTimeAgeNow.dateDayStart.unixMillisLong} "
                        ReportFilter.CONDITION_BETWEEN -> {
                            filterString += """ ${dateTimeAgeX.dateDayStart.unixMillisLong} 
                                AND ${dateTimeAgeY.dateDayStart.unixMillisDouble} """
                        }
                    }
                    whereList.add(filterString)
                }
                ReportFilter.FIELD_PERSON_GENDER -> {

                    var filterString = "Person.gender "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "${filter.reportFilterDropDownValue} "
                    whereList += (filterString)
                }
                ReportFilter.FIELD_CONTENT_COMPLETION -> {

                    var filterString = "(StatementEntity.contentEntryRoot AND StatementEntity.resultCompletion "
                    filterString += when (filter.reportFilterDropDownValue) {
                        StatementEntity.CONTENT_COMPLETE -> ")"
                        StatementEntity.CONTENT_PASSED -> "AND StatementEntity.resultSuccess ${handleCondition(filter.reportFilterCondition)} ${StatementEntity.RESULT_SUCCESS}) "
                        StatementEntity.CONTENT_FAILED -> "AND StatementEntity.resultSuccess ${handleCondition(filter.reportFilterCondition)} ${StatementEntity.RESULT_FAILURE}) "
                        else -> ""
                    }
                    whereList += (filterString)

                }
                ReportFilter.FIELD_CONTENT_ENTRY -> {

                    var filterString = "StatementEntity.statementContentEntryUid "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "(${filter.reportFilterValue}) "
                    whereList += (filterString)

                }
                ReportFilter.FIELD_ATTENDANCE_PERCENTAGE -> {

                    var filterString = """(SELECT ((CAST(COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / MAX(COUNT(DISTINCT ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid),1)) * 100) as attendance FROM ClazzLogAttendanceRecord) """
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += """ ${filter.reportFilterValueBetweenX} 
                        AND ${filter.reportFilterValueBetweenY} """
                    whereList += (filterString)

                }
                ReportFilter.FIELD_CONTENT_PROGRESS -> {

                    var filterString = "StatementEntity.extensionProgress "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += """ ${filter.reportFilterValueBetweenX} 
                        AND ${filter.reportFilterValueBetweenY} """
                    whereList += (filterString)

                }
                ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON -> {

                    var filterString = "ClazzEnrolment.clazzEnrolmentLeavingReasonUid "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "(${filter.reportFilterValue}) "
                    whereList += (filterString)

                }
                ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME -> {

                    var filterString = "ClazzEnrolment.clazzEnrolmentOutcome "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "${filter.reportFilterDropDownValue} "
                    whereList += (filterString)

                }
            }
        }
        if (report.reportDateRangeSelection != 0) {

            val dateRangeMoment = report.toDateRangeMoment().toFixedDatePair()

            whereList.add("(StatementEntity.timestamp >= ? AND StatementEntity.timestamp <= ?) ")
            paramList.add(dateRangeMoment.first)
            paramList.add(dateRangeMoment.second)
        }
        val whereListStr = " AND " + whereList.joinToString(" AND ")
        sql += whereListStr
        sqlList += whereListStr

    }

    // Start of resultSource group by
    when(queryType){
        ATTENDANCE_QUERY -> {
            sql += """
                GROUP BY ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid, 
                         ClazzLog.clazzLogUid, Person.personUid 
            """.trimIndent()
        }
        STATEMENT_QUERY -> {
            sql += """
                GROUP BY StatementEntity.statementUid, Person.personUid 
            """.trimMargin()
        }
    }
    if(addEnrolmentJoin){
        sql += ",ClazzEnrolment.clazzEnrolmentUid "
        if(addClassJoin){
            sql += ",Clazz.clazzUid "
        }
    }
    if (addEntryJoin || queryType == STATEMENT_QUERY) {
        sql += ", StatementEntity.statementUid "
    }
    // END of resultSource group by

    sql += ") AS ResultSource "


    sql += " GROUP BY xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , subgroup "
    }

    sqlList += " GROUP BY StatementEntity.statementUid ORDER BY StatementEntity.timestamp DESC"


    return QueryParts(sql, sqlList, paramList.toTypedArray())
}

private fun handleCondition(conditionOption: Int): String {
    return when (conditionOption) {
        ReportFilter.CONDITION_IN_LIST -> "IN "
        ReportFilter.CONDITION_NOT_IN_LIST -> "NOT IN "
        ReportFilter.CONDITION_IS -> "= "
        ReportFilter.CONDITION_IS_NOT -> "!= "
        ReportFilter.CONDITION_GREATER_THAN -> ">= "
        ReportFilter.CONDITION_LESS_THAN -> "<= "
        ReportFilter.CONDITION_BETWEEN -> "BETWEEN "
        else -> ""
    }
}


private fun groupBy(value: Int, queryType: Int, dbType: Int): String {
    return when (value) {
        Report.DAY -> {

            val field = when(queryType){
                STATEMENT_QUERY -> {
                    "ResultSource.timestamp"
                }
                ATTENDANCE_QUERY -> {
                    "ResultSource.logDate"
                }
                else ->{
                    ""
                }
            }
            when (dbType) {
                DoorDbType.SQLITE -> {
                    "strftime('%d/%m/%Y', $field/1000, 'unixepoch') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(TO_TIMESTAMP($field/1000), 'DD/MM/YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.WEEK -> {

            val field = when(queryType){
                STATEMENT_QUERY -> {
                    "ResultSource.timestamp"
                }
                ATTENDANCE_QUERY -> {
                    "ResultSource.logDate"
                }
                else ->{
                    ""
                }
            }

            when (dbType) {
                DoorDbType.SQLITE -> {
                    // -5 days to get the date on monday
                    "strftime('%d/%m/%Y', $field/1000, 'unixepoch', 'weekday 6', '-5 day') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(DATE(DATE_TRUNC('week', TO_TIMESTAMP($field/1000))), 'DD/MM/YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.MONTH -> {

            val field = when(queryType){
                STATEMENT_QUERY -> {
                    "ResultSource.timestamp"
                }
                ATTENDANCE_QUERY -> {
                    "ResultSource.logDate"
                }
                else ->{
                    ""
                }
            }

            when (dbType) {
                DoorDbType.SQLITE -> {
                    "strftime('%m/%Y', $field/1000, 'unixepoch') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(TO_TIMESTAMP($field/1000), 'MM/YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.CONTENT_ENTRY -> "ResultSource.statementContentEntryUid "
        Report.GENDER -> "ResultSource.gender "
        Report.CLASS -> "ResultSource.clazzUid "
        Report.ENROLMENT_OUTCOME -> "ResultSource.clazzEnrolmentOutcome "
        Report.ENROLMENT_LEAVING_REASON -> "ResultSource.clazzEnrolmentLeavingReasonUid "
        else -> ""
    }
}