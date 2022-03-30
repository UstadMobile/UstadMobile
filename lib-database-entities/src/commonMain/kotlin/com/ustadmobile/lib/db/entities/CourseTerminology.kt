package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseTerminology.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = CourseTerminologyReplicate::class)
@Serializable
@Triggers(arrayOf(
    Trigger(
        name = "courseterminology_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """REPLACE INTO CourseTerminology(ctUid, ctTitle, ctTerminology, ctLct) 
         VALUES (NEW.ctUid, NEW.ctTitle, NEW.ctTerminology, NEW.ctLct) 
         /*psql ON CONFLICT (ctUid) DO UPDATE 
         SET ctTitle = EXCLUDED.ctTitle, ctTerminology = EXCLUDED.ctTerminology, ctLct = EXCLUDED.ctLct
         */"""
        ]
    )
))
open class CourseTerminology {

    @PrimaryKey(autoGenerate = true)
    var ctUid: Long = 0

    var ctTitle: String? = null

    var ctTerminology: String? = null

    @LastChangedTime
    @ReplicationVersionId
    var ctLct: Long = 0

    companion object {

        const val TABLE_ID = 450


    }

}