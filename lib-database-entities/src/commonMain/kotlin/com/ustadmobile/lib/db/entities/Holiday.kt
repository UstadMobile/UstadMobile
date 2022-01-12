package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Holiday.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Triggers(arrayOf(
 Trigger(
     name = "holiday_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Holiday(holUid, holMasterCsn, holLocalCsn, holLastModBy, holLct, holActive, holHolidayCalendarUid, holStartTime, holEndTime, holName) 
         VALUES (NEW.holUid, NEW.holMasterCsn, NEW.holLocalCsn, NEW.holLastModBy, NEW.holLct, NEW.holActive, NEW.holHolidayCalendarUid, NEW.holStartTime, NEW.holEndTime, NEW.holName) 
         /*psql ON CONFLICT (holUid) DO UPDATE 
         SET holMasterCsn = EXCLUDED.holMasterCsn, holLocalCsn = EXCLUDED.holLocalCsn, holLastModBy = EXCLUDED.holLastModBy, holLct = EXCLUDED.holLct, holActive = EXCLUDED.holActive, holHolidayCalendarUid = EXCLUDED.holHolidayCalendarUid, holStartTime = EXCLUDED.holStartTime, holEndTime = EXCLUDED.holEndTime, holName = EXCLUDED.holName
         */"""
     ]
 )
))
@ReplicateEntity(tableId = TABLE_ID, tracker = HolidayReplicate::class)
@Serializable
class Holiday() {

    @PrimaryKey(autoGenerate = true)
    var holUid: Long = 0

    @MasterChangeSeqNum
    var holMasterCsn: Long = 0

    @LocalChangeSeqNum
    var holLocalCsn: Long = 0

    @LastChangedBy
    var holLastModBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var holLct: Long = 0

    var holActive: Boolean = true

    var holHolidayCalendarUid: Long = 0

    var holStartTime: Long = 0

    var holEndTime: Long = 0

    var holName: String? = null



    companion object {

        const val TABLE_ID = 99

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Holiday

        if (holUid != other.holUid) return false
        if (holMasterCsn != other.holMasterCsn) return false
        if (holLocalCsn != other.holLocalCsn) return false
        if (holLastModBy != other.holLastModBy) return false
        if (holActive != other.holActive) return false
        if (holHolidayCalendarUid != other.holHolidayCalendarUid) return false
        if (holStartTime != other.holStartTime) return false
        if (holEndTime != other.holEndTime) return false
        if (holName != other.holName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = holUid.hashCode()
        result = 31 * result + holMasterCsn.hashCode()
        result = 31 * result + holLocalCsn.hashCode()
        result = 31 * result + holLastModBy
        result = 31 * result + holActive.hashCode()
        result = 31 * result + holHolidayCalendarUid.hashCode()
        result = 31 * result + holStartTime.hashCode()
        result = 31 * result + holEndTime.hashCode()
        result = 31 * result + (holName?.hashCode() ?: 0)
        return result
    }
}
