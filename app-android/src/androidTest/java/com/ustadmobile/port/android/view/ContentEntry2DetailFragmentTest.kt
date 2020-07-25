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
import com.toughra.ustadmobile.BuildConfig
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.dbVersionHeader
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
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
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ContentEntry2DetailFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true,
        controlServerUrl = "http://${BuildConfig.TEST_HOST}:${BuildConfig.TEST_PORT}")

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val grantPermissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

    @AdbScreenRecord("Given content entry exists should show user selected content entry")
    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntry() {
        val entryTitle = "Dummy Title"
        val testEntry = ContentEntryWithLanguage().apply {
            title = entryTitle
            description = "Dummy description"
            leaf = true
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withText(entryTitle)).check(matches(isDisplayed()))
    }



    @AdbScreenRecord("Given a content entry with available translations, should show translations to user")
    @Test
    fun givenContentEntryWithTranslationExists_whenLaunched_thenShouldShowTranslations() {
        val parentUid = 10000L
        val totalTranslations = 5
        val testEntry = runBlocking {
            dbRule.db.insertContentEntryWithTranslations(totalTranslations,parentUid)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withText(testEntry.title)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(hasChildCount(totalTranslations)))
    }


    @AdbScreenRecord("Given content entry with translation exists, when user clicks translation, should navigate to translated entry")
    @Test
    fun givenContentEntryWithTranslationExists_whenTranslationClicked_thenShouldShowContentEntry() {
        val parentUid = 10001L
        val totalTranslations = 6
        val testEntry = runBlocking {
            dbRule.db.insertContentEntryWithTranslations(totalTranslations,parentUid)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.availableTranslationView)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(hasChildCount(totalTranslations)))

        onView(withId(R.id.availableTranslationView))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        assertEquals("After clicking on item, it navigates to translated detail view",
                R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)

    }

    //This test is work-in-progress
    @Test
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
                .withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withText("Server Title")).check(matches(isDisplayed()))
        onView(withText(R.string.download)).perform(click())



    }



}