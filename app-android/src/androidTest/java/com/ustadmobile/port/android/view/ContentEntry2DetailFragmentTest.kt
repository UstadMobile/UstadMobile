package com.ustadmobile.port.android.view

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.BuildConfig
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.dbVersionHeader
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.android.screen.ContentEntryDetailScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.*
import com.ustadmobile.util.test.ext.insertContentEntryWithTranslations
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class ContentEntry2DetailFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true,
            controlServerUrl = "http://${BuildConfig.TEST_HOST}:${BuildConfig.TEST_PORT}")

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val grantPermissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

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
                contentEntryUid = dbRule.db.contentEntryDao.insert(this)
            }

            val accountManager: UstadAccountManager by di.instance()
            val activeAccount = accountManager.activeAccount

            ContentEntryProgress().apply {
                contentEntryProgressContentEntryUid = testEntry.contentEntryUid
                contentEntryProgressProgress = 100
                contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_PASSED
                contentEntryProgressActive = true
                contentEntryProgressPersonUid = activeAccount.personUid
                contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insert(this)
            }


            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
                ContentEntry2DetailFragment().also { fragment ->
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
                contentEntryUid = dbRule.db.contentEntryDao.insert(this)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
                ContentEntry2DetailFragment().also { fragment ->
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
                dbRule.db.insertContentEntryWithTranslations(totalTranslations, parentUid)
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry!!.contentEntryUid)) {
                ContentEntry2DetailFragment().also { fragment ->
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
            dbRule.db.insertContentEntryWithTranslations(totalTranslations, parentUid)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also { fragment ->
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
                R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)

    }

    //This test is work-in-progress
    //@Test
    @UmAppDatabaseServerRequiredTest
    fun givenContentEntryOnServer_whenDownloadClicked_shouldCompleteDownloadAndShowOpenButton() {
        val testEntry = ContentEntryWithLanguage().apply {
            title = "Server Title"
            description = "Dummy description"
            leaf = true
        }

        val uid = runBlocking {
            defaultHttpClient().post<String>("${dbRule.endpointUrl}UmAppDatabase/ContentEntryDao/insert") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                dbVersionHeader(dbRule.db)
                body = testEntry
            }
        }.toLong()

        val containerUid = runBlocking {
            defaultHttpClient().get<String>("${dbRule.endpointUrl}UmContainer/addContainer") {
                parameter("entryUid", uid)
                parameter("type", "epub")
                parameter("resource", "test.epub")
            }
        }.toLong()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchIntent = Intent(context, MainActivity::class.java).also {
            it.putExtra(UstadView.ARG_NEXT, "${ContentEntry2DetailView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$uid")
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