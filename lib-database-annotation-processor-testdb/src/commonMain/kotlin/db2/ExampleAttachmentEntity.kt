package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
@SyncableEntity(tableId = 57)
@EntityWithAttachment
class ExampleAttachmentEntity(@PrimaryKey(autoGenerate = true) var eaUid: Long = 0,
                              @LocalChangeSeqNum var eaLcsn: Int = 0,
                              @MasterChangeSeqNum var eaMcsn: Int = 0,
                              @LastChangedBy var eaLcb: Int = 0,
                              var eaMimeType: String = "application/octet",
                              var eaNumber: Int = 0)
