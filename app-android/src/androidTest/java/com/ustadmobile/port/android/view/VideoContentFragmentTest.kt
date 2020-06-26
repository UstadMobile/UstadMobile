package com.ustadmobile.port.android.view

import android.Manifest
import android.content.res.Configuration
import android.media.session.PlaybackState.STATE_BUFFERING
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import com.google.android.exoplayer2.Player.STATE_READY
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.insertVideoContent
import kotlinx.android.synthetic.main.fragment_video_content.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils.copyInputStreamToFile
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File


@AdbScreenRecord("Video Content Screen Test")
@ExperimentalStdlibApi
class VideoContentFragmentTest {

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
        val tmpDir = UmFileUtilSe.makeTempDir("testVideoPlayer",
                "" + System.currentTimeMillis())

        val videoFile = File(tmpDir, "video.mp4")

        copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/video.mp4")!!,
                videoFile)

        runBlocking {
            container = dbRule.db.insertVideoContent()
            val manager = ContainerManager(container!!, dbRule.db,
                    dbRule.db, tmpDir.absolutePath)
            manager.addEntries(ContainerManager.FileEntrySource(videoFile, "video.mp4"))
        }
    }

    @AdbScreenRecord("given video content when rotated then show video without description")
    @Test
    fun givenVideoContent_whenRotatedWithConfigChanges_thenShowVideoWithoutDescription() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(ARG_CONTENT_ENTRY_UID to container!!.containerContentEntryUid, ARG_CONTAINER_UID to container!!.containerUid)) {
            VideoContentFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.activity_video_player_description))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        onView(allOf(withId(R.id.exo_play),
                isDescendantOfA(withId(R.id.player_view_controls))))
                .perform(click())

        fragmentScenario.letOnFragment {
            val playState = it.activity_video_player_view.player?.playbackState
            Assert.assertTrue("player is playing", playState == STATE_BUFFERING || playState == STATE_READY)
        }

        fragmentScenario.letOnFragment {
            it.onConfigurationChanged(Configuration().apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            })
        }

        onView(withId(R.id.activity_video_player_description))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.player_view_controls))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

    }

}