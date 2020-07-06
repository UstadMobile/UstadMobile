package com.ustadmobile.test.rules

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.SyncNode
import com.ustadmobile.lib.db.entities.UmAccount
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.random.Random

class UmAppDatabaseAndroidClientRule(val account: UmAccount = UmAccount(42, "theanswer","", "http://localhost/"),
                                     val useDbAsRepo: Boolean = false) : TestWatcher()  {

    private var dbInternal: UmAppDatabase? = null

    private var repoInternal: UmAppDatabase? = null

    val db: UmAppDatabase
        get() = dbInternal ?: throw IllegalStateException("Rule not started!")

    val repo: UmAppDatabase
        get() = (if(useDbAsRepo) db else repoInternal) ?: throw IllegalStateException("Rule not started!")

    val accountLiveData: DoorMutableLiveData<UmAccount?> = DoorMutableLiveData(account)


    override fun starting(description: Description?) {
        val context: Context = getApplicationContext()
        UmAccountManager.setActiveAccount(account, context)
        dbInternal = UmAccountManager.getActiveDatabase(context).apply {
            clearAllTables()
            val _nodeId = Random.nextInt(1, Int.MAX_VALUE)
            syncNodeDao.replace(SyncNode(_nodeId, false))
        }


        repoInternal = UmAccountManager.getRepositoryForActiveAccount(context)
    }


    override fun finished(description: Description?) {
        super.finished(description)
        dbInternal = null
        repoInternal = null
    }

}