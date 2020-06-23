package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ReportFilter.TABLE_ID)
@Serializable
open class ReportFilter {

    constructor()

    /*constructor(entityUid: Long, entityType: Int){
        this.entityUid = entityUid
        this.entityType = entityType
    }*/

    @PrimaryKey(autoGenerate = true)
    var reportFilterUid: Long = 0

    var reportFilterReportUid: Long = 0

    var entityUid: Long = 0

    var entityType: Int = 0

    var filterInactive: Boolean = false

    @MasterChangeSeqNum
    var reportFilterMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var reportFilterLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var reportFilterLastChangedBy: Int = 0


    companion object {

        const val TABLE_ID = 102

        const val PERSON_FILTER = 50

        const val CONTENT_FILTER = 80

        const val VERB_FILTER = 70

    }


}