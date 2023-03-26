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

    /**
     * A json map of keys as per TerminologyKeys to the terminology to use for this course.
     *
     * see CourseTerminologyStrings (in core)
     */
    var ctTerminology: String? = null

    @LastChangedTime
    @ReplicationVersionId
    var ctLct: Long = 0



    companion object {

        const val TABLE_ID = 450


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseTerminology) return false

        if (ctUid != other.ctUid) return false
        if (ctTitle != other.ctTitle) return false
        if (ctTerminology != other.ctTerminology) return false
        if (ctLct != other.ctLct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ctUid.hashCode()
        result = 31 * result + (ctTitle?.hashCode() ?: 0)
        result = 31 * result + (ctTerminology?.hashCode() ?: 0)
        result = 31 * result + ctLct.hashCode()
        return result
    }

}