package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertContentEntryWithTranslations
import junit.framework.Assert
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

class ContentEntry2DetailFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    lateinit var fragmentIdlingResource: UstadSingleEntityFragmentIdlingResource

    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntry() {
        val entryTitle = "Dummy Title"
        val testEntry = ContentEntryWithLanguage().apply {
            title = entryTitle
            description = "Dummy description"
            leaf = true
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
                fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(fragment).also {
                    IdlingRegistry.getInstance().register(it)
                }
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        onView(withText(entryTitle)).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }



    @Test
    fun givenContentEntryWithTranslationExists_whenLaunched_thenShouldShowTranslations() {
        val parentUid = 10000L
        val totalTranslations = 5
        val testEntry = runBlocking {
            dbRule.db.insertContentEntryWithTranslations(totalTranslations,parentUid)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
                fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(fragment).also {
                    IdlingRegistry.getInstance().register(it)
                }
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        onView(withText(testEntry.title)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(hasChildCount(totalTranslations)))

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }


    @Test
    fun givenContentEntryWithTranslationExists_whenTranslationClicked_thenShouldShowContentEntry() {
        val parentUid = 10001L
        val totalTranslations = 6
        val testEntry = runBlocking {
            dbRule.db.insertContentEntryWithTranslations(totalTranslations,parentUid)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to testEntry.contentEntryUid)) {
            ContentEntry2DetailFragment().also {fragment ->
                fragment.installNavController(systemImplNavRule.navController)
                fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(fragment).also {
                    IdlingRegistry.getInstance().register(it)
                }
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)


        onView(withId(R.id.availableTranslationView)).check(matches(isDisplayed()))

        onView(withId(R.id.availableTranslationView)).check(matches(hasChildCount(totalTranslations)))

        onView(withId(R.id.availableTranslationView))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        Assert.assertEquals("After clicking on item, it navigates to translated detail view",
                R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }


}