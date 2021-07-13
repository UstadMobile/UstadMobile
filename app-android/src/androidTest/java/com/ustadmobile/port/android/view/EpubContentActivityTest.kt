package com.ustadmobile.port.android.view

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.webdriver.Locator
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.web.KWebView
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
import com.ustadmobile.port.android.screen.EpubScreen
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


@AdbScreenRecord("Epub content screen test")
class EpubContentActivityTest : TestCase() {

    private lateinit var container: Container

    private lateinit var containerTmpDir: File

    private lateinit var contentEntry: ContentEntry

    @JvmField
    @Rule
    val tempFileRule = TemporaryFolder()

    private val context: Application = ApplicationProvider.getApplicationContext()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @Before
    fun setUp() {
        contentEntry = ContentEntry().apply {
            leaf = true
            contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = 1000
            dbRule.repo.containerDao.insert(this)
        }
        containerTmpDir = tempFileRule.newFolder("epubContent${System.currentTimeMillis()}")

        runBlocking {
            dbRule.repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                "/com/ustadmobile/app/android/test.epub",
                ContainerAddOptions(storageDirUri = containerTmpDir.toDoorUri()))
        }

    }

    @AdbScreenRecord("Given valid epub content when created should be loaded to the view")
    @Test
    fun givenValidEpubContent_whenCreated_shouldBeLoadedToTheView() {
        val intent = Intent(context, EpubContentActivity::class.java)
        intent.putExtra(UstadView.ARG_CONTAINER_UID, container.containerUid.toString())
        intent.putExtra(UstadView.ARG_CONTENT_ENTRY_UID, contentEntry.contentEntryUid.toString())
        val activityScenario = ActivityScenario.launch<EpubContentActivity>(intent)
        activityScenario.clickOptionMenu(R.id.menu_epub_content_showtoc)

        init {
            
        }.run {

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
                            reset()
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


    }

    @AdbScreenRecord("Given valid epub content opened when table of content item is clicked should be loaded to the view")
    @Test
    fun givenValidEpubContentOpened_whenTableOfContentItemIsClicked_shouldLoadThatItemIntoTheView() {
        val intent = Intent(context, EpubContentActivity::class.java)
        intent.putExtra(UstadView.ARG_CONTAINER_UID, container.containerUid.toString())
        intent.putExtra(UstadView.ARG_CONTENT_ENTRY_UID, contentEntry.contentEntryUid.toString())
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
