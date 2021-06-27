package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.picker.date.KDatePicker
import io.github.kakaocup.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.RegisterAgeRedirectFragment

object RegisterAgeRedirectScreen: KScreen<RegisterAgeRedirectScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_register_age_redirect

    override val viewClass: Class<*>
        get() = RegisterAgeRedirectFragment::class.java

    val nextButton = KButton { withId(R.id.next_button) }

    val datePicker = KDatePicker { withId(R.id.date_picker)}

}