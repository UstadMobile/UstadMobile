package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*


@Entity
@SyncableEntity(tableId = 44)
data class OtherSyncableEntity (@PrimaryKey var osUid: Long = 0,
                                @LastChangedBy var osLcb: Int = 0,
                                @MasterChangeSeqNum var osMcsn: Int = 0,
                                @LocalChangeSeqNum var osLcsn: Int = 0,
                                var otherFk: Int = 0,
                                var otherNum: Int = 0,
                                var otherStr: String = "")