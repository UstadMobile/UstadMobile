package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.SyncableEntity

@Entity
@SyncableEntity(tableId = 20)
class AccessGrant {

    @PrimaryKey(autoGenerate = true)
    val accessId: Long = 0

    val deviceId: Int = 0

    val tableId: Int = 0

    val entityUid: Long = 0

}