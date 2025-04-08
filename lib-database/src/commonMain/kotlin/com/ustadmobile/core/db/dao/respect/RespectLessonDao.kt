package com.ustadmobile.core.db.dao.respect

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.respect.RespectLesson
import com.ustadmobile.lib.db.entities.respect.RespectLessonAndApp

@DoorDao
@Repository
expect abstract class RespectLessonDao {

    @Query("""
        SELECT RespectLesson.*, RespectApp.*
          FROM RespectLesson
               JOIN RespectApp
                    ON RespectApp.raUid = RespectLesson.rlRaUid
    """)
    @HttpAccessible
    abstract fun allLessons(): PagingSource<Int, RespectLessonAndApp>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertOrIgnore(lessons: List<RespectLesson>)

}