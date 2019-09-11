package db2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 42)
@Serializable
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
