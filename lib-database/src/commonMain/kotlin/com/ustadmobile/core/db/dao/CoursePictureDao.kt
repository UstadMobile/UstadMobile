package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CoursePicture


@DoorDao
@Repository
expect abstract class CoursePictureDao : BaseDao<CoursePicture> {

    @Query("""SELECT * FROM CoursePicture 
        WHERE coursePictureClazzUid = :clazzUid
        AND CAST(coursePictureActive AS INTEGER) = 1
        ORDER BY coursePictureTimestamp DESC LIMIT 1""")
    abstract suspend fun findByClazzUidAsync(clazzUid: Long): CoursePicture?

    @Query("SELECT * FROM CoursePicture where coursePictureClazzUid = :clazzUid ORDER BY " + " coursePictureTimestamp DESC LIMIT 1")
    abstract fun findByClazzUidLive(clazzUid: Long): Flow<CoursePicture?>


    @Update
    abstract suspend fun updateAsync(coursePicture: CoursePicture)



}