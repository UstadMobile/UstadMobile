package com.ustadmobile.port.android.view

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_CONNECTION_ERR
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
public class RepoLoadingStatusViewRoboElectricTest {

    lateinit var repoLoadingStatusView: RepoLoadingStatusView

    private val context = RuntimeEnvironment.application.applicationContext

    @Before
    fun setup() {
        repoLoadingStatusView = RepoLoadingStatusView(context)
    }


    @Test
    fun givenLaunchedApplication_whenLoadingFromEitherCloudOrMirror_thenShouldShowProgressBarAndRightIconAndMessage(){
        repoLoadingStatusView.onChanged(RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADING_CLOUD))
        val iconView = repoLoadingStatusView.findViewById<ImageView>(R.id.statusViewImageInner)
        val messageView = repoLoadingStatusView.findViewById<TextView>(R.id.statusViewTextInner)
        val progressView = repoLoadingStatusView.findViewById<ProgressBar>(R.id.statusViewProgress)

        assertEquals("Correct loading from cloud Image resource was set successfully",
                R.drawable.ic_cloud_download_black_24dp,iconView.tag)

        assertEquals("Correct loading from cloud message was set successfully",
                context.getString(R.string.repo_loading_status_loading_cloud),  messageView.text)

        assertTrue("Loading progress is being shown", progressView.visibility == View.VISIBLE)


    }

    @Test
    fun givenApplicationLaunched_whenLoadingCompletedWithNoDataToShow_shouldHideProgressAndShowCorrectIconAndMessage(){
        repoLoadingStatusView.onChanged(RepositoryLoadHelper.RepoLoadStatus(STATUS_FAILED_CONNECTION_ERR))
        val iconView = repoLoadingStatusView.findViewById<ImageView>(R.id.statusViewImageInner)
        val messageView = repoLoadingStatusView.findViewById<TextView>(R.id.statusViewText)
        val progressView = repoLoadingStatusView.findViewById<ProgressBar>(R.id.statusViewProgress)

        assertEquals("Correct connection error while loading Image resource was set successfully",
                R.drawable.ic_error_black_24dp, iconView.tag)

        assertEquals("Correct connection error while loading message was set successfully",
                context.getString(R.string.repo_loading_status_failed_connection_error),messageView.text)

        assertTrue("Loading progress was hidden", progressView.visibility == View.GONE)
    }

    @Test
    fun getApplicationLaunched_whenCustomMessageAndIconAreSetToBeDisplayedOnEmptyView_thenShouldDisplayThem(){
       repoLoadingStatusView.emptyStatusText = R.string.empty_state_libraries
        repoLoadingStatusView.emptyStatusImage = R.drawable.ic_folder_black_24dp
        repoLoadingStatusView.onChanged(RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADED_NODATA))

        val messageView = repoLoadingStatusView.findViewById<TextView>(R.id.statusViewText)
        val iconView = repoLoadingStatusView.findViewById<ImageView>(R.id.statusViewImageInner)

        assertEquals("Custom message was set successfully",
                context.getString(R.string.empty_state_libraries),  messageView.text)

        assertEquals("Custom Image resource was set successfully",
                R.drawable.ic_folder_black_24dp,iconView.tag)

    }


    @Test
    fun givenLaunchedApplication_whenLoadingCompleteAndThereIsDataToShow_thenShouldHideTheStatusView(){
        repoLoadingStatusView.onFirstItemLoaded()
        assertTrue("Status view was hidden successfully", repoLoadingStatusView.visibility == View.GONE)
    }
}