package com.ustadmobile.test.port.android.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryStatusDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.view.ContentEntryDetailActivity
import com.ustadmobile.port.android.view.WebChunkActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ContentEntryDetailEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(ContentEntryDetailActivity::class.java, false, false)

    lateinit var context: Context

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION)


    private var statusDao: ContentEntryStatusDao? = null

    val db: UmAppDatabase
        get() {
            context = InstrumentationRegistry.getInstrumentation().context
            val db = UmAppDatabase.getInstance(context)
            db.clearAllTables()
            return  UmAppDatabase.getInstance(context)// db.getUmRepository("https://localhost", "")
        }

    @Throws(IOException::class)
    fun createDummyContent() {
        val repo = db

        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP

        val contentDao = repo.contentEntryDao
        val contentEntryRelatedEntryJoinDao = repo.contentEntryRelatedEntryJoinDao
        val languageDao = repo.languageDao
        val containerDao = repo.containerDao

        val app = UmAppDatabase.getInstance(context)
        statusDao = app.contentEntryStatusDao

        val englishLang = Language()
        englishLang.langUid = 1
        englishLang.name = "English"
        englishLang.iso_639_1_standard = "en"
        englishLang.iso_639_2_standard = "eng"
        englishLang.iso_639_3_standard = "eng"
        languageDao.insert(englishLang)

        val arabicLang = Language()
        arabicLang.langUid = 2
        arabicLang.name = "Arabic"
        arabicLang.iso_639_1_standard = "ar"
        arabicLang.iso_639_2_standard = "ara"
        arabicLang.iso_639_3_standard = "ara"
        languageDao.insert(arabicLang)

        val spanishLang = Language()
        spanishLang.langUid = 3
        spanishLang.name = "Spanish"
        spanishLang.iso_639_1_standard = "es"
        spanishLang.iso_639_2_standard = "esp"
        spanishLang.iso_639_3_standard = "esp"
        languageDao.insert(spanishLang)


        val quiz = ContentEntry()
        quiz.contentEntryUid = 6
        quiz.title = "Quiz Time"
        quiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        quiz.description = "All content"
        quiz.publisher = "CK12"
        quiz.author = "Binge"
        quiz.primaryLanguageUid = 1
        quiz.leaf = true
        contentDao.insert(quiz)

        val contentEntryFile = Container()
        contentEntryFile.mimeType = "application/zip"
        contentEntryFile.fileSize = 10
        contentEntryFile.lastModified = 1540728217
        contentEntryFile.containerContentEntryUid = 8
        containerDao.insert(contentEntryFile)

        // arabic
        val arabicQuiz = ContentEntry()
        arabicQuiz.contentEntryUid = 10
        arabicQuiz.title = "وقت الاختبار"
        arabicQuiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        arabicQuiz.description = "كل المحتوى"
        arabicQuiz.publisher = "CK12"
        arabicQuiz.author = "حفلة"
        arabicQuiz.primaryLanguageUid = 2
        arabicQuiz.leaf = true
        contentDao.insert(arabicQuiz)

        val updatedFile = Container()
        updatedFile.mimeType = "application/zip"
        updatedFile.fileSize = 10
        updatedFile.lastModified = 1540728218
        updatedFile.containerContentEntryUid = 11
        containerDao.insert(updatedFile)

        val arabicEnglishJoin = ContentEntryRelatedEntryJoin()
        arabicEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        arabicEnglishJoin.cerejRelatedEntryUid = arabicQuiz.contentEntryUid
        arabicEnglishJoin.cerejRelLanguageUid = 2
        arabicEnglishJoin.cerejUid = 13
        arabicEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(arabicEnglishJoin)

        val spanishQuiz = ContentEntry()
        spanishQuiz.contentEntryUid = 14
        spanishQuiz.title = "tiempo de prueba"
        spanishQuiz.thumbnailUrl = "https://www.africanstorybook.org/img/asb120.png"
        spanishQuiz.description = "todo el contenido"
        spanishQuiz.publisher = "CK12"
        spanishQuiz.author = "borrachera"
        spanishQuiz.primaryLanguageUid = 3
        spanishQuiz.leaf = true
        contentDao.insert(spanishQuiz)

        val spanishFile = Container()
        spanishFile.mimeType = "application/zip"
        spanishFile.fileSize = 10
        spanishFile.lastModified = 1540728218
        spanishFile.containerContentEntryUid = 15
        containerDao.insert(spanishFile)

        val spanishEnglishJoin = ContentEntryRelatedEntryJoin()
        spanishEnglishJoin.cerejContentEntryUid = quiz.contentEntryUid
        spanishEnglishJoin.cerejRelatedEntryUid = spanishQuiz.contentEntryUid
        spanishEnglishJoin.cerejRelLanguageUid = 3
        spanishEnglishJoin.cerejUid = 17
        spanishEnglishJoin.relType = ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION
        contentEntryRelatedEntryJoinDao.insert(spanishEnglishJoin)

        val statusStart = ContentEntryStatus()
        statusStart.cesLeaf = true
        statusStart.downloadStatus = 0
        statusStart.totalSize = 1000
        statusStart.bytesDownloadSoFar = 0
        statusStart.cesUid = 6
        statusDao!!.insert(statusStart)

        val statusPending = ContentEntryStatus()
        statusPending.cesLeaf = true
        statusPending.downloadStatus = JobStatus.RUNNING
        statusPending.totalSize = 1000
        statusPending.bytesDownloadSoFar = 500
        statusPending.cesUid = 10
        statusDao!!.insert(statusPending)

        val statusCompleted = ContentEntryStatus()
        statusCompleted.cesLeaf = true
        statusCompleted.downloadStatus = JobStatus.COMPLETE
        statusCompleted.totalSize = 1000
        statusCompleted.bytesDownloadSoFar = 1000
        statusCompleted.cesUid = 14
        statusDao!!.insert(statusCompleted)

        val file = Container()
        file.mimeType = "application/webchunk+zip"
        file.lastModified = System.currentTimeMillis()
        file.containerContentEntryUid = 18L
        containerDao.insert(file)

    }

    @Test
    @Throws(IOException::class)
    fun givenContentEntryDetailPresent_whenOpened_entryIsDisplayed() {
        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 6L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(allOf<View>(withId(R.id.entry_detail_title), withText("Quiz Time")))

        onView(allOf<View>(withId(R.id.entry_detail_description), withText("All content")))

        onView(allOf<View>(withId(R.id.entry_detail_author), withText("Binge")))

    }

    @Test
    @Throws(IOException::class)
    fun givenContentEntryDetailPresent_whenTranslatedIsSelected_arabicEntryIsDisplayed() {
        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 6L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(allOf<View>(isDisplayed(), withId(R.id.entry_detail_flex)))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        intended(AllOf.allOf(
                hasComponent(ContentEntryDetailActivity::class.java.canonicalName),
                hasExtra(equalTo(ContentEntryListFragmentPresenter.ARG_CONTENT_ENTRY_UID),
                        equalTo(10L.toString())
                )))

        onView(allOf<View>(withId(R.id.entry_detail_title), withText("وقت الاختبار")))

        onView(allOf<View>(withId(R.id.entry_download_open_button),
                withEffectiveVisibility(Visibility.VISIBLE),
                withText("Download")))


    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun givenContentEntryDetailPresent_whenDownloadJobNotStarted_thenShowDownloadButton() {
        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 6L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(withId(R.id.entry_download_open_button))
                .check(matches(withText("Download")))
                .check(matches(withEffectiveVisibility(
                        Visibility.VISIBLE)))
    }

    @Test
    @Throws(IOException::class)
    fun givenContentEntryDetailPresent_whenDownloadJobRunning_thenShowProgressBar() {

        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 10L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(withId(R.id.entry_download_open_button))
                .check(matches(withEffectiveVisibility(
                        Visibility.GONE)))
    }


    @Test
    @Throws(IOException::class)
    fun givenContentEntryDetailPresent_whenDownloadJobCompleted_thenCompletedDownloadButton() {
        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 14L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(withId(R.id.entry_download_open_button))
                .check(matches(withText("Open")))
                .check(matches(withEffectiveVisibility(
                        Visibility.VISIBLE)))

    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun givenWebChunkContentEntryDetailDownloaded_whenOpenButtonClicked_shouldOpenWebChunkFile() {
        createDummyContent()

        val launchActivityIntent = Intent()
        launchActivityIntent.putExtra(ARG_CONTENT_ENTRY_UID, 14L.toString())
        mActivityRule.launchActivity(launchActivityIntent)

        onView(withId(R.id.entry_download_open_button)).perform(click())

        Thread.sleep(5000)

        intended(AllOf.allOf(
                hasComponent(WebChunkActivity::class.java.canonicalName),
                hasExtra(equalTo(WebChunkView.ARG_CONTAINER_UID),
                        equalTo(18L.toString())
                )))

    }


}
