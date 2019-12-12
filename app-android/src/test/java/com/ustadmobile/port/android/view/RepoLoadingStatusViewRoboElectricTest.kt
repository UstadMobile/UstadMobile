package com.ustadmobile.port.android.view

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.toughra.ustadmobile.R
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
public class RepoLoadingStatusViewRoboElectricTest {

    lateinit var repoLoadingStatusView: RepoLoadingStatusView

    private val context = RuntimeEnvironment.application.applicationContext

    @Before
    public fun setup() {
        repoLoadingStatusView = RepoLoadingStatusView(context)
    }


    @Test
    fun givenLaunchedApplication_whenLoadingFromCloud_thenShouldShowCloudLoadingMessageAndIcon(){
        repoLoadingStatusView.onLoadStatusChanged(STATUS_LOADING_CLOUD,"")
        val iconView = repoLoadingStatusView.findViewById<ImageView>(R.id.statusViewImageInner)
        val messageView = repoLoadingStatusView.findViewById<TextView>(R.id.statusViewText)
        val progressView = repoLoadingStatusView.findViewById<ProgressBar>(R.id.statusViewProgress)

        assertEquals("Correct loading from cloud Image resource was set successfully", iconView.tag,
                R.drawable.ic_cloud_download_black_24dp)

        assertEquals("Correct loading from cloud message was set successfully", messageView.text,
                context.getString(R.string.repo_loading_status_loading_cloud))

        assertTrue("Loading progress is being shown", progressView.visibility == View.VISIBLE)


    }


    @Test
    fun givenLaunchedApplication_whenLoadingFromMirror_thenShouldShowMirrorLoadingMessageAndIcon(){

    }

    @Test
    fun givenLaunchedApplication_whenLoadedAndNoDataFound_shouldShowEmptyStateIconAndMessage(){

    }

    @Test
    fun givenApplicationLaunched_whenLoadedAndDataFound_shouldHideStatusView(){

    }

    @Test
    fun givenApplicationLaunched_whenLoadingAndFailed_shouldShowConnectionErrorIconAndMessage(){

    }

    @Test
    fun givenApplicationLaunched_whenLoadingAndFoundNoPeers_shouldShowNoPeersFoundMessageAndIcon(){

    }
}