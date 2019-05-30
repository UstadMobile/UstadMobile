package com.ustadmobile.port.android.view


import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException


class XapiPackageContentActivityEspressoTest {

    @Rule
    var mActivityRule = IntentsTestRule(XapiPackageContentActivity::class.java, false, false)


    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    private var tempXapiPackageContainer: UmFileUtilSe.TempZipContainer? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        db = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext())
        repo = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext()) //db!!.getRepository("http://localhost/dummy/", "")
        db!!.clearAllTables()

        val storageDir = ContextCompat.getExternalFilesDirs(
                InstrumentationRegistry.getTargetContext(), null)[0]
        val containerTmpDir = File(storageDir, "XapiPackageCOntentActivityEspressoTest." + System.currentTimeMillis())
        containerTmpDir.mkdirs()

        tempXapiPackageContainer = UmFileUtilSe.makeTempContainerFromClassResource(db!!, repo!!,
                "/com/ustadmobile/port/android/view/XapiPackage-JsTetris_TCAPI.zip",
                containerTmpDir)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        if (tempXapiPackageContainer != null)
            UmFileUtilSe.deleteRecursively(tempXapiPackageContainer!!.containerFileDir)
    }

    fun launchActivity() {
        val launchIntent = Intent()
        launchIntent.putExtra(XapiPackageContentView.ARG_CONTAINER_UID,
                tempXapiPackageContainer!!.container.containerUid.toString())
        mActivityRule.launchActivity(launchIntent)
    }

    @Test
    fun givenValidXapiZip_whenCreated_thenShouldShowContents() {
        launchActivity()
        SystemClock.sleep(1000)
       // onWebView().check<Document>(webContent(hasElementWithId("tetris")))
    }


}
