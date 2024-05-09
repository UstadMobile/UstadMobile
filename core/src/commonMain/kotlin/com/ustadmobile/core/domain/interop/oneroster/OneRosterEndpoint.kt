package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.Result as OneRosterResult
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterClass
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterResult

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

}