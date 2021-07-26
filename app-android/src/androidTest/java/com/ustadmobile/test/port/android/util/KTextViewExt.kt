package com.ustadmobile.test.port.android.util

import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KTextView

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