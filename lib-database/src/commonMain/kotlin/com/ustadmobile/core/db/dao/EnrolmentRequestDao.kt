package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndCoursePic
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class EnrolmentRequestDao {

    @Insert
    abstract suspend fun insert(enrolmentRequest: EnrolmentRequest)

    @Query("""
        SELECT EnrolmentRequest.*
          FROM EnrolmentRequest
         WHERE EnrolmentRequest.erPersonUid = :personUid
           AND EnrolmentRequest.erClazzUid = :clazzUid
           AND (:statusFilter = 0 OR EnrolmentRequest.erStatus = :statusFilter)
    """)
    abstract suspend fun findByClazzAndPerson(
        personUid: Long,
        clazzUid: Long,
        statusFilter: Int,
    ): List<EnrolmentRequest>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByClazzAndPerson",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "statusFilter",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = EnrolmentRequest.STATUS_PENDING.toString(),
                    )
                )
            )
        )
    )
    @Query("""
        SELECT EXISTS(
               SELECT EnrolmentRequest.erUid
                 FROM EnrolmentRequest
                WHERE EnrolmentRequest.erPersonUid = :personUid
                  AND EnrolmentRequest.erClazzUid = :clazzUid
                  AND EnrolmentRequest.erStatus = ${EnrolmentRequest.STATUS_PENDING})
    """)
    abstract suspend fun hasPendingRequests(
        personUid: Long,
        clazzUid: Long,
    ): Boolean

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT EnrolmentRequest.*, CoursePicture.*
          FROM EnrolmentRequest
               LEFT JOIN CoursePicture
                         ON CoursePicture.coursePictureUid = EnrolmentRequest.erClazzUid
         WHERE EnrolmentRequest.erPersonUid = :accountPersonUid 
           AND EnrolmentRequest.erStatus = ${EnrolmentRequest.STATUS_PENDING}
    """)
    abstract fun findPendingRequestsForUserAsFlow(
        accountPersonUid: Long
    ): Flow<List<EnrolmentRequestAndCoursePic>>

    @Query("""
        UPDATE EnrolmentRequest
           SET erStatus = :status,
               erLastModified = :updateTime
         WHERE erUid = :uid      
    """)
    abstract suspend fun updateStatus(
        uid: Long,
        status: Int,
        updateTime: Long,
    )


}