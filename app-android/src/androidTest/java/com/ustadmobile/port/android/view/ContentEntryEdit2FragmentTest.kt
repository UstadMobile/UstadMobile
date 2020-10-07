package com.ustadmobile.port.android.view

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.not
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.io.File

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
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    private var containerManager: ContainerManager ? = null

    private var container : Container ? = null


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
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.content_entry_select_file)).check(matches(not(isDisplayed())))

        onView(withId(R.id.container_storage_option)).check(matches(not(isDisplayed())))

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ContentEntryWithLanguage().apply {
            title = dummyTitle
            description = "Description"
        }

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

    @AdbScreenRecord("Given content entry does not exist, when user fills in form and selects zipped file, should save to database")
   // @Test
    fun givenNoEntryYet_whenFormFilledZippedFileSelectedAndSaveClicked_thenShouldSaveToDatabase (){
        createEntryFromFile("test.epub")
        assertTrue("Container for an entry was created from a zipped file",
                container!!.fileSize > 0 && container!!.mimeType?.contains("zip")!!
                        && containerManager!!.allEntries.size > 1)
    }


    @AdbScreenRecord("Given content entry does not exist, when user fills in form and selects non zipped file, should save to database")
    //@Test
    fun givenNoEntryYet_whenFormFilledNonZippedFileSelectedAndSaveClicked_thenShouldSaveToDatabase (){
        createEntryFromFile("video.mp4", false)
        assertTrue("Container for an entry was created from a non zipped file",
                container!!.fileSize > 0 && !container!!.mimeType?.contains("zip")!!
                        && containerManager!!.allEntries.size == 1)
    }


    private fun createEntryFromFile(fileName: String, isZipped: Boolean = true){
        val containerTmpDir = UmFileUtilSe.makeTempDir("containerTmpDir","${System.currentTimeMillis()}")
        val testFile = File.createTempFile("contentEntryEdit", fileName, containerTmpDir)
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/$fileName")
        testFile.outputStream().use { input?.copyTo(it) }
        val expectedUri = Uri.fromFile(testFile)

         val registry = object : ActivityResultRegistry() {
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
            ContentEntryEdit2Fragment(registry).also {
                it.installNavController(systemImplNavRule.navController)
            } }) { onFragment { fragment -> fragment.handleFileSelection()}
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withId(R.id.content_entry_select_file)).check(matches(isDisplayed()))

        if(!isZipped){
            onView(withId(R.id.entry_title_text)).perform(clearText(), typeText("Dummy Title"))
        }

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val entries = dbRule.db.contentEntryDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario, 15000) {
            it.isNotEmpty()
        }

        container = dbRule.db.containerDao.getMostRecentContainerForContentEntryLive(entries?.first()?.contentEntryUid!!)
                .waitUntilWithFragmentScenario(fragmentScenario, timeout = 15000){it != null}

        assertTrue("Entry's data set and is a leaf", entries.first().title != null && entries.first().leaf)

        containerManager = ContainerManager(container!!, dbRule.db, dbRule.repo, containerTmpDir.absolutePath)

        containerTmpDir.deleteRecursively()
    }
}