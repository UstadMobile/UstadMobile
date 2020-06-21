package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
@EditEntity_Import@
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.IdlingRegistry
import com.ustadmobile.core.view.UstadView


class @BaseFileName@FragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @Test
    fun givenNo@Entity@PresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            @BaseFileName@Fragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = @EditEntity@().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. @Entity_VariableName@Name = "New @Entity@"
        }

        fillFields(fragmentScenario, formVals, currentEntity)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val @Entity_VariableName@List = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("@Entity@ data set", "New @Entity@",
                @Entity_VariableName@List.first() .@Entity_VariableName@Name)
    }


    @Test
    fun given@Entity@Exists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existing@Entity@ = @EditEntity@().apply {
            @Entity_VariableName@Name = "New @Entity@"
            @Entity_VariableName@Uid = dbRule.db.@Entity_VariableName@Dao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existing@Entity@.@Entity_VariableName@Uid)) {
            @BaseFileName@Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val editIdlingResource = UstadSingleEntityFragmentIdlingResource(fragmentScenario.letOnFragment { it })
        IdlingRegistry.getInstance().register(editIdlingResource)

        onIdle()

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, @EditEntity@::class.java).apply {
            @Entity_VariableName@Name = "Updated @Entity@"
        }


        fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        Assert.assertEquals("Entity in database was loaded for user",
                "New @Entity@",
                defaultGson().fromJson(entityLoadedJson, @EditEntity@::class.java).clazzName)

        val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existing@Entity@.@Entity_VariableName@Uid)
                .waitUntilWithFragmentScenario(fragmentScenario){ it?.clazzName == "Updated @Entity@"}
        Assert.assertEquals("@Entity@ name is updated", "Updated @Entity@",
                updatedEntityFromDb?.@Entity_VariableName@Name)
    }

    companion object {

        fun fillFields(fragmentScenario: FragmentScenario<@BaseFileName@Fragment>,
                       @Entity_VariableName@: @EditEntity@,
                       @Entity_VariableName@OnForm: @EditEntity@?,
                       setFieldsRequiringNavigation: Boolean = true) {
            //TODO: set these values on the form using Espresso.

            @Entity_VariableName@.@Entity_VariableName@Name?.takeIf {it != @Entity_VariableName@OnForm?.@Entity_VariableName@Name }?.also {
                onView(withId(R.id.id_of_textfield)).perform(clearText(), typeText(it))
            }

            if(!setFieldsRequiringNavigation) {
                return
            }

            //TODO: if required, use the savedstatehandle to add link entities

            fragmentScenario.onFragment { fragment ->
                fragment.takeIf {@Entity_VariableName@.relatedEntity != @Entity_VariableName@OnForm?.relatedEntity }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("RelatedEntityName", defaultGson().toJson(listOf(@Entity_VariableName@.relatedEntity)))
            }

        }
    }
}