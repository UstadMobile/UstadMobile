package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.LineItem
import com.ustadmobile.core.domain.interop.oneroster.model.toCourseBlock
import com.ustadmobile.core.domain.interop.oneroster.model.Result as OneRosterResult
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterClass
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterLineItem
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterResult
import com.ustadmobile.core.domain.interop.oneroster.model.toStudentResult
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.toLongOrHash
import com.ustadmobile.core.util.ext.localFirstThenRepoIfFalse
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import kotlinx.serialization.json.Json

/**
 *  Implements OneRoster Endpoints by running a database query and converting from database entities
 *  to the OneRoster model.
 *
 *  Mapping OneRoster sourcedId to database primary keys methodology:
 *    If a sourcedId string is a valid Long (eg toLongOrNull != null), then it will be used as the
 *    primary key value.
 *    When a sourcedId string is not a valid long, the string will be hashed using XXHash64(seed=0),
 *    and the hash will be used as the primary key value.
 *
 */
class OneRosterEndpoint(
    private val db: UmAppDatabase,
    repo: UmAppDatabase?,
    private val learningSpace: LearningSpace,
    private val xxHasher: XXStringHasher,
    private val json: Json,
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
        return repoOrDb.clazzDao().findOneRosterUserClazzes(
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

        return repoOrDb.studentResultDao().findByClazzAndStudent(
            clazzUid, studentPersonUid, accountPersonUid
        ).map {
            it.toOneRosterResult(learningSpace)
        }
    }

    suspend fun getLineItem(
        accountPersonUid: Long,
        lineItemSourcedId: String,
    ) : LineItem? {
        return repoOrDb.courseBlockDao().findBySourcedId(
            sourcedId = lineItemSourcedId,
            accountPersonUid = accountPersonUid
        )?.toOneRosterLineItem(learningSpace, json)
    }

    /**
     * As per the spec (table 3.1c), if a given LineItem already exists, it will be replaced
     *
     * @return As per OneRoster spec (Section 3.5) 201 is returned if a new resource is created,
     *         200 otherwise
     */
    suspend fun putLineItem(
        @Suppress("UNUSED_PARAMETER")  //Reserved for future use
        accountPersonUid: Long,
        lineItemSourcedId: String,
        lineItem: LineItem
    ) : PutResponse {
        val courseBlock = lineItem.toCourseBlock(xxHasher, json)

        if(
            !repoOrDb.localFirstThenRepoIfFalse {
                it.clazzDao().clazzUidExistsAsync(courseBlock.cbClazzUid)
            }
        ) {
            return PutResponse(400, "Clazz SourcedId does not exist: ${courseBlock.cbClazzUid}")
        }

        val isUpdate = db.courseBlockDao().existsByUid(
            xxHasher.toLongOrHash(lineItemSourcedId))
        repoOrDb.courseBlockDao().replaceListAsync(listOf(courseBlock))

        return if(isUpdate) {
            PutResponse(200, "")
        }else {
            PutResponse(201, null)
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
        val srUid = xxHasher.toLongOrHash(resultSourcedId)
        val lineItemUid = xxHasher.toLongOrHash(result.lineItem.sourcedId)
        val isUpdate = db.studentResultDao().existsByUid(srUid)

        val blockUidAndClazzUid = repoOrDb.localFirstThenRepoIfNull {
            it.courseBlockDao().findCourseBlockAndClazzUidByCbUid(
                lineItemUid, accountPersonUid
            )
        } ?: return PutResponse(400, "LineItem SourcedId does not exist: ${result.lineItem.sourcedId}")

        val studentResult = result.toStudentResult(
            xxHasher = xxHasher, clazzUid = blockUidAndClazzUid.clazzUid
        )

        if(
            repoOrDb.localFirstThenRepoIfNull {
                it.personDao().findByUidAsync(studentResult.srStudentPersonUid)
            } == null
        ) {
            return PutResponse(400, "Invalid student sourcedId (not found): ${result.student.sourcedId}")
        }

        repoOrDb.studentResultDao().upsertAsync(studentResult)

        return if(isUpdate) {
            PutResponse(200, "")
        }else {
            PutResponse(201, null)
        }
    }


}