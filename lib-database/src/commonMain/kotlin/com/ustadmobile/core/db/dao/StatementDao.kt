package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.annotation.ParamName
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@UmRepository
abstract class StatementDao : BaseDao<StatementEntity> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    abstract fun getOneStatement(): DoorLiveData<StatementEntity?>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<ReportData>

    open suspend fun getResultsFromOptions(@ParamName("options") options: ReportWithFilters): List<ReportData> {
        val sql = options.toSql()
        return getResults(SimpleDoorQuery(sql.sqlStr, sql.queryParams))
    }

    open suspend fun getResultsListFromOptions(@ParamName("options") options: ReportWithFilters): DataSource.Factory<Int, StatementListReport> {
        val sql = options.toSql()
        return getListResults(SimpleDoorQuery(sql.sqlListStr, sql.queryParams))
    }

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    abstract fun getListResults(query: DoorQuery): DataSource.Factory<Int, StatementListReport>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("SELECT * FROM XLangMapEntry LIMIT 1")
    abstract fun getXLangMap(): XLangMapEntry?

    @Serializable
    data class ReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")

}
