package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.LineItem
import com.ustadmobile.core.domain.interop.oneroster.model.Status
import com.ustadmobile.core.domain.interop.oneroster.model.toCourseBlock
import com.ustadmobile.core.domain.interop.oneroster.model.Result as OneRosterResult
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterClass
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterLineItem
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterResult
import com.ustadmobile.core.domain.interop.oneroster.model.toStudentResult
import com.ustadmobile.core.domain.interop.timestamp.parse8601Timestamp
import com.ustadmobile.core.util.ext.localFirstThenRepoIfFalse
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull

/**
 *  Implements OneRoster Endpoints by running a database query and converting from database entities
 *  to the OneRoster model.
 */
class OneRosterEndpoint(
    private val db: UmAppDatabase,
    repo: UmAppDatabase?,
    private val endpoint: Endpoint,
) {

    private val repoOrDb = repo ?: db

    data class PutResponse(
        val statusCode: Int,
        val body: String?
    )

    /**
     * @param userSourcedId in reality this is the personUid. This will likely need to change to
     *        accept a string as per the OneRoster spec
     */
    suspend fun getClassesForUser(
        accountPersonUid: Long,
        userSourcedId: String,
    ) : List<Clazz> {
        return repoOrDb.clazzDao.findOneRosterUserClazzes(
            accountPersonUid, userSourcedId.toLongOrNull() ?: 0
        ).map {
            it.toOneRosterClass()
        }
    }


    suspend fun getResultsForStudentForClass(
        accountPersonUid: Long,
        clazzSourcedId: String,
        studentSourcedId: String,
    ) : List<OneRosterResult> {
        val clazzUid = clazzSourcedId.toLongOrNull() ?: 0
        val studentPersonUid = studentSourcedId.toLongOrNull() ?: 0

        return repoOrDb.studentResultDao.findByClazzAndStudent(
            clazzUid, studentPersonUid, accountPersonUid
        ).map {
            it.toOneRosterResult(endpoint)
        }
    }

    suspend fun getLineItem(
        accountPersonUid: Long,
        lineItemSourcedId: String,
    ) : LineItem? {
        return repoOrDb.courseBlockDao.findBySourcedId(
            sourcedId = lineItemSourcedId,
            accountPersonUid = accountPersonUid
        )?.toOneRosterLineItem(endpoint)
    }

    /**
     * @return As per OneRoster spec (Section 3.5) 201 is returned if a new resource is created,
     *         200 otherwise
     */
    suspend fun putLineItem(
        accountPersonUid: Long,
        lineItemSourcedId: String,
        lineItem: LineItem
    ) : PutResponse {
        val existingCourseBlock = db.courseBlockDao.findBySourcedId(
            lineItemSourcedId, accountPersonUid
        )

        return if(existingCourseBlock == null) {
            val courseBlock = lineItem.toCourseBlock()

            when {
                courseBlock.cbClazzUid == 0L -> {
                    PutResponse(400, "Invalid class sourcedId: ${lineItem.sourcedId}")
                }

                !repoOrDb.localFirstThenRepoIfFalse {
                    it.clazzDao.clazzUidExistsAsync(courseBlock.cbClazzUid)
                } -> {
                    PutResponse(400, "Clazz SourcedId does not exist: ${courseBlock.cbClazzUid}")
                }

                else -> {
                    repoOrDb.courseBlockDao.insert(lineItem.toCourseBlock())
                    PutResponse(201, null)
                }
            }
        }else {
            repoOrDb.courseBlockDao.updateFromLineItem(
                cbUid = existingCourseBlock.cbUid,
                active = lineItem.status == Status.ACTIVE,
                dateLastModified = parse8601Timestamp(lineItem.dateLastModified),
                title = lineItem.description,
                description = lineItem.description,
                assignDate = parse8601Timestamp(lineItem.assignDate),
                dueDate = parse8601Timestamp(lineItem.dueDate),
                resultValueMin = lineItem.resultValueMin,
                resultValueMax = lineItem.resultValueMax
            )
            PutResponse(200, "")
        }
    }

    /**
     * @return As per OneRoster spec (Section 3.5) 201 is returned if a new resource is created,
     *         200 otherwise
     */
    suspend fun putResult(
        accountPersonUid: Long,
        resultSourcedId: String,
        result: OneRosterResult
    ) : PutResponse {
        val existingStudentResultUid = repoOrDb.localFirstThenRepoIfNull {
            it.studentResultDao.findUidBySourcedId(resultSourcedId)
        }

        val blockUidAndClazzUid = repoOrDb.localFirstThenRepoIfNull {
            it.courseBlockDao.findCourseBlockUidAndClazzUidBySourcedId(
                result.lineItem.sourcedId, accountPersonUid
            )
        } ?: return PutResponse(400, "LineItem SourcedId does not exist: ${result.lineItem.sourcedId}")

        val studentResult = result.toStudentResult().copy(
            srCourseBlockUid = blockUidAndClazzUid.courseBlockUid,
            srClazzUid = blockUidAndClazzUid.clazzUid,
        )

        if(
            repoOrDb.localFirstThenRepoIfNull {
                it.personDao.findByUidAsync(studentResult.srStudentPersonUid)
            } == null
        ) {
            return PutResponse(400, "Invalid student sourcedId (not found): ${result.student.sourcedId}")
        }

        return if(existingStudentResultUid == 0L) {
            repoOrDb.studentResultDao.insertListAsync(listOf(studentResult))
            PutResponse(201, null)
        }else {
            repoOrDb.studentResultDao.updateAsync(studentResult.copy(srUid = existingStudentResultUid))
            PutResponse(200, "")
        }
    }


}