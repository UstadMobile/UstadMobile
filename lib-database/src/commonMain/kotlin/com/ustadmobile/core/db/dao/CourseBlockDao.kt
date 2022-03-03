package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlin.js.JsName

@Repository
@Dao
abstract class CourseBlockDao : BaseDao<CourseBlock>, OneToManyJoinDao<CourseBlock> {

    @Query("""
     TODO
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseBlock::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)



    @Query("""
        TODO
    """)
    @ReplicationRunOnChange([CourseBlock::class])
    @ReplicationCheckPendingNotificationsFor([CourseBlock::class])
    abstract suspend fun replicateOnChange()

    @JsName("findByUid")
    @Query("SELECT * FROM CourseBlock WHERE cbUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): CourseBlock?

    @Update
    abstract suspend fun updateAsync(entity: CourseBlock): Int


    @Query("""SELECT * 
                     FROM CourseBlock 
                    WHERE cbClazzUid = :clazzUid
                      AND cbActive""")
    abstract suspend fun findAllCourseBlockByClazzUidAsync(clazzUid: Long): List<CourseBlock>




}