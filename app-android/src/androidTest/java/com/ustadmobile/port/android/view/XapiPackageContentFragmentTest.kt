package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
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
import com.ustadmobile.port.android.screen.XapiContentScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


@AdbScreenRecord("Xapi package content screen test")
class XapiPackageContentFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    private var contentEntry: ContentEntry? = null

    private lateinit var container: Container

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Suppress("BlockingMethodInNonBlockingContext")
    @Before
    fun setUp(){
        contentEntry = ContentEntry().apply {
            leaf = true
            contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry?.contentEntryUid!!
            containerUid = dbRule.repo.containerDao.insert(this)
        }

        runBlocking {
            dbRule.repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                "/com/ustadmobile/app/android/XapiPackage-JsTetris_TCAPI.zip",
                    ContainerAddOptions(temporaryFolder.newFolder().toDoorUri()))
        }
    }

    @AdbScreenRecord("Given valid xapi package content when created should be loaded to the view")
    @Test
    fun givenValidXapiPackage_whenCreated_shouldLoadToTheView(){

        init{

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_CONTAINER_UID to container?.containerUid,
                            UstadView.ARG_CONTENT_ENTRY_UID to contentEntry?.contentEntryUid)) {
                XapiPackageContentFragment().also { fragment ->
                    fragment.installNavController(systemImplNavRule.navController)
                }
            }
        }.run{

            XapiContentScreen{
                //Timeout increased due to flakey failure on Jenkins 4/Jan/2021. Added reset 5/Jan/20201
                flakySafely(timeoutMs = 15 * 1000) {
                    webView{
                        reset()
                        withElement(Locator.TAG_NAME, "a"){
                            hasText("Tin Can Home")
                        }
                    }
                }
            }


        }

    }


}
