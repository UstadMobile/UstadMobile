package com.ustadmobile.test.port.android.util

import com.agoda.kakao.common.views.KView
import com.agoda.kakao.text.KTextView

fun setMessageIdOption(view: KTextView, message: String){

    view{
        isClickable()
        click()
    }

    KView{
        withText(message)
    } perform {
        inRoot { isPlatformPopup() }
        isDisplayed()
        click()
    }

    view.hasText(message)

}