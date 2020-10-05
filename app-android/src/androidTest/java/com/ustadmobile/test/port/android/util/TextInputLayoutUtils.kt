package com.ustadmobile.test.port.android.util

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.common.views.KView
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption

fun setMessageIdOption(viewId: Int, message: String) {
    /*KView{ withId(viewId) } perform {
        click()
    }
    KView{ withText(message) } perform {
        inRoot { isPlatformPopup() }
        click()
    }*/

    Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.click())
    Espresso.onView(ViewMatchers.withText(message))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(ViewActions.click())

}