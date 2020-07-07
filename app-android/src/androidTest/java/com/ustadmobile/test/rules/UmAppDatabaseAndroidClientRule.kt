package com.ustadmobile.test.rules

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SyncNode
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.impl.BaseUstadApp
import com.ustadmobile.port.android.impl.UstadApp
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.random.Random

class UmAppDatabaseAndroidClientRule(val account: UmAccount = UmAccount(42, "theanswer","", "http://localhost/"),
                                     val useDbAsRepo: Boolean = false) : TestWatcher()  {

    private var dbInternal: UmAppDatabase? = null

    private var repoInternal: UmAppDatabase? = null

    val db: UmAppDatabase
        get() = dbInternal ?: throw IllegalStateException("Rule not started!")

    val repo: UmAppDatabase
        get() = (if(useDbAsRepo) db else repoInternal) ?: throw IllegalStateException("Rule not started!")


    override fun starting(description: Description?) {
        val di = (getApplicationContext<BaseUstadApp>() as UstadApp).di

        val accountManager: UstadAccountManager by di.instance()
        accountManager.activeAccount = account

        dbInternal = di.direct.on(accountManager.activeAccount).instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).apply {
            clearAllTables()
            val _nodeId = Random.nextInt(1, Int.MAX_VALUE)
            syncNodeDao.replace(SyncNode(_nodeId, false))
        }

        repoInternal = if(useDbAsRepo) dbInternal else di.direct.on(accountManager.activeAccount).instance<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO)
    }


    override fun finished(description: Description?) {
        super.finished(description)
        dbInternal = null
        repoInternal = null
    }

}