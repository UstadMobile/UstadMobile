package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.screen.RoleListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Role list screen tests")
class RoleListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @AdbScreenRecord("List screen should show roles in database and allow clicking on item")
    @Test
    fun givenRolePresent_whenClickOnRole_thenShouldNavigateToRoleEdit() {


        val testRoleA = Role().apply{
            roleName = "Role A"
            roleActive = true
            rolePermissions = Role.PERMISSION_CLAZZ_SELECT or Role.PERMISSION_CLAZZ_ADD_TEACHER
            roleUid = dbRule.repo.roleDao.insert(this)
        }

        val testRoleB = Role().apply{
            roleName = "Role B"
            roleActive = true
            rolePermissions = Role.PERMISSION_SCHOOL_SELECT or
                    Role.PERMISSION_SCHOOL_ADD_STAFF or Role.PERMISSION_SCHOOL_ADD_STUDENT or
                    Role.PERMISSION_SCHOOL_UPDATE
            roleUid = dbRule.repo.roleDao.insert(this)
        }

        init {

            //Add admin
            dbRule.insertPersonForActiveUser(Person().apply {
                admin = true
                firstNames = "Test"
                lastName = "User"
            })

            //Launch fragment
            val fragmentScenario = launchFragmentInContainer(
                    bundleOf(), themeResId = R.style.UmTheme_App){
                RoleListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                } }

            fragmentScenario.onFragment {
                Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
            }

        }.run {

            RoleListScreen{

                recycler{

                    childWith<RoleListScreen.MainItem> {
                        withTag(testRoleA.roleUid)
                    } perform {
                        click()
                    }
                }
            }

            Assert.assertEquals("After clicking on item, it navigates to edit view",
                    R.id.role_edit_dest, systemImplNavRule.navController.currentDestination?.id)

        }



    }

}