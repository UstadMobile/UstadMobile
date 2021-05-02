package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.screen.RoleEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilLetOnFragment
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Role edit screen tests")
class RoleEditFragmentTest : TestCase() {

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
    val dataBindingIdlingResourceRule =
            ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule =
            ScenarioIdlingResourceRule(CrudIdlingResource())

    lateinit var fragmentScenario: FragmentScenario<RoleEditFragment>


    @AdbScreenRecord("When a new Role is filled, it should save to the Database")
    @Test
    fun givenNoRolePresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        init {
            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                RoleEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                    it.arguments = bundleOf()
                }
            }
        }.run {

            fragmentScenario.waitUntilLetOnFragment { it.entity }

            val formVals = Role().apply {
                roleName = "Role C"
                rolePermissions = Role.PERMISSION_PERSON_SELECT or Role.PERMISSION_PERSON_INSERT or
                        Role.PERMISSION_PERSON_UPDATE
            }


            RoleEditScreen {

                editNameLayout {
                    edit {
                        clearText()
                        replaceText(formVals.roleName!!)
                        hasText(formVals.roleName!!)
                    }
                }

                val repo = dbRule.repo as DoorDatabaseSyncRepository
                repo.clientId
                fragmentScenario.clickOptionMenu(R.id.menu_done)

                flakySafely(timeoutMs = 10000, intervalMs = 1000) {
                    val roles = dbRule.db.roleDao.findAllActiveRolesLive().waitUntilWithFragmentScenario(fragmentScenario) {
                        it.isNotEmpty() && it[0].roleName == "Role C"
                    }

                    Assert.assertEquals("Role data set", "Role C", roles!!.first().roleName)
                }
            }

        }


    }

    @Test
    fun givenRoleExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {

        val existingRole = Role().apply {
            roleName = "Role D"
            roleUid = dbRule.repo.roleDao.insert(this)
        }
        init {

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingRole.roleUid)) {
                RoleEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                    .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        }.run {

            RoleEditScreen {

                //Freeze and serialize the value as it was first shown to the user
                val entityLoadedByFragment = fragmentScenario.waitUntilLetOnFragment { it.entity }
                val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
                val newRoleValues = defaultGson().fromJson(entityLoadedJson, Role::class.java).apply {
                    roleName = "Role E"
                }

                editNameLayout {
                    edit {
                        hasText(existingRole.roleName!!)
                        clearText()
                        replaceText(newRoleValues.roleName!!)
                        hasText(newRoleValues.roleName!!)
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "Role D",
                        defaultGson().fromJson(entityLoadedJson, Role::class.java).roleName)

                val updatedEntityFromDb = dbRule.db.roleDao.findByUidLive(existingRole.roleUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.roleName == "Role E" }
                Assert.assertEquals("Role name is updated", "Role E",
                        updatedEntityFromDb?.roleName)

            }
        }
    }

}
