package com.ustadmobile.port.android.view

import android.os.Handler
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class ContentEntryEdit2FragmentTest  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @Test
    fun givenNoEntryYet_whenFormFilledInAndSaveClicked_thenShouldSaveToDatabase (){
        val dummyFolderTitle = "New Entry"

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme) {
            ContentEntryEdit2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(ARG_LEAF to false.toString(),
                        ARG_PARENT_ENTRY_UID to 10000L.toString())
            }
        }

        //wait for the fragment to be ready since we are waiting on onViewCreated to create a presenter
        Handler(Looper.getMainLooper()).postDelayed({
            val currentEntity = fragmentScenario.letOnFragment { it.entity }
            val formVals = ContentEntryWithLanguage().apply {
                title = dummyFolderTitle
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

            Assert.assertEquals("Entry's data set", dummyFolderTitle, entries!!.first().title)

        }, TimeUnit.SECONDS.toMillis(3))
    }
}