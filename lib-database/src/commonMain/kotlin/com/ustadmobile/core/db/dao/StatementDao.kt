package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@Repository
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
    abstract suspend fun getResults(query: DoorQuery): List<ReportData>

    open suspend fun getResults(sqlStr: String, paramsList: Array<Any>): List<ReportData> {
        return getResults(SimpleDoorQuery(sqlStr, paramsList))
    }

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    abstract fun getListResults(query: DoorQuery): DataSource.Factory<Int, StatementEntityWithDisplayDetails>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("SELECT * FROM XLangMapEntry LIMIT 1")
    abstract fun getXLangMap(): XLangMapEntry?

    @Serializable
    data class ReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")

    companion object{

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

        const val SORT_LAST_ACTIVE_ASC = 5

        const val SORT_LAST_ACTIVE_DESC = 6


    }


}
