package com.ustadmobile.port.android.view

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Environment
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.IdlingRegistry
import androidx.test.rule.GrantPermissionRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertVideoContent
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils.copyInputStreamToFile
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalStdlibApi
class VideoContentFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)

    var container: Container? = null

    @Before
    fun setup() {
        val tmpDir = UmFileUtilSe.makeTempDir("testVideoPlayer",
                "" + System.currentTimeMillis())

        val videoFile = File(tmpDir, "video1.webm")
        val audioTempFile = File(tmpDir, "audio.c2")
        val srtTmpFile = File(tmpDir, "subtitle-english.srt")
        val germanTmpFile = File(tmpDir, "subtitle-Deutsch.srt")

        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/video1.webm")!!,
                videoFile)

        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/video1-codec2-version2.c2")!!,
                audioTempFile)
        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/subtitle-english.srt")!!,
                srtTmpFile)

        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/subtitle-Deutsch.srt")!!,
                germanTmpFile)

        val dir = Environment.getExternalStorageDirectory()

        runBlocking {
            container = dbRule.db.insertVideoContent()
        }

        val manager = ContainerManager(container!!, dbRule.db,
                dbRule.db, dir.absolutePath)

        runBlocking {
            manager.addEntries(ContainerManager.FileEntrySource(videoFile, "video1.webm"),
                    ContainerManager.FileEntrySource(audioTempFile, "audio.c2"),
                    ContainerManager.FileEntrySource(srtTmpFile, "subtitle-english.srt"),
                    ContainerManager.FileEntrySource(germanTmpFile, "subtitle-Deutsch.srt"))
        }

    }

    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntry() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(ARG_CONTENT_ENTRY_UID to container!!.containerContentEntryUid, ARG_CONTAINER_UID to container!!.containerUid)) {
            VideoContentFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(fragmentScenario.letOnFragment { it }).also {
            IdlingRegistry.getInstance().register(it)
        }

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }

}