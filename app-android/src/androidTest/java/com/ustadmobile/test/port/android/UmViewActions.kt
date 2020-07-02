package com.ustadmobile.test.port.android

import android.view.View
import android.widget.Checkable
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object UmViewActions {

    fun setChecked(checked: Boolean): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): BaseMatcher<View> {
                return object : BaseMatcher<View>() {
                    override fun matches(item: Any): Boolean {
                        return isA(Checkable::class.java).matches(item)
                    }

                    override fun describeMismatch(item: Any, mismatchDescription: Description) {}

                    override fun describeTo(description: Description) {}
                }
            }

            override fun getDescription(): String? {
                return null
            }

            override fun perform(uiController: UiController, view: View) {
                val checkableView = view as Checkable
                checkableView.isChecked = checked
            }
        }
    }

    fun singleTap(x: Float, y: Float): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isDisplayed()
            }

            override fun getDescription(): String {
                return "Send single tap event"
            }

            override fun perform(uiController: UiController, view: View) {
                val location = IntArray(2)
                view.getLocationOnScreen(location)

                // Offset coordinates by view position
                val coordinates = floatArrayOf(x + location[0], y + location[1])
                val precision = floatArrayOf(1f, 1f)

                val down = MotionEvents.sendDown(uiController, coordinates, precision).down
                uiController.loopMainThreadForAtLeast(200)
                MotionEvents.sendUp(uiController, down, coordinates)
            }
        }
    }

    fun hasInputLayoutError(expectedErrorText: String): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) { }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val error = item.error ?: ""
            return expectedErrorText == error
        }
    }

}
