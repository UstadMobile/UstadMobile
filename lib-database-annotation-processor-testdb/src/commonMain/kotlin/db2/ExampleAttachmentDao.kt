package db2

import androidx.room.Dao
import androidx.room.Insert
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.SetAttachmentData

@Dao
abstract class ExampleAttachmentDao {

    @Insert
    abstract fun insert(entity: ExampleAttachmentEntity): Long

    @SetAttachmentData
    open fun setAttachmentData(entity: ExampleAttachmentEntity, filePath: String) {

    }

    @GetAttachmentData
    open fun getAttachmentDataFileName(entity: ExampleAttachmentEntity): String {
        return ""
    }

}