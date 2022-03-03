package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseBlock.TABLE_ID, tracker = CourseBlockReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "courseblock_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO CourseBlock(cbUid, cbType, cbIndentLevel, cbTitle, cbDescription, cbIndex, cbClazzUid, cbActive, cbLct) 
         VALUES (NEW.cbUid, NEW.cbType, NEW.cbIndentLevel, NEW.cbTitle, NEW.cbDescription, NEW.cbIndex, NEW.cbClazzUid,NEW.cbActive, NEW.cbLct ) 
         /*psql ON CONFLICT (cbUid) DO UPDATE 
         SET cbUid = EXCLUDED.cbUid, cbType = EXCLUDED.cbType, cbIndentLevel = EXCLUDED.cbIndentLevel, cbTitle = EXCLUDED.cbTitle, cbDescription = EXCLUDED.cbDescription, cbIndex = EXCLUDED.cbIndex,cbClazzUid = EXCLUDED.cbClazzUid, cbActive = EXCLUDED.cbActive, cbLct = EXCLUDED.cbLct
         */"""
                ]
        )
))
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

    companion object {

        const val TABLE_ID = 124

        const val BLOCK_MODULE_TYPE = 1

        const val BLOCK_TEXT_TYPE = 2

        const val BLOCK_ASSIGNMENT_TYPE = 3

        const val BLOCK_CONTENT_TYPE = 4

        const val BLOCK_DISCUSSION_TYPE = 5

    }

}