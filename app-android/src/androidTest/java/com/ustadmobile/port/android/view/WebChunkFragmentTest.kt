package com.ustadmobile.port.android.view

import android.Manifest
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.rule.GrantPermissionRule
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FileUtils.copyInputStreamToFile
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@AdbScreenRecord("WebChunk Screen Test")
@ExperimentalStdlibApi
class WebChunkFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)

    var container: Container? = null

    @Before
    fun setup() {

        val tmpDir = UmFileUtilSe.makeTempDir("testWebChunk",
                "" + System.currentTimeMillis())

        val chunkCountingOut = File(tmpDir, "counting-out.zip")

        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/counting-out-1-20-objects.zip"),
                chunkCountingOut)

        val targetEntry = ContentEntry()
        targetEntry.title = "tiempo de prueba"
        targetEntry.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        targetEntry.description = "todo el contenido"
        targetEntry.publisher = "CK12"
        targetEntry.author = "borrachera"
        targetEntry.primaryLanguageUid = 53
        targetEntry.leaf = true
        targetEntry.contentEntryUid = dbRule.db.contentEntryDao.insert(targetEntry)

        container = Container()
        container?.mimeType = "application/webchunk+zip"
        container?.containerContentEntryUid = targetEntry.contentEntryUid
        container?.containerUid = dbRule.db.containerDao.insert(container!!)

        val containerManager = ContainerManager(container!!, dbRule.db, dbRule.repo,
                tmpDir.absolutePath)
        addEntriesFromZipToContainer(chunkCountingOut.absolutePath, containerManager)

    }

    @Test
    fun test(){

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to container!!.containerContentEntryUid, UstadView.ARG_CONTAINER_UID to container!!.containerUid)) {
            WebChunkFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)




    }

}