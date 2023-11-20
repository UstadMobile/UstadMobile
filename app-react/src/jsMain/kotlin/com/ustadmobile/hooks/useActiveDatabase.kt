package com.ustadmobile.hooks

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import react.useMemo
import react.useRequiredContext

/**
 * Use the active database as per the accountmanager
 */
fun useActiveDatabase(tag: Int = DoorTag.TAG_DB) : UmAppDatabase {
    val di = useRequiredContext(DIContext)
    val accountManager: UstadAccountManager = useMemo(dependencies = emptyArray()) {
        di.direct.instance()
    }

    val db: UmAppDatabase = useMemo(accountManager.activeEndpoint) {
        di.direct.on(accountManager.currentAccount).instance(tag = tag)
    }

    return db
}