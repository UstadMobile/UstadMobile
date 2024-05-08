package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.interop.oneroster.model.Clazz
import com.ustadmobile.core.domain.interop.oneroster.model.toOneRosterClass

/**
 *  Implements OneRoster Endpoints by running a database query and converting from database entities
 *  to the OneRoster model.
 */
class OneRosterEndpoint(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
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


}