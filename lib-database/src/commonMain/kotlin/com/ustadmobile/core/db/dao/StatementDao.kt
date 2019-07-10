package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.StatementEntity

@Dao
@UmRepository
abstract class StatementDao : BaseDao<StatementEntity> {

    @Query("SELECT * From StatementEntity")
    abstract fun all(): List<StatementEntity>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @Query("SELECT COUNT(*) AS yAxis,Person.gender AS xAxis, StatementEntity.xObjectUid AS subgroup FROM StatementEntity LEFT JOIN PERSON ON Person.personUid = StatementEntity.personUid GROUP BY xAxis, subgroup")
    abstract fun findThis(): List<ReportData>

    data class ReportData(var yAxis: Int, var xAxis: String, var subgroup: String)
}
