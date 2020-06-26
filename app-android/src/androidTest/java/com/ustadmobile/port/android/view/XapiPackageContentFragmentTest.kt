package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
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
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File


@AdbScreenRecord("Xapi package content screen test")
class XapiPackageContentFragmentTest {

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
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context: Application = ApplicationProvider.getApplicationContext()

    private val bleServiceHelper = NetworkManagerBleServiceHelper()


    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    private lateinit var testFile: File

    private var contentEntry: ContentEntry? = null

    private var container: Container? = null

    private lateinit var containerTmpDir: File



    @Before
    fun setUp(){

        contentEntry = ContentEntry().apply {
            leaf = true
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry?.contentEntryUid!!
            containerUid = dbRule.db.containerDao.insert(this)
        }
        containerTmpDir = UmFileUtilSe.makeTempDir("containerTmpDir",
                "${System.currentTimeMillis()}")
        testFile = File.createTempFile("epubcontent", "epubfile", containerTmpDir)
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/XapiPackage-JsTetris_TCAPI.zip")
        testFile.outputStream().use { input?.copyTo(it) }

        val containerManager = ContainerManager(container!!, dbRule.db, dbRule.repo,containerTmpDir.absolutePath)
        addEntriesFromZipToContainer(testFile.absolutePath, containerManager)
        bleServiceHelper.bindService()
    }

    @AdbScreenRecord("Given valid xapi package content when created should be loaded to the view")
    @Test
    fun givenValidXapiPackage_whenCreated_shouldLoadToTheView(){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CONTAINER_UID to container?.containerUid,
                        UstadView.ARG_CONTENT_ENTRY_UID to contentEntry?.contentEntryUid)) {
            XapiPackageContentFragment().also { fragment ->
                fragment.installNavController(systemImplNavRule.navController)
                fragment.networkManagerProvider = bleServiceHelper
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //verify that there is a header link with Home title
        onWebView().withElement(findElement(Locator.TAG_NAME, "a"))
                .check(webMatches(getText(), containsString("Tin Can Home")))

        //clean up
        testFile.deleteOnExit()
        containerTmpDir.deleteOnExit()

    }
}