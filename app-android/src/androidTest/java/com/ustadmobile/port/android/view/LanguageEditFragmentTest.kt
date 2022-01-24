package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.LanguageEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R

import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.screen.LeavingReasonEditScreen
import kotlinx.coroutines.runBlocking


@AdbScreenRecord("LanguageEdit screen Test")
class LanguageEditFragmentTest : TestCase(){


    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

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

    private lateinit var fragmentScenario: FragmentScenario<LanguageEditFragment>


    @AdbScreenRecord("given Language not present when filled then should save to database")
    @Test
    fun givenNoLanguagePresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        init{

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                LanguageEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            LanguageEditScreen{

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                languageTitleInput {
                    isErrorEnabled()
                    edit {
                        typeText("Russian")
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val languageList = dbRule.db.languageDao.findAllLanguageLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.firstOrNull()?.name == "Russian"
                }

                Assert.assertEquals("Language data set", "Russian",
                        languageList!!.first().name)

            }


        }
    }


    @AdbScreenRecord("given Language exists when updated then should be updated on database")
    @Test
    fun givenLanguageExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingLanguage = Language().apply {
            name = "Russian"
            langUid = dbRule.repo.languageDao.insert(this)
        }

        init{

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to
                            existingLanguage.langUid)) {
                LanguageEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            LanguageEditScreen {

                flakySafely {
                    languageTitleInput {
                        edit {
                            clearText()
                            typeText("Italian")
                            hasText("Italian")
                        }
                    }
                }
                fragmentScenario.clickOptionMenu(R.id.menu_done)

                runBlocking {
                    val langFromDb = dbRule.db.languageDao.findAllLanguageLive()
                            .waitUntilWithFragmentScenario(fragmentScenario){
                                it[0].name == "Italian"
                            }
                    Assert.assertEquals("title change matches",
                            "Italian",
                            langFromDb!![0].name)
                }



            }

        }

    }
}