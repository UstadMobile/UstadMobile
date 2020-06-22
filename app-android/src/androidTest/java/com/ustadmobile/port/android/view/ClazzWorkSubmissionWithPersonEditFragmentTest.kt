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

import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.IdlingRegistry
import com.ustadmobile.core.view.UstadView


class ClazzWorkSubmissionWithPersonEditFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @Test
    fun givenNoClazzWorkSubmissionWithPersonPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ClazzWorkSubmissionMarkingFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ClazzMemberAndClazzWorkWithSubmission().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. clazzWorkSubmissionWithPersonName = "New ClazzWorkSubmissionWithPerson"
        }

        fillFields(fragmentScenario, formVals, currentEntity)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val clazzWorkSubmissionWithPersonList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("ClazzWorkSubmissionWithPerson data set", "New ClazzWorkSubmissionWithPerson",
                clazzWorkSubmissionWithPersonList.first() .clazzWorkSubmissionWithPersonName)
    }


    @Test
    fun givenClazzWorkSubmissionWithPersonExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingClazzWorkSubmissionWithPerson = ClazzMemberAndClazzWorkWithSubmission().apply {
            clazzWorkSubmissionWithPersonName = "New ClazzWorkSubmissionWithPerson"
            clazzWorkSubmissionWithPersonUid = dbRule.db.clazzWorkSubmissionWithPersonDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingClazzWorkSubmissionWithPerson.clazzWorkSubmissionWithPersonUid)) {
            ClazzWorkSubmissionMarkingFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val editIdlingResource = UstadSingleEntityFragmentIdlingResource(fragmentScenario.letOnFragment { it })
        IdlingRegistry.getInstance().register(editIdlingResource)

        onIdle()

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, ClazzMemberAndClazzWorkWithSubmission::class.java).apply {
            clazzWorkSubmissionWithPersonName = "Updated ClazzWorkSubmissionWithPerson"
        }


        fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        Assert.assertEquals("Entity in database was loaded for user",
                "New ClazzWorkSubmissionWithPerson",
                defaultGson().fromJson(entityLoadedJson, ClazzMemberAndClazzWorkWithSubmission::class.java).clazzName)

        val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingClazzWorkSubmissionWithPerson.clazzWorkSubmissionWithPersonUid)
                .waitUntilWithFragmentScenario(fragmentScenario){ it?.clazzName == "Updated ClazzWorkSubmissionWithPerson"}
        Assert.assertEquals("ClazzWorkSubmissionWithPerson name is updated", "Updated ClazzWorkSubmissionWithPerson",
                updatedEntityFromDb?.clazzWorkSubmissionWithPersonName)
    }

    companion object {

        fun fillFields(fragmentScenario: FragmentScenario<ClazzWorkSubmissionMarkingFragment>,
                       clazzMemberAndClazzWorkWithSubmission: ClazzMemberAndClazzWorkWithSubmission,
                       clazzMemberAndClazzWorkWithSubmissionOnForm: ClazzMemberAndClazzWorkWithSubmission?,
                       setFieldsRequiringNavigation: Boolean = true) {
            //TODO: set these values on the form using Espresso.

            clazzMemberAndClazzWorkWithSubmission.clazzWorkSubmissionWithPersonName?.takeIf {it != clazzMemberAndClazzWorkWithSubmissionOnForm?.clazzWorkSubmissionWithPersonName }?.also {
                onView(withId(R.id.id_of_textfield)).perform(clearText(), typeText(it))
            }

            if(!setFieldsRequiringNavigation) {
                return
            }

            //TODO: if required, use the savedstatehandle to add link entities

            fragmentScenario.onFragment { fragment ->
                fragment.takeIf {clazzMemberAndClazzWorkWithSubmission.relatedEntity != clazzMemberAndClazzWorkWithSubmissionOnForm?.relatedEntity }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("RelatedEntityName", defaultGson().toJson(listOf(clazzMemberAndClazzWorkWithSubmission.relatedEntity)))
            }

        }
    }
}