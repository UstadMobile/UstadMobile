package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.BuildConfig
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.dbVersionHeader
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.ContentEntryDetailScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.UmAppDatabaseServerRequiredTest
import com.ustadmobile.util.test.ext.insertContentEntryWithTranslations
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

class ContentEntryDetailOverviewFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(controlServerUrl = "http://${BuildConfig.TEST_HOST}:${BuildConfig.TEST_PORT}")

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    private lateinit var di: DI

    @Before
    fun setup() {
        di = (ApplicationProvider.getApplicationContext<Context>() as DIAware).di
    }

    @AdbScreenRecord("Given content entry exists should show user selected content entry")
    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntryWithProgressComplete() {

        val entryTitle = "Dummy Title"
        init {

            val testEntry = ContentEntryWithLanguage().apply {
                title = entryTitle
                description = "Dummy description"
                leaf = true
                contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
            }

            val accountManager: UstadAccountManager by di.instance()
            val activeAccount = accountManager.activeAccount

            StatementEntity().apply {
                extensionProgress = 100
                statementPersonUid = activeAccount.personUid
                statementContentEntryUid = testEntry.contentEntryUid
                contentEntryRoot = true
                resultCompletion = true
                extensionProgress = 100
                statementVerbUid = VerbEntity.VERB_COMPLETED_UID
                statementUid = dbRule.repo.statementDao.insert(this)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
                ContentEntryDetailOverviewFragment().also { fragment ->
                    fragment.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {


            ContentEntryDetailScreen {

                entryTitleTextView {
                    isDisplayed()
                    hasText(entryTitle)
                }

                progress {
                    isDisplayed()
                }
                progressCheck {
                    isDisplayed()
                }

            }


        }


    }

    @AdbScreenRecord("Given content entry exists should show user selected content entry with no progress")
    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntryWithProgressHidden() {
        val entryTitle = "Dummy Title"
        init {

            val testEntry = ContentEntryWithLanguage().apply {
                title = entryTitle
                description = "Dummy description"
                leaf = true
                contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
                ContentEntryDetailOverviewFragment().also { fragment ->
                    fragment.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ContentEntryDetailScreen {

                entryTitleTextView {
                    isDisplayed()
                    hasText(entryTitle)
                }

                progress {
                    isNotDisplayed()
                }
                progressCheck {
                    isNotDisplayed()
                }

            }


        }


    }


    @AdbScreenRecord("Given a content entry with available translations, should show translations to user")
    @Test
    fun givenContentEntryWithTranslationExists_whenLaunched_thenShouldShowTranslations() {
        val parentUid = 10000L
        val totalTranslations = 5
        var testEntry: ContentEntry? = null
        init {
            testEntry = runBlocking {
                dbRule.repo.insertContentEntryWithTranslations(totalTranslations, parentUid)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry!!.contentEntryUid)) {
                ContentEntryDetailOverviewFragment().also { fragment ->
                    fragment.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {

            ContentEntryDetailScreen {

                entryTitleTextView {
                    isDisplayed()
                    hasText(testEntry!!.title!!)
                }

                translationsList {
                    isVisible()
                    isDisplayed()
                    hasChildCount(totalTranslations)
                }


            }


        }


    }


    @AdbScreenRecord("Given content entry with translation exists, when user clicks translation, should navigate to translated entry")
    @Test
    fun givenContentEntryWithTranslationExists_whenTranslationClicked_thenShouldShowContentEntry() {
        val parentUid = 10001L
        val totalTranslations = 6
        val testEntry = runBlocking {
            dbRule.repo.insertContentEntryWithTranslations(totalTranslations, parentUid)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntryDetailOverviewFragment().also { fragment ->
                fragment.installNavController(systemImplNavRule.navController)
            }
        }

        ContentEntryDetailScreen {

            translationsList {
                isDisplayed()
                hasChildCount(totalTranslations)
                emptyChildAt(1) {
                    click()
                }
            }

        }

        assertEquals("After clicking on item, it navigates to translated detail view",
                R.id.content_entry_detail_dest, systemImplNavRule.navController.currentDestination?.id)

    }

    @Test
    fun givenContentEntryImported_whenJobWaitingForConnection_thenShowOpenButtonAndProgressBar(){

        val entryTitle = "Dummy Title"
        init {

            val contentJob = ContentJob().apply {
                cjUid = 1
                cjNotificationTitle = "Waiting for connection"
            }

            val testEntry = ContentEntryWithLanguage().apply {
                title = entryTitle
                description = "Dummy description"
                leaf = true
                contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
            }

            val container = Container().apply {
                containerUid = 1
                containerContentEntryUid = testEntry.contentEntryUid
                dbRule.repo.containerDao.insert(this)
            }

            val contentJobItem = ContentJobItem().apply {
                cjiUid = 1
                cjiJobUid = 1
                cjiContentEntryUid = testEntry.contentEntryUid
                cjiContainerUid = container.containerUid
                cjiItemProgress = 50
                cjiItemTotal = 100
                cjiStatus = JobStatus.WAITING_FOR_CONNECTION
            }

            val contentJob2 = ContentJob().apply {
                cjUid = 2
                cjNotificationTitle = "Link Import"
            }
            val contentJobItem2 = ContentJobItem().apply {
                cjiUid = 2
                cjiJobUid = 2
                cjiContentEntryUid = testEntry.contentEntryUid
                cjiContainerUid = container.containerUid
                cjiItemProgress = 0
                cjiItemTotal = 100
                cjiStatus = JobStatus.QUEUED
            }


            val containerEntry = ContainerEntry().apply {
                this.ceContainerUid = container.containerUid
                this.ceUid = dbRule.db.containerEntryDao.insert(this)
            }

            runBlocking {
                dbRule.db.contentJobDao.insertAsync(contentJob)
                dbRule.db.contentJobItemDao.insertJobItem(contentJobItem)

                dbRule.db.contentJobDao.insertAsync(contentJob2)
                dbRule.db.contentJobItemDao.insertJobItem(contentJobItem2)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
                ContentEntryDetailOverviewFragment().also { fragment ->
                    fragment.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ContentEntryDetailScreen {

                entryTitleTextView {
                    isDisplayed()
                    hasText(entryTitle)
                }

                openButton{
                    isDisplayed()
                }

                progressRecycler{
                    isVisible()
                    hasSize(2)
                }

            }

        }

    }


    //This test is work-in-progress
    //@Test
    @UmAppDatabaseServerRequiredTest
    fun givenContentEntryOnServer_whenDownloadClicked_shouldCompleteDownloadAndShowOpenButton() {
        val httpClient: HttpClient = di.direct.instance()
        val testEntry = ContentEntryWithLanguage().apply {
            title = "Server Title"
            description = "Dummy description"
            leaf = true
        }

        val uid = runBlocking {
            httpClient.post("${dbRule.endpointUrl}UmAppDatabase/ContentEntryDao/insert") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                dbVersionHeader(dbRule.db)
                setBody(testEntry)
            }.bodyAsText()
        }.toLong()

        val containerUid = runBlocking {
            httpClient.get("${dbRule.endpointUrl}UmContainer/addContainer") {
                parameter("entryUid", uid)
                parameter("type", "epub")
                parameter("resource", "test.epub")
            }.bodyAsText()
        }.toLong()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchIntent = Intent(context, MainActivity::class.java).also {
            it.putExtra(UstadView.ARG_NEXT, "${ContentEntryDetailOverviewView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$uid")
        }


        val activityScenario = launchActivity<MainActivity>(intent = launchIntent)

        //TODO: Replace this with IdlingResources
        Thread.sleep(1000)

        onView(withText("Server Title")).check(matches(isDisplayed()))
        onView(withText(R.string.download)).perform(click())

        //Click on the dialog
        onView(withText(R.string.download)).perform(click())

        Thread.sleep(5000)

        //now open it
        onView(withText(R.string.open)).perform(click())
    }


}