package com.ustadmobile.core.schedule

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class ClazzLogCreatorManagerJvm(override val di : DI): ClazzLogCreatorManager, DIAware {

    override fun requestClazzLogCreation(clazzUidFilter: Long, endpointUrl: String, fromTime: Long, toTime: Long) {
        val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = TAG_DB)
        db.createClazzLogs(fromTime, toTime, clazzUidFilter, false)
    }
}