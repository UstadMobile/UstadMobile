package com.ustadmobile.test.port.android

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object UmAndroidTestUtil {

    /**
     * Set Airplane mode on to test reaction to the system going offline
     *
     * @param enabled
     */
    fun setAirplaneModeEnabled(enabled: Boolean, backTwice: Boolean) {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val deviceHeight = uiDevice.displayHeight
        uiDevice.swipe(100, 0, 100, deviceHeight / 2, 10)
        SystemClock.sleep(200)
        uiDevice.swipe(100, 0, 100, deviceHeight / 2, 10)

        //see what the state is now
        var airplaneModeObject: UiObject2? = uiDevice.findObject(By.descContains("plane"))
        if (airplaneModeObject == null) {
            airplaneModeObject = uiDevice.findObject(By.descContains("Flight"))
        }

        if (airplaneModeObject == null)
            throw IllegalStateException("Could not find flight mode button")

        val contentDesc = airplaneModeObject.contentDescription

        if (isAirPlaneModeOn(contentDesc) != enabled || !isAirPlaneModeSupported(contentDesc))
            airplaneModeObject.click()

        SystemClock.sleep(100)
        uiDevice.pressBack()
        if (backTwice) {
            SystemClock.sleep(100)
            uiDevice.pressBack()
        }
    }

    fun setAirplaneModeEnabled(enabled: Boolean) {
        setAirplaneModeEnabled(enabled, true)
    }

    private fun isAirPlaneModeOn(contentDesc: String): Boolean {
        if (isAirPlaneModeSupported(contentDesc)) {
            for (desc in contentDesc.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (desc.toLowerCase().startsWith("on")) return true
            }
        }
        return false
    }

    fun swipeScreenDown() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val deviceHeight = uiDevice.displayHeight
        uiDevice.swipe(100, deviceHeight / 2, 0, 0, 10)
    }


    fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    private fun isAirPlaneModeSupported(contentDesc: String): Boolean {
        val descs = contentDesc.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var supported = descs.size > 1
        for (desc in descs) {
            if (desc.toLowerCase().startsWith("on") || desc.toLowerCase().startsWith("off")) {
                supported = true
            }
        }
        return supported
    }

}
