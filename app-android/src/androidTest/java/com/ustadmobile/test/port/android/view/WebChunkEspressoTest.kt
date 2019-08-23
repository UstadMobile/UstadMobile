package com.ustadmobile.test.port.android.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.port.android.view.WebChunkActivity
import com.ustadmobile.port.sharedse.util.UmZipUtils
import com.ustadmobile.test.port.android.UmAndroidTestUtil
import com.ustadmobile.test.port.android.UmAndroidTestUtil.readAllFilesInDirectory
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.*

@RunWith(AndroidJUnit4::class)
class WebChunkEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(WebChunkActivity::class.java, false, false)

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)

    internal var path = "/HomeView?/ContentEntryList?entryid=40/ContentEntryList?entryid=41/ContentEntryDetail?entryid=10/ContentEntryDetail?entryid=11/webChunk?"
    private var tmpDir: File? = null
    private var repo: UmAppDatabase? = null
    private var db: UmAppDatabase? = null
    private var dir: File? = null

    lateinit var context: Context


    fun getDb(): UmAppDatabase {
        context = InstrumentationRegistry.getInstrumentation().context
        db = UmAppDatabase.getInstance(context)
        db!!.clearAllTables()
        return UmAppDatabase.getInstance(context) //db!!.getUmRepository("https://localhost", "")
    }

    @Throws(IOException::class)
    fun createDummyContent() {
        repo = getDb()

        val contentDao = repo!!.contentEntryDao
        val containerDao = repo!!.containerDao

        val app = UmAppDatabase.getInstance(context)
        val statusDao = app.contentEntryStatusDao

        dir = Environment.getExternalStorageDirectory()
        tmpDir = Files.createTempDirectory("testWebChunk").toFile()
        tmpDir!!.mkdirs()

        val countingFolder = File(tmpDir, "counting-out")
        countingFolder.mkdirs()
        val chunkCountingOut = File(tmpDir, "counting-out.zip")
        unZipAndCreateManager(countingFolder, chunkCountingOut, 1L, 10,
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/counting-out-1-20-objects.zip"))


        val targetEntryStatusComplete = ContentEntryStatus()
        targetEntryStatusComplete.cesLeaf = true
        targetEntryStatusComplete.downloadStatus = JobStatus.COMPLETE
        targetEntryStatusComplete.totalSize = 1000
        targetEntryStatusComplete.bytesDownloadSoFar = 1000
        targetEntryStatusComplete.cesUid = 1L
        statusDao.insert(targetEntryStatusComplete)

        val targetEntry = ContentEntry()
        targetEntry.contentEntryUid = 1L
        targetEntry.title = "tiempo de prueba"
        targetEntry.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        targetEntry.description = "todo el contenido"
        targetEntry.publisher = "CK12"
        targetEntry.author = "borrachera"
        targetEntry.primaryLanguageUid = 53
        targetEntry.leaf = true
        contentDao.insert(targetEntry)


        val countingObjectsFolder = File(tmpDir, "counting-objects")
        countingObjectsFolder.mkdirs()
        val chunkcountingObjects = File(tmpDir, "counting-objects.zip")
        unZipAndCreateManager(countingObjectsFolder, chunkcountingObjects, 3L, 11,
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/counting-objects.zip"))

        val countingEntryStatusComplete = ContentEntryStatus()
        countingEntryStatusComplete.cesLeaf = true
        countingEntryStatusComplete.downloadStatus = JobStatus.COMPLETE
        countingEntryStatusComplete.totalSize = 1000
        countingEntryStatusComplete.bytesDownloadSoFar = 1000
        countingEntryStatusComplete.cesUid = 3
        statusDao.insert(countingEntryStatusComplete)

        val countingEntry = ContentEntry()
        countingEntry.contentEntryUid = 3
        countingEntry.title = "tiempo de prueba"
        countingEntry.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        countingEntry.description = "todo el contenido"
        countingEntry.publisher = "CK12"
        countingEntry.author = "borrachera"
        countingEntry.primaryLanguageUid = 345
        countingEntry.sourceUrl = "khan-id://x7d37671e"
        countingEntry.leaf = true
        contentDao.insert(countingEntry)


    }

    @Throws(IOException::class)
    private fun unZipAndCreateManager(countingFolder: File, chunkCountingOut: File, contentEntryUid: Long, containerUid: Int, resourceAsStream: InputStream?) {

        FileUtils.copyInputStreamToFile(
                resourceAsStream!!,
                chunkCountingOut)

        UmZipUtils.unzip(chunkCountingOut, countingFolder)
        val countingMap = HashMap<File, String>()
        readAllFilesInDirectory(countingFolder, countingMap)

        val container = Container()
        container.mimeType = "application/webchunk+zip"
        container.containerContentEntryUid = contentEntryUid
        container.containerUid = containerUid.toLong()
        repo!!.containerDao.insert(container)

        val manager = ContainerManager(container, db!!,
                repo!!, dir!!.absolutePath)
        runBlocking {
            countingMap.forEach {
                manager.addEntries(ContainerManager.FileEntrySource(it.component1(), it.component2()))
            }
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenServerOffline_whenKhanExerciseZippedIsOpened_WebviewLoads() {
        val launchActivityIntent = Intent()

        createDummyContent()

        UmAndroidTestUtil.setAirplaneModeEnabled(true)
        val b = Bundle()

        b.putString(WebChunkView.ARG_CONTAINER_UID, 10L.toString())
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, 1L.toString())
        b.putString(ARG_REFERRER, path)
        launchActivityIntent.putExtras(b)
        mActivityRule.launchActivity(launchActivityIntent)


        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


    @Test
    @Throws(IOException::class)
    fun givenServerOffline_whenNewKhanExerciseZippedIsOpened_WebviewLoads() {
        val launchActivityIntent = Intent()

        createDummyContent()

        UmAndroidTestUtil.setAirplaneModeEnabled(true)
        val b = Bundle()

        val countingFolder = File(tmpDir, "review")
        countingFolder.mkdirs()
        val chunkCountingOut = File(tmpDir, "review-out.zip")
        unZipAndCreateManager(countingFolder, chunkCountingOut, 1L, 12,
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/comparison-symbols-review.zip"))


        b.putString(WebChunkView.ARG_CONTAINER_UID, 12.toString())
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, 1L.toString())
        b.putString(ARG_REFERRER, path)
        launchActivityIntent.putExtras(b)
        mActivityRule.launchActivity(launchActivityIntent)

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }

    @Test
    @Throws(IOException::class)
    fun givenServerOffline_whenNewKhanArticleZippedIsOpened_WebviewLoads() {

        createDummyContent()
        val launchActivityIntent = Intent()

        createDummyContent()

        UmAndroidTestUtil.setAirplaneModeEnabled(true)
        val b = Bundle()

        b.putString(WebChunkView.ARG_CONTAINER_UID, 11.toString())
        b.putString(WebChunkView.ARG_CONTENT_ENTRY_ID, 1.toString())
        b.putString(ARG_REFERRER, path)
        launchActivityIntent.putExtras(b)
        mActivityRule.launchActivity(launchActivityIntent)

        // the webview looks for an element "questionController" which is the start button of plix.
        // This is only available once plix has fully loaded and displayed to the use
    }


}
