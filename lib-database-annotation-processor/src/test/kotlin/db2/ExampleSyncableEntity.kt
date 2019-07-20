package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
data class ExampleSyncableEntityTracker(@PrimaryKey var pk: Long = 0,
                                        @TrackerEntityPrimaryKey var eUid: Long = 0,
                                        @TrackDestId var clientId: Int = 0,
                                        @TrackerChangeSeqNum var lastCsn: Int = 0,
                                        @TrackerReceived var received: Boolean = false,
                                        @TrackerRequestId var reqId: Int = 0,
                                        @TrackerTimestamp var ts: Long = 0)

@Entity
@SyncableEntity(tableId = 42, syncTrackerEntity = ExampleSyncableEntityTracker::class)
data class ExampleSyncableEntity(@PrimaryKey var esUid: Long = 0,
                                 @LocalChangeSeqNum var esLcsn: Int = 0,
                                 @MasterChangeSeqNum var esMcsn: Int = 0,
                                 @LastChangedBy var esLcb: Int = 0,
                                 var esNumber: Int = 0)
