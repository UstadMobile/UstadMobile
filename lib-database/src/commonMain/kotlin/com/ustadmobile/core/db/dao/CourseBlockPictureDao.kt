package com.ustadmobile.core.db.dao

import androidx.room.Insert
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.CourseBlockPicture

@Repository
@DoorDao
expect abstract class CourseBlockPictureDao {

    @Insert
    abstract suspend fun insertListAsync(entities: List<CourseBlockPicture>)

}