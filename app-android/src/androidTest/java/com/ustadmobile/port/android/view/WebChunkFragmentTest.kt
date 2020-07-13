package com.ustadmobile.port.android.view

import android.Manifest
import android.os.Build
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isJavascriptEnabled
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.Locator
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
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import kotlinx.android.synthetic.main.fragment_web_chunk.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FileUtils.copyInputStreamToFile
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assume
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

    lateinit var container: Container

    private val WAIT_TIME = 2000L

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
        container.mimeType = "application/webchunk+zip"
        container.containerContentEntryUid = targetEntry.contentEntryUid
        container.containerUid = dbRule.db.containerDao.insert(container)

        val containerManager = ContainerManager(container, dbRule.db, dbRule.repo,
                tmpDir.absolutePath)
        addEntriesFromZipToContainer(chunkCountingOut.absolutePath, containerManager)

    }

    @AdbScreenRecord("given contentEntry when web view loads then show web chunk")
    @Test
    fun givenContentEntry_whenWebViewLoads_thenShowWebChunk() {

        Assume.assumeTrue(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to container.containerContentEntryUid, UstadView.ARG_CONTAINER_UID to container.containerUid)) {
            WebChunkFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)


        repeat(5) {
            try {
                onWebView(allOf(isDisplayed(), isJavascriptEnabled()))
                        .withElement(findElement(Locator.CSS_SELECTOR,
                                "div[data-test-id=tutorial-page]"))
            } catch (io: RuntimeException) {
                if (it == 5) {
                    throw Exception()
                }
                Thread.sleep(WAIT_TIME)
            }
        }


    }

}