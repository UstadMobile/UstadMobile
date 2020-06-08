package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportFilterWithDisplayDetails
import com.ustadmobile.lib.db.entities.VerbDisplay

@Dao
@UmRepository
abstract class ReportFilterDao : BaseDao<ReportFilter>, OneToManyJoinDao<ReportFilter> {

    @Query("""SELECT ReportFilter.*, Person.*, VerbEntity.*, 
        (SELECT valueLangMap FROM XLangMapEntry LEFT JOIN VerbEntity ON 
        VerbEntity.verbUid = XLangMapEntry.verbLangMapUid) AS xlangMapDisplay, 
        ContentEntry.* FROM ReportFilter 
        LEFT JOIN Person On ReportFilter.entityType = ${ReportFilter.PERSON_FILTER} 
        AND Person.personUid = ReportFilter.entityUid 
        LEFT JOIN VerbEntity On ReportFilter.entityType = ${ReportFilter.VERB_FILTER} 
        AND VerbEntity.verbUid = ReportFilter.entityUid 
          LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid 
          LEFT JOIN ContentEntry On ReportFilter.entityType = ${ReportFilter.CONTENT_FILTER}
           AND ContentEntry.contentEntryUid = ReportFilter.entityUid
        WHERE ReportFilter.reportFilterReportUid = :reportUid AND NOT ReportFilter.filterInactive""")
    abstract fun findByReportUid(reportUid: Long): List<ReportFilterWithDisplayDetails>

    @Update
    abstract fun updateAsyncList(reportFilterList: List<ReportFilter>)


    @Query("UPDATE ReportFilter SET filterInactive = :active WHERE reportFilterUid = :holidayUid")
    abstract fun updateActiveByUid(holidayUid: Long, active: Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach { updateActiveByUid(it, true) }
    }



}