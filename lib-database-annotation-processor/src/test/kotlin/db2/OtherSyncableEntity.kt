package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*


@Entity
data class OtherSyncableEntityTracker(@PrimaryKey var pk: Long = 0,
                                        @TrackerEntityPrimaryKey var eUid: Long = 0,
                                        @TrackDestId var clientId: Int = 0,
                                        @TrackerChangeSeqNum var lastCsn: Int = 0,
                                        @TrackerReceived var received: Boolean = false,
                                        @TrackerRequestId var reqId: Int = 0,
                                        @TrackerTimestamp var ts: Long = 0)

@Entity
@SyncableEntity(tableId = 44, syncTrackerEntity = OtherSyncableEntityTracker::class)
data class OtherSyncableEntity (@PrimaryKey var osUid: Long = 0,
                                @LastChangedBy var osLcb: Int = 0,
                                @MasterChangeSeqNum var osMcsn: Int = 0,
                                @LocalChangeSeqNum var osLcsn: Int = 0,
                                var otherFk: Int = 0,
                                var otherNum: Int = 0,
                                var otherStr: String = "")