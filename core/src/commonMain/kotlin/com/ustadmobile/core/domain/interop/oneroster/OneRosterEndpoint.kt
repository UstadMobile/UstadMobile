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

/**
 *  Implements OneRoster Endpoints by running a database query and converting from database entities
 *  to the OneRoster model.
 */
class OneRosterEndpoint(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val endpoint: Endpoint,
) {

    /**
     * @param userSourcedId in reality this is the personUid. This will likely need to change to
     *        accept a string as per the OneRoster spec
     */
    suspend fun getClassesForUser(
        accountPersonUid: Long,
        userSourcedId: String,
    ) : List<Clazz> {
        return db.clazzDao.findOneRosterUserClazzes(
            accountPersonUid, userSourcedId.toLong()
        ).map {
            it.toOneRosterClass()
        }
    }


    suspend fun getResultsForStudentForClass(
        accountPersonUid: Long,
        clazzSourcedId: String,
        studentSourcedId: String,
    ) : List<OneRosterResult> {
        val clazzUid = clazzSourcedId.toLong()
        val studentPersonUid = studentSourcedId.toLong()

        return db.studentResultDao.findByClazzAndStudent(
            clazzUid, studentPersonUid, accountPersonUid
        ).map {
            it.toOneRosterResult(endpoint)
        }
    }

    suspend fun getLineItem(
        accountPersonUid: Long,
        lineItemSourcedId: String,
    ) : LineItem? {
        return db.courseBlockDao.findBySourcedId(
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
    ) : Int {
        val existingCourseBlock = db.courseBlockDao.findBySourcedId(
            lineItemSourcedId, accountPersonUid
        )

        return if(existingCourseBlock == null) {
            db.courseBlockDao.insert(lineItem.toCourseBlock())
            201
        }else {
            db.courseBlockDao.updateFromLineItem(
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
            200
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
    ) : Int {
        val existingStudentResultUid = db.studentResultDao.findUidBySourcedId(resultSourcedId)

        val blockUidAndClazzUid = db.courseBlockDao
            .findCourseBlockUidAndClazzUidBySourcedId(
                result.lineItem.sourcedId, accountPersonUid
            ) ?: throw IllegalArgumentException("Cannot find LineItem (courseblock) for result: " +
                "${result.lineItem.sourcedId} ")

        val studentResult = result.toStudentResult().copy(
            srCourseBlockUid = blockUidAndClazzUid.courseBlockUid,
            srClazzUid = blockUidAndClazzUid.clazzUid,
        )

        return if(existingStudentResultUid == 0L) {
            db.studentResultDao.insertListAsync(listOf(studentResult))
            201
        }else {
            db.studentResultDao.updateAsync(studentResult.copy(srUid = existingStudentResultUid))
            200
        }
    }


}