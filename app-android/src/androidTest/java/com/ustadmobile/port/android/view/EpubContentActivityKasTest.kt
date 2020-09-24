package com.ustadmobile.port.android.view

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.text.KTextView
import com.agoda.kakao.web.KWebView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.screen.EpubScreen
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File


@AdbScreenRecord("KAS Epub content screen test")
@RunWith(AndroidJUnit4::class)
class EpubContentActivityKasTest : TestCase() {

    private lateinit var container: Container

    private lateinit var containerTmpDir: File

    @JvmField
    @Rule
    val tempFileRule = TemporaryFolder()

    private val context: Application = ApplicationProvider.getApplicationContext()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @Before
    fun setUp() {
        val contentEntry = ContentEntry().apply {
            leaf = true
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = 1000
            dbRule.db.containerDao.insert(this)
        }
        containerTmpDir = tempFileRule.newFolder("epubContent${System.currentTimeMillis()}")
        val testFile = tempFileRule.newFile("test${System.currentTimeMillis()}.epub")
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/test.epub")
        testFile.outputStream().use { input?.copyTo(it) }

        val containerManager = ContainerManager(container, dbRule.db, dbRule.repo,
                containerTmpDir.absolutePath)
        addEntriesFromZipToContainer(testFile.absolutePath, containerManager)
    }

    @AdbScreenRecord("Given valid epub content when created should be loaded to the view")
    @Test
    fun givenValidEpubContent_whenCreated_shouldBeLoadedToTheView() {
        val intent = Intent(context, EpubContentActivity::class.java)
        intent.putExtra(UstadView.ARG_CONTAINER_UID, container.containerUid.toString())
        val activityScenario = ActivityScenario.launch<EpubContentActivity>(intent)
        activityScenario.clickOptionMenu(R.id.menu_epub_content_showtoc)

        EpubScreen {

            epubTitle {
                hasText("ರುಮ್ನಿಯಾ")
                isDisplayed()
            }
            activityScenario.clickOptionMenu(R.id.menu_epub_content_showtoc)

            recycler {

                firstChild<EpubScreen.EpubPage> {
                    KWebView {
                        withTag("1.xhtml")
                        withId(R.id.epub_contentview)
                    }.invoke {
                        ViewMatchers.isDisplayed()
                        ViewMatchers.isJavascriptEnabled()
                        withElement(Locator.CLASS_NAME, "authors") {
                            hasText("Rukmini Banerji")
                        }
                    }
                }
            }

        }
    }

    @AdbScreenRecord("Given valid epub content opened when table of content item is clicked should be loaded to the view")
    @Test
    fun givenValidEpubContentOpened_whenTableOfContentItemIsClicked_shouldLoadThatItemIntoTheView() {
        val intent = Intent(context, EpubContentActivity::class.java)
        intent.putExtra(UstadView.ARG_CONTAINER_UID, container.containerUid.toString())
        val activityScenario = ActivityScenario.launch<EpubContentActivity>(intent)
        activityScenario.clickOptionMenu(R.id.menu_epub_content_showtoc)

        EpubScreen {

            epubTitle {
                hasText("ರುಮ್ನಿಯಾ")
                isDisplayed()
            }

            KTextView {
                withId(R.id.expandedListItem)
                withText("Page 7")
            } perform {
                isDisplayed()
                click()
            }

            toolBarTitle{
                hasDescendant { withText("Page 7") }
                isDisplayed()
            }


        }
    }


}