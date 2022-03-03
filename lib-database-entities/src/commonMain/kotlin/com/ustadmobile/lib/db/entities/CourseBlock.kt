package com.ustadmobile.lib.db.entities

import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedTime
import com.ustadmobile.door.annotation.ReplicationVersionId
import kotlinx.serialization.Serializable

@Serializable
class CourseBlock {

    @PrimaryKey(autoGenerate = true)
    var cbUid: Long = 0

    var cbType: Int = 0

    var cbIndentLevel: Int = 0

    var cbTitle: String? = null

    var cbDescription: String? = null

    var cbIndex: Int = 0

    var cbClazzUid: Long = 0

    var cbActive: Boolean = true

    @LastChangedTime
    @ReplicationVersionId
    var cbLct: Long = 0


}