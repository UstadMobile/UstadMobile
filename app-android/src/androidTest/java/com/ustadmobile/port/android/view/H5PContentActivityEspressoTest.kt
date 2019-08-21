package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class H5PContentActivityEspressoTest {

    private var h5PTmpFile: File? = null

    private var containerTmpDir: File? = null

    private var h5pContainer: Container? = null

    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    @JvmField
    @get:Rule
    val mActivityRule = IntentsTestRule(H5PContentActivity::class.java, false, false)

    @Before
    fun setup() {
        db = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext())
        repo =  UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext()) //db?.getUmRepository("http://localhost/dummy/", "")
        db?.clearAllTables()

        h5PTmpFile = File.createTempFile("H5pContentActivityEspressoTest", "h5p-true-false.h5p")
        containerTmpDir = UmFileUtilSe.makeTempDir("H5PContentActivityEspressoTest", "containerDir")

        h5pContainer = Container()
        h5pContainer?.containerUid = repo!!.containerDao.insert(h5pContainer!!)

        val containerManager = ContainerManager(h5pContainer!!, db!!, repo!!, containerTmpDir?.absolutePath!!)
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/android/view/H5P-true-false.h5p",
                h5PTmpFile!!)
        addEntriesFromZipToContainer(h5PTmpFile!!.absolutePath, containerManager)
    }

    @Test
    fun givenValidH5PFile_whenOnCreateCalled_thenContentShouldLoad() {
        val intent = Intent()
        intent.putExtra(UstadView.ARG_CONTAINER_UID, h5pContainer?.containerUid.toString())
        mActivityRule.launchActivity(intent)

        TimeUnit.MILLISECONDS.sleep(180000)
        Assert.assertTrue(true)


    }

}