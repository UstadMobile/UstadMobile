package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseGroupSet.TABLE_ID, tracker = CourseGroupSetReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "coursegroupset_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """REPLACE INTO CourseGroupSet(cgsUid, cgsName, cgsTotalGroups, cgsActive, cgsClazzUid, cgsLct) 
         VALUES (NEW.cgsUid, NEW.cgsName, NEW.cgsTotalGroups, NEW.cgsActive, NEW.cgsClazzUid, NEW.cgsLct) 
         /*psql ON CONFLICT (cgsUid) DO UPDATE 
         SET cgsName = EXCLUDED.cgsName, cgsTotalGroups = EXCLUDED.cgsTotalGroups, cgsActive = EXCLUDED.cgsActive, cgsClazzUid = EXCLUDED.cgsClazzUid, cgsLct = EXCLUDED.cgsLct
         */"""
        ]
    )
))
@Serializable
class CourseGroupSet {

    @PrimaryKey(autoGenerate = true)
    var cgsUid: Long = 0

    var cgsName: String? = null

    var cgsTotalGroups: Int = 4

    var cgsActive: Boolean = true

    @ColumnInfo(index = true)
    var cgsClazzUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var cgsLct: Long = 0

    companion object {

        const val TABLE_ID = 242


    }


}