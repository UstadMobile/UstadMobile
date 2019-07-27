package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
data class ExampleSyncableEntityTracker(@PrimaryKey(autoGenerate = true) var pk: Long = 0,
                                        @TrackerEntityPrimaryKey var eUid: Long = 0,
                                        @TrackDestId var clientId: Int = 0,
                                        @TrackerChangeSeqNum var lastCsn: Int = 0,
                                        @TrackerReceived var received: Boolean = false,
                                        @TrackerRequestId var reqId: Int = 0,
                                        @TrackerTimestamp var ts: Long = 0)

@Entity
@SyncableEntity(tableId = 42, syncTrackerEntity = ExampleSyncableEntityTracker::class)
open class ExampleSyncableEntity(@PrimaryKey(autoGenerate = true) var esUid: Long = 0,
                                 @LocalChangeSeqNum var esLcsn: Int = 0,
                                 @MasterChangeSeqNum var esMcsn: Int = 0,
                                 @LastChangedBy var esLcb: Int = 0,
                                 var esNumber: Int = 0) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExampleSyncableEntity) return false

        if (esUid != other.esUid) return false
        if (esLcsn != other.esLcsn) return false
        if (esMcsn != other.esMcsn) return false
        if (esLcb != other.esLcb) return false
        if (esNumber != other.esNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = esUid.hashCode()
        result = 31 * result + esLcsn
        result = 31 * result + esMcsn
        result = 31 * result + esLcb
        result = 31 * result + esNumber
        return result
    }
}
