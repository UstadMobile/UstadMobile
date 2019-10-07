package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.annotation.ParamName
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.lib.db.entities.XapiReportOptions
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@UmRepository
abstract class StatementDao : BaseDao<StatementEntity> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity")
    abstract fun all(): List<StatementEntity>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<ReportData>

    open suspend fun getResultsFromOptions(@ParamName("options") options: XapiReportOptions): List<ReportData> {
        val sql = options.toSql()
        return getResults(SimpleDoorQuery(sql.sqlStr, sql.queryParams))
    }

    open suspend fun getResultsListFromOptions(@ParamName("options") options: XapiReportOptions): List<ReportListData> {
        val sql = options.toSql()
        return getListResults(SimpleDoorQuery(sql.sqlListStr, sql.queryParams))
    }


    @RawQuery
    abstract fun getListResults(query: DoorQuery): List<ReportListData>


    @Serializable
    data class ReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")

    @Serializable
    data class ReportListData(var name: String? = "", var verb: String? = "", var result: Byte = 0.toByte(), var whenDate: Long = 0L)
}
