package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndCoursePic
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndPersonPicture
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
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findRequestsForUserAsFlow",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "statusFilter",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT EnrolmentRequest.*, CoursePicture.*
          FROM EnrolmentRequest
               LEFT JOIN CoursePicture
                         ON CoursePicture.coursePictureUid = EnrolmentRequest.erClazzUid
         WHERE EnrolmentRequest.erPersonUid = :accountPersonUid 
           AND (:statusFilter = 0 OR EnrolmentRequest.erStatus = :statusFilter)
    """)
    abstract fun findRequestsForUserAsFlow(
        accountPersonUid: Long,
        statusFilter: Int,
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


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findPendingEnrolmentsForCourse",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true"
                    ),
                    HttpServerFunctionParam(
                        name = "statusFilter",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT EnrolmentRequest.*, PersonPicture.*
          FROM EnrolmentRequest
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = EnrolmentRequest.erPersonUid
         WHERE EnrolmentRequest.erClazzUid = :clazzUid
           AND (:statusFilter = 0 OR EnrolmentRequest.erStatus = :statusFilter)
           AND (CAST(:includeDeleted AS INTEGER) = 1 OR NOT EnrolmentRequest.erDeleted)
           AND (:searchText = '%' OR EnrolmentRequest.erPersonFullname LIKE :searchText)
      ORDER BY CASE(:sortOrder)
                WHEN ${ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_ASC} THEN EnrolmentRequest.erPersonFullname
                WHEN ${ClazzEnrolmentDaoCommon.SORT_LAST_NAME_ASC} THEN EnrolmentRequest.erPersonFullname
                ELSE ''
                END ASC,
                CASE(:sortOrder)
                WHEN ${ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_DESC} THEN EnrolmentRequest.erPersonFullname
                WHEN ${ClazzEnrolmentDaoCommon.SORT_LAST_NAME_DESC} THEN EnrolmentRequest.erPersonFullname
                ELSE ''
            END DESC,
            CASE(:sortOrder)
                WHEN ${ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_ASC} THEN EnrolmentRequest.erRequestTime
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN ${ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_DESC} THEN EnrolmentRequest.erRequestTime
                ELSE 0
            END DESC     
    """)
    abstract fun findPendingEnrolmentsForCourse(
        clazzUid: Long,
        includeDeleted: Boolean,
        statusFilter: Int,
        searchText: String,
        sortOrder: Int,
    ): PagingSource<Int, EnrolmentRequestAndPersonPicture>

}