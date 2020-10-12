/*
package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import androidx.navigation.findNavController
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("MainActivityIntent tests")
class MainActivityIntentTest {

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



    @AdbScreenRecord("Given app launched via intent link - " +
            "should go to correct positions")
    @Test
    fun givenApp_whenNotRunning_intentTest() {

        Thread.sleep(2000)
        var uri = "http://192.168.1.148:8087/umclient/JoinWithCode?argCode=cs75sp&argCodeTable=6"
        var intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(uri))
                .setPackage(getTargetContext().packageName)

        Thread.sleep(2000)
        var activityScenario = launchActivity<MainActivity>(intent).onActivity {
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        Thread.sleep(2000)

        var destId = 0
        activityScenario.onActivity {
            destId =
                    it.findNavController(R.id.activity_main_navhost_fragment).currentDestination?.id?:0
        }
        var destinationName : String? = null
        activityScenario.onActivity { destinationName = it.resources.getResourceName(destId?:0) }

        var implDestId = systemImplNavRule.navController.currentDestination?.id
        var implDestName : String? = null
        activityScenario.onActivity { implDestName = it.resources.getResourceName(implDestId?:0) }


        assertEquals("It navigated to joinWithcode",
                R.id.join_with_code_dest, destId)
        assertEquals("It navigated to Login", R.id.login_dest, implDestId)




        //Try for another view

        uri = "http://192.168.1.148:8087/umclient/PersonListView?argCode=cs75sp&argCodeTable=6"
        intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(uri))
                .setPackage(getTargetContext().packageName)


        activityScenario = launchActivity<MainActivity>(intent).onActivity {
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        destId = 0
        activityScenario.onActivity {
            destId =
                    it.findNavController(R.id.activity_main_navhost_fragment).currentDestination?.id?:0 }
        destinationName = null
        activityScenario.onActivity { destinationName = it.resources.getResourceName(destId?:0) }


        assertEquals("It navigated to person list",
                R.id.person_list_dest, destId)


    }


    @AdbScreenRecord("Given app resumed via intent link - " +
            "should go to correct positions")
    @Test
    fun givenApp_whenRunning_intentTest() {


        //Launch activity
        var activityScenario = launchActivity<MainActivity>().onActivity {
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onView(withText("G")).check(matches(isDisplayed()))


        //Now launch activity with intent

        var uri = "http://192.168.1.148:8087/umclient/JoinWithCode?argCode=cs75sp&argCodeTable=6"
        var intent = Intent(Intent.ACTION_VIEW,
                Uri.parse(uri))
                .setPackage(getTargetContext().packageName)

        activityScenario = launchActivity<MainActivity>(intent).onActivity {
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        var implDestId = systemImplNavRule.navController.currentDestination?.id

        var destId: Int = 0
        var currentDest = activityScenario.onActivity {
            destId =
                    it.findNavController(R.id.activity_main_navhost_fragment).currentDestination?.id?:0 }
        var destinationName : String? = null
        activityScenario.onActivity { destinationName = it.resources.getResourceName(destId?:0) }

        var implDestName : String? = null
        activityScenario.onActivity { implDestName = it.resources.getResourceName(implDestId?:0) }


        assertEquals("It navigated to joinWithcode",
                R.id.join_with_code_dest, destId)


    }


}
*/
