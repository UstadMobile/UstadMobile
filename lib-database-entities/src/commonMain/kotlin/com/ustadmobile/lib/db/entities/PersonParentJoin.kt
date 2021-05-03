package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LastChangedTime
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import kotlinx.serialization.Serializable

@Entity
@Serializable
class PersonParentJoin {

    @PrimaryKey(autoGenerate = true)
    var ppjUid: Long = 0

    @MasterChangeSeqNum
    var ppjPcsn: Long = 0

    @LocalChangeSeqNum
    var ppjLcsn: Long = 0

    @LastChangedBy
    var ppjLcb: Int = 0

    @LastChangedTime
    var ppjLct: Long = 0

    var ppjParentPersonUid: Long = 0

    var ppjMinorPersonUid: Long = 0

    var ppjRelationship: Int = 0

    var ppjEmail: String? = null

    var ppjPhone: String? = null

    var ppjInactive: Boolean = false

    var ppjApprovalTiemstamp: Long = 0

    var ppjApprovalIpAddr: String? = null

    companion object {

    }

}