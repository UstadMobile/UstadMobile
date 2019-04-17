package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.test.core.impl.PlatformTestUtil
import org.junit.Before
import org.junit.Test

class TestDownloadJobItemManagerList {

    private val umAppDatabase: UmAppDatabase = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)

    @Before
    fun initDb() {
        umAppDatabase.clearAllTables()


        val rootContentEntry1 = ContentEntry("title", "desc", false, true)
        var downloadJob1 = DownloadJob()

    }

    @Test
    fun givenListOfDownloadJobItemManagers_whenExistingContentEntryStatusRequested_thenShouldCallbackWithValue() {

    }

    @Test
    fun givenActiveDownload_whenDownloadCompletes_thenShouldBeRemovedFromManagerList() {

    }

}