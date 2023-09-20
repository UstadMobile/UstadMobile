package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseDiscussion

@DoorDao
@Repository
expect abstract class CourseDiscussionDao: BaseDao<CourseDiscussion>, OneToManyJoinDao<CourseDiscussion>{



    @Query("""
        UPDATE CourseDiscussion 
           SET courseDiscussionActive = :active, 
               courseDiscussionLct = :changeTime
         WHERE courseDiscussionUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    @Query("""
        SELECT CourseDiscussion.* 
          FROM CourseDiscussion
         WHERE CourseDiscussion.courseDiscussionUid = :courseDiscussionUid 
           AND CAST(CourseDiscussion.courseDiscussionActive AS INTEGER) = 1 
         
         """)
    abstract fun getCourseDiscussionByUid(courseDiscussionUid: Long): Flow<CourseDiscussion?>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<CourseDiscussion>)

}