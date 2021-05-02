package com.ustadmobile.port.android.view

import android.os.Build
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isJavascriptEnabled
import androidx.test.espresso.web.webdriver.Locator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.screen.WebChunkScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@AdbScreenRecord("WebChunk Screen Test")
class WebChunkFragmentTest : TestCase() {

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


    @Before
    fun setup() {
        val targetEntry = ContentEntry()
        targetEntry.title = "tiempo de prueba"
        targetEntry.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        targetEntry.description = "todo el contenido"
        targetEntry.publisher = "CK12"
        targetEntry.author = "borrachera"
        targetEntry.primaryLanguageUid = 53
        targetEntry.leaf = true
        targetEntry.contentEntryUid = dbRule.repo.contentEntryDao.insert(targetEntry)

        container = Container()
        container.mimeType = "application/webchunk+zip"
        container.containerContentEntryUid = targetEntry.contentEntryUid
        container.containerUid = dbRule.repo.containerDao.insert(container)
        runBlocking {
            dbRule.repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    "/com/ustadmobile/app/android/counting-out-1-20-objects.zip",
                    ContainerAddOptions(temporaryFolder.newFolder().toDoorUri()))
        }
    }

    @AdbScreenRecord("given contentEntry when web view loads then show web chunk")
    @Test
    fun givenContentEntry_whenWebViewLoads_thenShowWebChunk() {

        Assume.assumeTrue(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)



        init{

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to container.containerContentEntryUid, UstadView.ARG_CONTAINER_UID to container.containerUid)) {
                WebChunkFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{
            WebChunkScreen{

                webView{
                    isDisplayed()
                    isJavascriptEnabled()
                    withElement(Locator.CSS_SELECTOR, "div[data-test-id=tutorial-page]"){

                    }
                }

            }
        }



    }

}