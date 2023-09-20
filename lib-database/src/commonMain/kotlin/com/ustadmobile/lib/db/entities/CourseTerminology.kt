package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseTerminology.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Serializable
@Triggers(arrayOf(
    Trigger(
        name = "courseterminology_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
        ]
    )
))
open class CourseTerminology {

    @PrimaryKey(autoGenerate = true)
    var ctUid: Long = 0

    var ctTitle: String? = null

    /**
     * A json map of keys as per TerminologyKeys to the terminology to use for this course.
     *
     * see CourseTerminologyStrings (in core)
     */
    var ctTerminology: String? = null

    @ReplicateLastModified
    @ReplicateEtag
    var ctLct: Long = 0

    companion object {

        const val TABLE_ID = 450


    }

}