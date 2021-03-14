package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.LanguageEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


@AdbScreenRecord("LanguageEdit screen Test")
class LanguageEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @AdbScreenRecord("given Language not present when filled then should save to database")
    @Test
    fun givenNoLanguagePresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            LanguageEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = Language().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. languageName = "New Language"
        }

        init{

        }.run{

            LanguageEditScreen{

                language.languageName?.takeIf {it != languageOnForm?.languageName }?.also {
                    LanguageTitleInput{
                        edit{
                            clearText()
                            typeText(it)
                        }
                    }
                }

                //TODO: if required, use the savedstatehandle to add link entities
                fragmentScenario.onFragment { fragment ->
                    fragment.takeIf {language.relatedEntity != languageOnForm?.relatedEntity }
                            ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(language.relatedEntity)))
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val languageList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Language data set", "New Language",
                        languageList.first() .languageName)

            }


        }
    }


    @AdbScreenRecord("given Language exists when updated then should be updated on database")
    @Test
    fun givenLanguageExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingLanguage = Language().apply {
            languageName = "New Language"
            languageUid = dbRule.db.languageDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingLanguage.languageUid)) {
            LanguageEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, Language::class.java).apply {
            languageName = "Updated Language"
        }

        init{


        }.run{

            LanguageEditScreen {

                language.languageName?.takeIf {it != languageOnForm?.languageName }?.also {
                    LanguageTitleInput{
                        edit{
                            clearText()
                            typeText(it)
                        }
                    }
                }

                //TODO: if required, use the savedstatehandle to add link entities

                fragmentScenario.onFragment { fragment ->
                    fragment.takeIf {language.relatedEntity != languageOnForm?.relatedEntity }
                            ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(language.relatedEntity)))
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New Language",
                        defaultGson().fromJson(entityLoadedJson, Language::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingLanguage.languageUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Language" }
                Assert.assertEquals("Language name is updated", "Updated Language",
                        updatedEntityFromDb?.languageName)

            }

        }

    }
}