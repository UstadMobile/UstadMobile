package com.ustadmobile.port.android.view

import android.content.Context
import android.content.res.Configuration
import android.media.session.PlaybackState.STATE_BUFFERING
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.ui.PlayerView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.io.ext.addEntryToContainerFromResource
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.port.android.screen.VideoContentScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.port.android.util.waitUntilOnFragment
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertVideoContent
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DIAware


@AdbScreenRecord("Video Content Screen Test")
class VideoContentFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    lateinit var container: Container

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()


    @Suppress("BlockingMethodInNonBlockingContext")
    @Before
    fun setup() {
        val localDi = (ApplicationProvider.getApplicationContext<Context>() as DIAware).di
        runBlocking {
            container = dbRule.repo.insertVideoContent()
            dbRule.repo.addEntryToContainerFromResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/app/android/video.mp4", "video.mp4",
                    localDi, ContainerAddOptions(temporaryFolder.newFolder().toDoorUri()))
        }
    }

    @AdbScreenRecord("given video content when rotated then show video without description")
    @Test
    fun givenVideoContent_whenRotatedWithConfigChanges_thenShowVideoWithoutDescription() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(
                        ARG_CONTENT_ENTRY_UID to container.containerContentEntryUid,
                        ARG_CONTAINER_UID to container.containerUid)) {
            VideoContentFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            VideoContentScreen{

                desc {
                    isDisplayed()
                }
                exoPlayButton{
                    click()
                }
                
                runBlocking {
                    fragmentScenario.waitUntilOnFragment(5000,
                        {
                            it.view?.findViewById<PlayerView>(R.id.activity_video_player_view)
                                ?.player?.playbackState
                        }) { playState -> playState == STATE_BUFFERING || playState == STATE_READY }
                }

                fragmentScenario.onFragment {
                    val videoPlayer = it.view?.findViewById<PlayerView>(R.id.activity_video_player_view)
                    val playState = videoPlayer?.player?.playbackState
                    Assert.assertTrue("player is playing",
                        playState == STATE_BUFFERING || playState == STATE_READY)
                }

                fragmentScenario.letOnFragment {
                    it.onConfigurationChanged(Configuration().apply {
                        orientation = Configuration.ORIENTATION_LANDSCAPE
                    })
                }

                desc{
                    isGone()
                }
                playerControls{
                    isGone()
                }

                runBlocking {
                    var statement: ContentEntryStatementScoreProgress? = null
                    while(statement == null){
                        statement = dbRule.db.statementDao
                                .getBestScoreForContentForPerson(
                                        container.containerContentEntryUid, dbRule.account.personUid)
                    }
                    Assert.assertEquals("progress started since user pressed play", 0,
                            statement.progress)
                }

            }

        }

    }

}