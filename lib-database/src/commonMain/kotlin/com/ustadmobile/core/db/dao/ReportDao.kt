package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Report
import kotlin.js.JsName

@Dao
@Repository
abstract class ReportDao : BaseDao<Report> {

    @RawQuery
    abstract fun getResults(query: DoorQuery): List<Report>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND reportOwnerUid = :personUid
        AND isTemplate = :isTemplate
        AND reportTitle LIKE :searchBit
        ORDER BY priority, CASE(:sortOrder)
            WHEN $SORT_TITLE_ASC THEN Report.reportTitle
            ELSE ''
        END ASC,
        CASE(:sortOrder)
            WHEN $SORT_TITLE_DESC THEN Report.reportTitle
            ELSE ''
        END DESC
            """)
    abstract fun findAllActiveReport(searchBit: String, personUid: Long, sortOrder: Int,
                                     isTemplate: Boolean)
            : DoorDataSourceFactory<Int, Report>

    @Query("SELECT * FROM Report WHERE reportUid = :entityUid")
    abstract suspend fun findByUid(entityUid: Long): Report?

    @Update
    abstract suspend fun updateAsync(entity: Report)

    @Query("SELECT * From Report WHERE  reportUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Report?>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND isTemplate = :isTemplate
        ORDER BY priority ASC
            """)
    abstract fun findAllActiveReportLive(isTemplate: Boolean)
            : DoorLiveData<List<Report>>

    @Query("""SELECT * FROM REPORT WHERE NOT reportInactive 
        AND isTemplate = :isTemplate
        ORDER BY priority ASC
            """)
    abstract fun findAllActiveReportList(isTemplate: Boolean): List<Report>

    @Query("""UPDATE Report SET reportInactive = :inactive,
                reportLastChangedBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
                WHERE reportUid = :uid""")
    abstract fun updateReportInactive(inactive: Boolean, uid: Long)

    @JsName("findByUidList")
    @Query("SELECT reportUid FROM Report WHERE reportUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>


    @Query("""UPDATE Report SET reportInactive = :toggleVisibility, 
                reportLastChangedBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
                WHERE reportUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityReportItems(toggleVisibility: Boolean, selectedItem: List<Long>)


    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<Report>)

    suspend fun initPreloadedTemplates() {
        val uidsInserted = findByUidList(Report.FIXED_TEMPLATES.map { it.reportUid })
        val templateListToInsert = Report.FIXED_TEMPLATES.filter { it.reportUid !in uidsInserted }
        replaceList(templateListToInsert)
    }

    companion object{

        const val SORT_TITLE_ASC = 1

        const val SORT_TITLE_DESC = 2

    }

}