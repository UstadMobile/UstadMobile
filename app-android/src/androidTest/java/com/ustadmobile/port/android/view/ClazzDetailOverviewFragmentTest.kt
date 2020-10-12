package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Rule
import org.junit.Test


class ClazzDetailOverviewFragmentTest {

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

    lateinit var fragmentIdlingResource: UstadSingleEntityFragmentIdlingResource

    // We need this: https://github.com/android/architecture-components-samples/tree/7f861fd45d158e6277a3c35163c7f663e135b2cf/GithubBrowserSample/app/src/androidTest/java/com/android/example/github/util
    // https://medium.com/freenet-engineering/running-android-espresso-tests-with-data-binding-and-koin-a57a8d38daa5
    // https://android.jlelse.eu/espresso-ui-test-for-data-binding-dbe988d97340
    @Test
    fun givenClazzExists_whenLaunched_thenShouldShowClazz() {
        val existingClazz = Clazz().apply {
            clazzName = "Test Clazz"
            clazzDesc = "Test Description"
            clazzStartTime = (DateTime.now() - 14.days).unixMillisLong
            clazzEndTime = (DateTime.now() + 14.days).unixMillisLong
            clazzUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingClazz.clazzUid)) {
            ClazzDetailOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
                fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(it)
                IdlingRegistry.getInstance().register(fragmentIdlingResource)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)


        onView(withText("Test Description")).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }

}