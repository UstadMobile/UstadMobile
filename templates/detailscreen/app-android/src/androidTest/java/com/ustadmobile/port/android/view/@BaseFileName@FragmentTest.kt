package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class @BaseFileName@FragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @Test
    fun given@Entity@Exists_whenLaunched_thenShouldShow@Entity@() {
        val existingClazz = @Entity@().apply {
            @Entity_VariableName@Name = "Test @Entity@"
            @Entity_VariableName@Uid = dbRule.db.@Entity_VariableName@Dao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(ARG_ENTITY_UID to existingClazz.clazzUid)) {
            @BaseFileName@Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(fragmentScenario.letOnFragment { it }).also {
            IdlingRegistry.getInstance().register(it)
        }

        onView(withText("Test @Entity@")).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)
    }

}