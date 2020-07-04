package com.ustadmobile.core.util

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.bindJndiForActiveEndpoint
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider
import javax.naming.InitialContext

/**
 * Use this Rule to simulate a client database. The databases will be wrapped with the Mockito
 * spy function and can therefor be used to verify calls, capture arguments, etc.
 */
class UmAppDatabaseClientRule(val account: UmAccount = UmAccount(42, "theanswer","", "http://localhost/"),
                              val useDbAsRepo: Boolean = false,
                              val contextProvider: () -> Any = {dummyContext} ): TestWatcher() {

    private var dbInternal: UmAppDatabase? = null

    private var repoInternal: UmAppDatabase? = null

    val db: UmAppDatabase
        get() = dbInternal ?: throw IllegalStateException("Rule not started!")

    val repo: UmAppDatabase
        get() = (if(useDbAsRepo) db else repoInternal) ?: throw IllegalStateException("Rule not started!")

    val accountLiveData: DoorMutableLiveData<UmAccount?> = DoorMutableLiveData(account)


    val diModule = DI.Module("UmAppDatabase") {
        bind<UmAppDatabase>(tag = TAG_DB) with provider { db }
        bind<UmAppDatabase>(tag = TAG_REPO) with provider { repo }
    }

    override fun starting(description: Description?) {
        val context = contextProvider()
        InitialContext().bindJndiForActiveEndpoint(account.endpointUrl!!)

        UmAccountManager.setActiveAccount(account, context)
        dbInternal = spy(UmAccountManager.getActiveDatabase(context).apply {
            clearAllTables()
        })
        repoInternal = spy(UmAccountManager.getRepositoryForActiveAccount(context))
        accountLiveData.setVal(account)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        dbInternal = null
        repoInternal = null
    }

    companion object {
        val dummyContext = Any()
    }

}