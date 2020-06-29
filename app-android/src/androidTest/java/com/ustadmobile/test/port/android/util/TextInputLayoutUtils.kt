package com.ustadmobile.test.port.android.util

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.ustadmobile.core.util.MessageIdOption

fun setMessageIdOption(viewId: Int, option: Int, messageIdMap: List<MessageIdOption>) {
    Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.click())
    Espresso.onView(ViewMatchers.withText(messageIdMap.find { it.code == option }!!.messageStr))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(ViewActions.click())

}