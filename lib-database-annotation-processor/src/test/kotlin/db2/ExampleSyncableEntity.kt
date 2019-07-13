package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@Entity
data class ExampleSyncableEntityTracker(@PrimaryKey var pk: Long = 0, var eUid: Long = 0,
                                         var clientId: Int = 0, var lastCsn: Int = 0)

@Entity
@SyncableEntity(tableId = 42, syncTrackerEntity = ExampleSyncableEntityTracker::class)
data class ExampleSyncableEntity(@PrimaryKey var esUid: Long = 0,
                                 @LocalChangeSeqNum var esLcsn: Int = 0,
                                 @MasterChangeSeqNum var esMcsn: Int = 0,
                                 @LastChangedBy var esLcb: Int = 0,
                                 var esNumber: Int = 0)
