package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.@BaseFileName@Screen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
@EditEntity_Import@
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.google.gson.Gson
import org.kodein.di.direct
import org.kodein.di.instance

@AdbScreenRecord("@BaseFileName@ screen Test")
class @BaseFileName@FragmentTest : TestCase(){

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


    @AdbScreenRecord("given @Entity@ not present when filled then should save to database")
    @Test
    fun givenNo@Entity@PresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            @BaseFileName@Fragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            @BaseFileName@Screen{

                @Entity@TitleInput{
                    edit{
                        clearText()
                        typeText("title")
                    }
                }


                //TODO: if required, use the savedstatehandle to add link entities
                /*
                fragmentScenario.onFragment { fragment ->
                        fragment.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(@Entity_VariableName@.relatedEntity)))
                }
                */

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val @Entity_VariableName@List = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("@Entity@ data set", "New @Entity@",
                        @Entity_VariableName@List.first().@Entity_VariableName@Name)

            }


        }
    }


    @AdbScreenRecord("given @Entity@ exists when updated then should be updated on database")
    @Test
    fun given@Entity@Exists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existing@Entity@ = @EditEntity@().apply {
            @Entity_VariableName@Name = "New @Entity@"
            @Entity_VariableName@Uid = dbRule.db.@Entity_VariableName@Dao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existing@Entity@.@Entity_VariableName@Uid)) {
            @BaseFileName@Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val gson: Gson = getApplicationDi().direct.instance()
        val entityLoadedJson = gson.toJson(entityLoadedByFragment)
        val newClazzValues = gson.fromJson(entityLoadedJson, @EditEntity@::class.java).apply {
            @Entity_VariableName@Name = "Updated @Entity@"
        }

        init{


        }.run{

            @BaseFileName@Screen {

                @Entity@TitleInput{
                    edit{
                        clearText()
                        typeText("Updated @Entity@")
                    }
                }


                //TODO: if required, use the savedstatehandle to add link entities

                /*
                fragmentScenario.onFragment { fragment ->
                    fragment..findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(@Entity_VariableName@.relatedEntity)))
                }
                */

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New @Entity@",
                        gson.fromJson(entityLoadedJson, @EditEntity@::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existing@Entity@.@Entity_VariableName@Uid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated @Entity@" }
                Assert.assertEquals("@Entity@ name is updated", "Updated @Entity@",
                        updatedEntityFromDb?.@Entity_VariableName@Name)

            }

        }

    }
}