package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.github.kakaocup.kakao.switch.KSwitch
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ScopedGrantEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.ScopedGrant
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
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.direct
import org.kodein.di.instance
import java.lang.IllegalStateException

@AdbScreenRecord("ScopedGrantEdit screen Test")
class ScopedGrantEditFragmentTest : TestCase(){

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


    private fun launchFragmentInContainer(initalEntity: ScopedGrant? = null): FragmentScenario<ScopedGrantEditFragment> {
        val argBundle = bundleOf(
            ScopedGrantEditView.ARG_PERMISSION_LIST to Clazz.TABLE_ID.toString(),
            UstadView.ARG_RESULT_DEST_VIEWNAME to ClazzEdit2View.VIEW_NAME,
            UstadView.ARG_RESULT_DEST_KEY to "ScopedGrant"
        )

        if(initalEntity != null) {
            argBundle.putString(
                ARG_ENTITY_JSON, Json.encodeToString(ScopedGrant.serializer(), initalEntity))
        }

        return launchFragmentInContainer(themeResId = R.style.UmTheme_App, fragmentArgs = argBundle) {
            ScopedGrantEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
                (systemImplNavRule.navController as TestNavHostController).also {
                    it.navigate(R.id.clazz_edit_dest)
                    it.navigate(R.id.scoped_grant_edit_dest)
                }
            }
        }
    }

    @AdbScreenRecord("given ScopedGrant not present when filled then should save to database")
    @Test
    fun givenNoScopedGrantPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToJson() {
        lateinit var fragmentScenario: FragmentScenario<ScopedGrantEditFragment>
        init{
            fragmentScenario = launchFragmentInContainer()
        }.run{
            ScopedGrantEditScreen{
                recycler {
                    childWith<ScopedGrantEditScreen.BitmaskItem> {
                        withTag(Role.PERMISSION_CLAZZ_ADD_STUDENT)
                    } perform {
                        click()
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                fragmentScenario.onFragment {
                    val savedItem = systemImplNavRule.navController.getBackStackEntry(R.id.clazz_edit_dest)
                        .savedStateHandle.get<String>("ScopedGrant") ?: throw IllegalStateException("no saveditem")
                    val savedScopedGrant = Json.decodeFromString(ListSerializer(ScopedGrant.serializer()),
                        savedItem)
                    Assert.assertEquals("ScopedGrant has expected permission",
                        Role.PERMISSION_CLAZZ_ADD_STUDENT, savedScopedGrant.first().sgPermissions)
                }

            }
        }
    }

    @AdbScreenRecord("given ScopedGrant not present when filled then should save to database")
    @Test
    fun givenScopedGrantPassedViaArg_whenFilledInAndSaveClicked_thenShouldSaveToJson() {
        lateinit var fragmentScenario: FragmentScenario<ScopedGrantEditFragment>
        init{
            fragmentScenario = launchFragmentInContainer(ScopedGrant().apply {
                sgPermissions = Role.PERMISSION_CLAZZ_ADD_TEACHER or Role.PERMISSION_CLAZZ_ADD_STUDENT
            })
        }.run{

            ScopedGrantEditScreen{
                permissionSwitch {
                    withTag(Role.PERMISSION_CLAZZ_ADD_STUDENT)
                } perform {
                    isChecked()
                }

                recycler {
                    childWith<ScopedGrantEditScreen.BitmaskItem> {
                        withTag(Role.PERMISSION_PERSON_SELECT)
                    } perform {
                        click()
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                fragmentScenario.onFragment {
                    val savedItem = systemImplNavRule.navController.getBackStackEntry(R.id.clazz_edit_dest)
                        .savedStateHandle.get<String>("ScopedGrant") ?: throw IllegalStateException("no saveditem")
                    val savedScopedGrant = Json.decodeFromString(ListSerializer(ScopedGrant.serializer()),
                        savedItem)
                    val expectedPermission = Role.PERMISSION_PERSON_SELECT
                        .or(Role.PERMISSION_CLAZZ_ADD_STUDENT)
                        .or(Role.PERMISSION_CLAZZ_ADD_TEACHER)

                    Assert.assertEquals("ScopedGrant has expected permission",
                        expectedPermission, savedScopedGrant.first().sgPermissions)
                }

            }
        }
    }

}