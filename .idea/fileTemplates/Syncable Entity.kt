#set ($CLASSNAME = $FILE_NAME.substring(0, $FILE_NAME.indexOf(".")) ) 
package ${PACKAGE_NAME}

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import ${PACKAGE_NAME}.${CLASSNAME}.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
class ${CLASSNAME} () {

    @PrimaryKey(autoGenerate = true)
    var ${PREFIX}Uid: Long = 0

    @MasterChangeSeqNum
    var ${PREFIX}MasterCsn: Long = 0

    @LocalChangeSeqNum
    var ${PREFIX}LocalCsn: Long = 0

    @LastChangedBy
    var ${PREFIX}LastModBy: Int = 0

    companion object {

        const val TABLE_ID = TODO("Add a unique integer for the table id")
        
    }
}
