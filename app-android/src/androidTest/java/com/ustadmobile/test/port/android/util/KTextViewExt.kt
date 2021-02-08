package com.ustadmobile.test.port.android.util

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.text.KTextView

fun KTextView.setMessageIdOption(message: String) {

    isClickable()
    click()

    KView {
        withText(message)
    } perform {
        inRoot { isPlatformPopup() }
        isDisplayed()
        click()
    }

    hasText(message)

}