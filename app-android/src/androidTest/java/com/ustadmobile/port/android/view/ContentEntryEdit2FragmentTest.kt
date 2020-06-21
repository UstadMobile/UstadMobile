package com.ustadmobile.port.android.view

import android.app.Application
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.lang.Thread.sleep

@AdbScreenRecord("Content entry edit screen tests")
class ContentEntryEdit2FragmentTest  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("Given folder does not yet exist, when user fills in form for new folder, should be saved to database")
    //@Test
    fun givenNoFolderYet_whenFormFilledInAndSaveClicked_thenShouldSaveToDatabase (){
        val dummyTitle = "New Folder Entry"

        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(ARG_LEAF to false.toString(),
                        ARG_PARENT_ENTRY_UID to 10000L.toString()),
                themeResId = R.style.UmTheme_App) {
            ContentEntryEdit2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        //wait for the fragment to be ready since we are waiting on onViewCreated to create a presenter
        sleep(1000)

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ContentEntryWithLanguage().apply {
            title = dummyTitle
            description = "Description"
        }

        onView(withId(R.id.content_entry_select_file)).check(matches(not(isDisplayed())))

        onView(withId(R.id.container_storage_option)).check(matches(not(isDisplayed())))

        formVals.title?.takeIf { it != currentEntity?.title }?.also {
            onView(withId(R.id.entry_title_text)).perform(clearText(), typeText(it))
        }

        formVals.description?.takeIf { it != currentEntity?.description }?.also {
            onView(withId(R.id.entry_description_text)).perform(clearText(), typeText(it))
        }

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val entries = dbRule.db.contentEntryDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("Entry's data set", dummyTitle, entries?.first()?.title)

    }

    @AdbScreenRecord("Given content entry does not exist, when user fills in form and selects file, should save to database")
    //@Test
    fun givenNoEntryYet_whenFormFilledInAndSaveClicked_thenShouldSaveToDatabase (){
        val context = getApplicationContext<Application>()
        val testFile = File.createTempFile("contentEntryEdit", "testFile", context.cacheDir)
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/test.epub")
        testFile.outputStream().use { input?.copyTo(it) }
        val expectedUri = Uri.fromFile(testFile)

        val umTestRegistry = object : ActivityResultRegistry() {
            override fun <I, O> invoke(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedUri)
            }
        }

        val fragmentScenario  = with(launchFragmentInContainer(
                fragmentArgs = bundleOf(ARG_LEAF to true.toString(),
                ARG_PARENT_ENTRY_UID to 10000L.toString()), themeResId = R.style.UmTheme_App) {
            ContentEntryEdit2Fragment(umTestRegistry).also {
                it.installNavController(systemImplNavRule.navController)
            } }) { onFragment { fragment ->
                fragment.handleFileSelection()
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        //wait for the fragment to be ready since we are waiting on onViewCreated to create a presenter
        sleep(1000)

        onView(withId(R.id.content_entry_select_file)).check(matches(isDisplayed()))

        onView(withId(R.id.container_storage_option)).check(matches(isDisplayed()))

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        sleep(2500)

        val entries = dbRule.db.contentEntryDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertTrue("Entry's data set and is a leaf", entries?.first()?.title != null && entries.first().leaf)

        val container = runBlocking {
            dbRule.db.containerDao.getMostRecentContainerForContentEntry(entries?.first()?.contentEntryUid!!)
        }

        val totalContainerSize = runBlocking {
            dbRule.db.containerEntryFileDao.sumContainerFileEntrySizes(container?.containerUid!!)
        }

        Assert.assertTrue("Container for an entry was created created and has files",
                container != null && totalContainerSize > 0)

        testFile.deleteOnExit()

    }
}