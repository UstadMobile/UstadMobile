package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.SALES_TOTAL
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.NUMBER_OF_SALES
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.AVERAGE_SALE_TOTAL
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
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_ABSENCES
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_ATTENDANCE
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_CLASSES
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_DURATION
import com.ustadmobile.lib.db.entities.ReportSeries.Companion.TOTAL_LATES


data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)

fun ReportSeries.toSql(report: Report, accountPersonUid: Long, dbType: Int): QueryParts {

    val paramList = mutableListOf<Any>()

    var sql = "SELECT " + when (reportSeriesYAxis) {
        SALES_TOTAL -> """ COALESCE(
                            ( SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) -  SUM(Sale.saleDiscount) 
                            ) , 0 ) AS  yAxis, """.trimMargin()
        NUMBER_OF_SALES -> """COUNT(Sale.saleActive) AS yAxis, """
        AVERAGE_SALE_TOTAL -> """ 
                            COALESCE( (
			(SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - 
			SUM(Sale.saleDiscount) ) / COUNT(Sale.saleActive)
			) , 0 ) AS yAxis, """
        TOTAL_DURATION -> "SUM(StatementEntity.resultDuration) AS yAxis, "
        AVERAGE_DURATION -> """SUM(StatementEntity.resultDuration) / COUNT(DISTINCT 
            StatementEntity.contextRegistration) AS yAxis, """.trimMargin()
        NUMBER_SESSIONS -> "COUNT(DISTINCT StatementEntity.contextRegistration) As yAxis, "
        INTERACTIONS_RECORDED -> "COUNT(StatementEntity.statementId) AS yAxis, "
        NUMBER_ACTIVE_USERS -> """COUNT(DISTINCT StatementEntity.statementPersonUid) As yAxis, """
        AVERAGE_USAGE_TIME_PER_USER -> """SUM(StatementEntity.resultDuration) / COUNT(DISTINCT 
            StatementEntity.statementPersonUid) As yAxis, """.trimMargin()
        TOTAL_ATTENDANCE -> """COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        TOTAL_ABSENCES -> """COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ABSENT 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        TOTAL_LATES -> """COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_PARTIAL 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) As yAxis, """
        PERCENTAGE_STUDENTS_ATTENDED -> """((CAST(COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid)) * 100) as yAxis, """.trimMargin()
        PERCENTAGE_STUDENTS_ATTENDED_OR_LATE -> """((CAST(COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
            OR ClazzLogAttendanceRecord.attendanceStatus = $STATUS_PARTIAL 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid)) * 100) as yAxis, """.trimMargin()
        TOTAL_CLASSES -> """COUNT(DISTINCT ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid) As yAxis, """
        NUMBER_UNIQUE_STUDENTS_ATTENDING -> """COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED THEN
            StatementEntity.statementPersonUid ELSE NULL END) As yAxis, """.trimMargin()
        NUMBER_OF_STUDENTS_COMPLETED_CONTENT -> """COUNT(DISTINCT CASE WHEN (StatementEntity.resultCompletion 
            AND StatementEntity.contentEntryRoot AND StatementEntity.statementVerbUid = ${VerbEntity.VERB_COMPLETED_UID})
            THEN StatementEntity.statementPersonUid ELSE NULL END) as yAxis, """.trimMargin()
        PERCENT_OF_STUDENTS_COMPLETED_CONTENT -> """((CAST(COUNT(DISTINCT CASE WHEN 
            (StatementEntity.resultCompletion AND StatementEntity.contentEntryRoot 
            AND StatementEntity.statementVerbUid = ${VerbEntity.VERB_COMPLETED_UID})
            THEN StatementEntity.statementPersonUid ELSE NULL END) 
            AS REAL) / COUNT(DISTINCT StatementEntity.statementPersonUid)) * 100) as yAxis, """
        else -> ""
    }

//    val personPermission = """${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1}
//        ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
//         LEFT JOIN StatementEntity ON StatementEntity.statementPersonUid = Person.personUid """.replace(":accountPersonUid","?")
//    paramList.add(accountPersonUid)
//    paramList.add(accountPersonUid)

    var personPermission = """ 
        FROM Sale  
                    LEFT JOIN Person AS Customer ON Customer.personUid = Sale.saleCustomerUid
                    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = Sale.saleUid 
                        AND CAST(SaleItem.saleItemActive AS INTEGER) = 1
                    LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid
                    LEFT JOIN Person as LE ON LE.personUid = :leUid
                    LEFT JOIN Person as SaleLE ON SaleLE.personUid = Sale.salePersonUid
                    LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid 
                    
                
    """.replace(":leUid","?")
    paramList.add(accountPersonUid)


    var sqlList = """SELECT  Person.* , Sale.* 
                $personPermission 
                LEFT JOIN XLangMapEntry ON XLangMapEntry.statementLangMapUid = 
                (SELECT statementLangMapUid FROM XLangMapEntry 
                WHERE statementVerbUid = StatementEntity.statementVerbUid LIMIT 1) """


    sql += groupBy(report.xAxis, dbType) + "AS xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , " + groupBy(reportSeriesSubGroup, dbType) + "AS subgroup "
    }

    sql += personPermission

    if(report.xAxis == Report.CLASS || reportSeriesSubGroup == Report.CLASS){
        sql += "LEFT JOIN ClazzMember ON StatementEntity.statementPersonUid = ClazzMember.clazzMemberPersonUid "
        sql += "LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid "
    }

    when(reportSeriesYAxis){
        TOTAL_ATTENDANCE, TOTAL_ABSENCES, TOTAL_LATES, TOTAL_CLASSES,
        PERCENTAGE_STUDENTS_ATTENDED, PERCENTAGE_STUDENTS_ATTENDED_OR_LATE,
        NUMBER_UNIQUE_STUDENTS_ATTENDING -> {
            if(report.xAxis != Report.CLASS && reportSeriesSubGroup != Report.CLASS){
                sql += "LEFT JOIN ClazzMember ON StatementEntity.statementPersonUid = ClazzMember.clazzMemberPersonUid "
            }
            sql += "LEFT JOIN ClazzLogAttendanceRecord ON ClazzMember.clazzMemberUid  = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid "
        }
    }

//    when(reportSeriesYAxis){
//        SALES_TOTAL, NUMBER_OF_SALES, AVERAGE_SALE_TOTAL -> {
//            sql+= """ LEFT JOIN SaleItem ON    SaleItem.saleItemSaleUid = Sale.saleUid
//                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1      """
//        }
//    }

//    val where = " WHERE PersonGroupMember.groupMemberPersonUid = ? "
//    sql += where
//    sqlList += where
//    paramList.add(accountPersonUid)

    val where = """ WHERE CAST(Sale.saleActive AS INTEGER) = 1
                    AND (
                        CAST(LE.admin AS INTEGER) = 1 OR 
                        Sale.salePersonUid = LE.personUid
                        )
                """
    sql += where
    sqlList += where

    if(report.reportDateRangeSelection != 0 || reportSeriesFilters?.isNotEmpty() == true){

        val whereList = mutableListOf<String>()
        reportSeriesFilters?.forEach { filter ->

            when(filter.reportFilterField){

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
                    when(filter.reportFilterCondition){
                        ReportFilter.CONDITION_GREATER_THAN -> filterString += "${dateTimeAgeNow.dateDayStart.unixMillisLong} "
                        ReportFilter.CONDITION_LESS_THAN -> filterString += "${dateTimeAgeNow.dateDayStart.unixMillisLong} "
                        ReportFilter.CONDITION_BETWEEN -> {
                            filterString += """ ${dateTimeAgeX.dateDayStart.unixMillisLong} 
                                AND ${dateTimeAgeY.dateDayStart.unixMillisDouble} """
                        }
                    }
                    whereList.add(filterString)
                }
                ReportFilter.FIELD_PERSON_GENDER ->{

                    var filterString = "Person.gender "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "${filter.reportFilterDropDownValue} "
                    whereList += (filterString)
                }
                ReportFilter.FIELD_CONTENT_COMPLETION ->{

                    var filterString = "(StatementEntity.contentEntryRoot AND StatementEntity.resultCompletion "
                    filterString += when(filter.reportFilterDropDownValue){
                        ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED -> ")"
                        ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_PASSED -> "AND StatementEntity.resultSuccess ${handleCondition(filter.reportFilterCondition)} ${StatementEntity.RESULT_SUCCESS}) "
                        ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_FAILED -> "AND StatementEntity.resultSuccess ${handleCondition(filter.reportFilterCondition)} ${StatementEntity.RESULT_FAILURE}) "
                        else -> ""
                    }
                    whereList += (filterString)

                }
                ReportFilter.FIELD_CONTENT_ENTRY ->{

                    var filterString = "StatementEntity.statementContentEntryUid "
                    filterString += handleCondition(filter.reportFilterCondition)
                    filterString += "(${filter.reportFilterValue}) "
                    whereList += (filterString)

                }
                ReportFilter.FIELD_ATTENDANCE_PERCENTAGE ->{

                    var filterString = """(SELECT ((CAST(COUNT(DISTINCT CASE WHEN 
            ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
            THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) 
            AS REAL) / COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid)) * 100) as attendance FROM ClazzLogAttendanceRecord) """
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


    sql += " GROUP BY xAxis "
    if (reportSeriesSubGroup != 0) {
        sql += " , subgroup "
    }

    sqlList += " GROUP BY StatementEntity.statementUid ORDER BY StatementEntity.timestamp DESC"


//    return QueryParts(sql, sqlList, paramList.toTypedArray())
    return QueryParts(sql, "", paramList.toTypedArray())
}

private fun handleCondition(conditionOption: Int): String{
    return when(conditionOption){
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


private fun groupBy(value: Int, dbType: Int): String {
    return when (value) {
        Report.DAY -> {
            when (dbType) {
                DoorDbType.SQLITE -> {
                    "strftime('%d %m %Y', Sale.saleCreationDate/1000, 'unixepoch') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(TO_TIMESTAMP(Sale.saleCreationDate/1000), 'DD MM YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.WEEK -> {
            when (dbType) {
                DoorDbType.SQLITE -> {
                    // -5 days to get the date on monday
                    "strftime('%d %m %Y', Sale.saleCreationDate/1000, 'unixepoch', 'weekday 6', '-5 day') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(DATE(DATE_TRUNC('week', TO_TIMESTAMP(Sale.saleCreationDate/1000))), 'DD MM YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.MONTH -> {
            when (dbType) {
                DoorDbType.SQLITE -> {
                    "strftime('%m %Y', Sale.saleCreationDate/1000, 'unixepoch') "
                }
                DoorDbType.POSTGRES -> {
                    "TO_CHAR(TO_TIMESTAMP(Sale.saleCreationDate/1000), 'MM YYYY') "
                }
                else -> {
                    ""
                }
            }
        }
        Report.CONTENT_ENTRY -> "StatementEntity.statementContentEntryUid "
        Report.GENDER -> " SaleLE.gender "
        Report.CLASS -> "Clazz.clazzUid "
        Report.LE -> " SaleLE.personUid "
        Report.PRODUCT_CATEGORY -> " Product.productUid "
        Report.PRODUCT -> " Product.productUid "
        Report.CUSTOMER -> " Customer.personUid "
        Report.PROVINCE -> " Sale.saleLocationUid "
        else -> ""
    }
}