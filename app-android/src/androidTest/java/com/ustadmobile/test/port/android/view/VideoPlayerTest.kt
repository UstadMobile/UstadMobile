package com.ustadmobile.test.port.android.view
// Disabled 10/June/2020 by Mike: these tests need to be updated
//import android.Manifest
//import android.content.Intent
//import android.os.Bundle
//import android.os.Environment
//import androidx.test.espresso.intent.rule.IntentsTestRule
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.rule.GrantPermissionRule
//import androidx.test.runner.AndroidJUnit4
//import com.ustadmobile.core.container.ContainerManager
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.view.UstadView
//import com.ustadmobile.lib.db.entities.Container
//import com.ustadmobile.lib.db.entities.ContentEntry
//import com.ustadmobile.port.android.view.VideoPlayerActivity
//import com.ustadmobile.port.sharedse.util.UmFileUtilSe
//import com.ustadmobile.test.port.android.UmAndroidTestUtil
//import kotlinx.coroutines.runBlocking
//import org.apache.commons.io.FileUtils
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.io.File
//import java.io.IOException
//
////@RunWith(AndroidJUnit4::class)
//class VideoPlayerTest {
//
//    @get:Rule
//    var mActivityRule = IntentsTestRule(VideoPlayerActivity::class.java, false, false)
//    @get:Rule
//    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)
//    private var containerUid: Long = 0
//
//
//    val db: UmAppDatabase
//        get() {
//            val context = InstrumentationRegistry.getInstrumentation().context
//            val db = UmAppDatabase.getInstance(context)
//            db.clearAllTables()
//            return UmAppDatabase.getInstance(context)//db.getUmRepository("https://localhost", "")
//        }
//
//    @Throws(IOException::class)
//    fun createDummyContent() {
//        val db = db
//        val repo = db
//        val contentDao = repo.contentEntryDao
//        val containerDao = repo.containerDao
//
//
//    }
//
//
//    //@Test
//    @Throws(IOException::class)
//    fun givenServerOffline_whenPlixZippedIsOpened_WebviewLoads() {
//        val launchActivityIntent = Intent()
//
//        createDummyContent()
//
//        UmAndroidTestUtil.setAirplaneModeEnabled(true)
//        val b = Bundle()
//        b.putString(UstadView.ARG_CONTAINER_UID, containerUid.toString())
//        b.putString(UstadView.ARG_CONTENT_ENTRY_UID, 14L.toString())
//        launchActivityIntent.putExtras(b)
//        mActivityRule.launchActivity(launchActivityIntent)
//
//        //  mHomeActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        // the webview looks for an element "questionController" which is the start button of plix.
//        // This is only available once plix has fully loaded and displayed to the user
//    }
//
//
//}
