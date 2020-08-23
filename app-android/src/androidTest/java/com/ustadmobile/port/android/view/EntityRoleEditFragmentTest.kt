package com.ustadmobile.port.android.view

import android.app.Application
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep


@AdbScreenRecord("EntityRoleEdit screen Test")
class EntityRoleEditFragmentTest {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var serverUrl: String

    val impl =  UstadMobileSystemImpl.instance

    @Before
    fun setUp(){
        impl.messageIdMap = MessageIDMap.ID_MAP
        mockWebServer = MockWebServer()
        mockWebServer.start()
        serverUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @AdbScreenRecord("Given new entity role assignment. When submitted without Role " +
            " selct, should show error")
    @Test
    fun givenNewEntityRole_whenSavedWithoutRole_shouldShowError(){


        val newRole = Role().apply {
            roleName = "Role A"
            roleUid= dbRule.db.roleDao.insert(this)
        }
        val schoolA = School().apply {
            schoolName = "School A"
            schoolActive = true
            schoolUid = dbRule.db.schoolDao.insert(this)
        }

        val person =  dbRule.db.insertPersonOnlyAndGroup(Person().apply {
            firstNames = "Person"
            lastName = "Two"
            admin = false
            active = true
        })


        val scenario = launchFragment(true)


        onView(withId(R.id.fragment_entityrole_edit_role_tiet)).check(matches(isDisplayed()))


    }


    private fun launchFragment(fillForm: Boolean = true,
                               entityRoleUid: Long = 0, groupUid: Long = 0): FragmentScenario<EntityRoleEditFragment> {

        var args = mapOf(UstadView.ARG_ENTITY_UID to entityRoleUid.toString(),
                        UstadView.ARG_FILTER_BY_PERSONGROUPUID to groupUid.toString())
        if(entityRoleUid != 0L){
            args = mapOf(UstadView.ARG_ENTITY_UID to entityRoleUid.toString(),
                    UstadView.ARG_FILTER_BY_PERSONGROUPUID to groupUid.toString())
        }

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
            EntityRoleEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = args.toBundle()
            }
        }

        onIdle()

        //Soft keyboard tend to hide views, when try to type will throw exception so instead of type we'll replace text
        if(fillForm){

            val entityRoleOnForm = scenario.letOnFragment { it.entity}

            //TODO: fill me up

            //scenario.clickOptionMenu(R.id.menu_done)
        }

        return scenario
    }

    private fun scrollToBottom(){
        onView(withId(R.id.nested_view)).perform(swipeUp())
        //make sure scroll animation is completed
        sleep(500)
    }

}