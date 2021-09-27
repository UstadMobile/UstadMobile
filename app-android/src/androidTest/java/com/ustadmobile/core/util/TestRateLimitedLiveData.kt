package com.ustadmobile.core.util

import androidx.lifecycle.Observer
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.test.port.android.util.getApplicationDi
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Test RateLimitedLiveData on Android itself
 */
class TestRateLimitedLiveData {

    @Test
    fun givenRateLimitedLiveData_shouldWOrk() {
        val appDi = getApplicationDi()
        val testEndpoint = appDi.direct.instance<UstadAccountManager>().activeEndpoint
        val db: UmAppDatabase by appDi.on(testEndpoint).instance(tag = DoorTag.TAG_DB)
        val contentJobItem = ContentJobItem().apply {
            cjiItemTotal = 100
            cjiUid = runBlocking { db.contentJobItemDao.insertJobItem(this@apply) }
        }
        val rateLimitedLiveData = RateLimitedLiveData<ContentJobItem?>(db, listOf("ContentJobItem")) {
            db.contentJobItemDao.findByUidAsync(contentJobItem.cjiUid)
        }

        runBlocking {
            val completable = CompletableDeferred<Boolean>()
            val observer = Observer<ContentJobItem?> {
                if((it?.cjiItemProgress ?: 0) >= 50)
                    completable.complete(true)
            }

            launch(Dispatchers.Main) {
                rateLimitedLiveData.observeForever(observer)
            }


            val startTime = systemTimeInMillis()

            db.contentJobItemDao.updateItemProgress(contentJobItem.cjiUid, 50, 100)

            withTimeout(5000) {
                completable.await()
            }
            Assert.assertTrue("Completed in less than 4000ms (e.g. not just via timeout)",
                systemTimeInMillis() - startTime < 4000)

            val updatedItem = db.contentJobItemDao.findByUidAsync(contentJobItem.cjiUid)
            Assert.assertEquals("Job item was updated", 50L,
                updatedItem?.cjiItemProgress)

            withContext(Dispatchers.Main) {
                rateLimitedLiveData.removeObserver(observer)
            }
        }
    }

}