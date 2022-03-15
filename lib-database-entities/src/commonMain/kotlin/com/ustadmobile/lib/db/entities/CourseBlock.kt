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
                    """REPLACE INTO CourseBlock(cbUid, cbType, cbIndentLevel, cbModuleParentBlock, cbTitle, cbDescription, cbIndex, cbClazzUid, cbActive,cbHidden, cbTableId, cbTableUid, cbLct) 
         VALUES (NEW.cbUid, NEW.cbType, NEW.cbIndentLevel, NEW.cbModuleParentBlock, NEW.cbTitle, NEW.cbDescription, NEW.cbIndex, NEW.cbClazzUid,NEW.cbActive, NEW.cbHidden, NEW.cbTableId,  NEW.cbTableUid, NEW.cbLct) 
         /*psql ON CONFLICT (cbUid) DO UPDATE 
         SET cbUid = EXCLUDED.cbUid, cbType = EXCLUDED.cbType, cbIndentLevel = EXCLUDED.cbIndentLevel, cbModuleParentBlock = EXCLUDED.cbModuleParentBlock, cbTitle = EXCLUDED.cbTitle, cbDescription = EXCLUDED.cbDescription, cbIndex = EXCLUDED.cbIndex,cbClazzUid = EXCLUDED.cbClazzUid, cbActive = EXCLUDED.cbActive, cbHidden = EXCLUDED.cbHidden, cbTableId = EXCLUDED.cbTableId, cbTableUid = EXCLUDED.cbTableUid, cbLct = EXCLUDED.cbLct
         */"""
                ]
        )
))
@Serializable
open class CourseBlock {

    @PrimaryKey(autoGenerate = true)
    var cbUid: Long = 0

    var cbType: Int = 0

    var cbIndentLevel: Int = 0

    var cbModuleParentBlock: Long = 0

    var cbTitle: String? = null

    var cbDescription: String? = null

    var cbIndex: Int = 0

    var cbClazzUid: Long = 0

    var cbActive: Boolean = true

    var cbHidden: Boolean = false

    var cbTableId: Int = 0

    var cbTableUid: Long = 0

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CourseBlock

        if (cbUid != other.cbUid) return false
        if (cbType != other.cbType) return false
        if (cbIndentLevel != other.cbIndentLevel) return false
        if (cbModuleParentBlock != other.cbModuleParentBlock) return false
        if (cbTitle != other.cbTitle) return false
        if (cbDescription != other.cbDescription) return false
        if (cbIndex != other.cbIndex) return false
        if (cbClazzUid != other.cbClazzUid) return false
        if (cbActive != other.cbActive) return false
        if (cbHidden != other.cbHidden) return false
        if (cbTableId != other.cbTableId) return false
        if (cbTableUid != other.cbTableUid) return false
        if (cbLct != other.cbLct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cbUid.hashCode()
        result = 31 * result + cbType
        result = 31 * result + cbIndentLevel
        result = 31 * result + cbModuleParentBlock.hashCode()
        result = 31 * result + (cbTitle?.hashCode() ?: 0)
        result = 31 * result + (cbDescription?.hashCode() ?: 0)
        result = 31 * result + cbIndex
        result = 31 * result + cbClazzUid.hashCode()
        result = 31 * result + cbActive.hashCode()
        result = 31 * result + cbHidden.hashCode()
        result = 31 * result + cbTableId
        result = 31 * result + cbTableUid.hashCode()
        result = 31 * result + cbLct.hashCode()
        return result
    }

}