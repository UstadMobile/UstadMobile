package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Serializable
open class ReportFilter {

    var reportFilterUid: Long = 0

    var reportFilterField: Int = 0

    var reportFilterCondition: Int = 0

    var reportFilterValue: String? = null

    companion object {



    }


}