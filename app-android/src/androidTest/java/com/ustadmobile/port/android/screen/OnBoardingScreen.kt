package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R

object OnBoardingScreen : KScreen<OnBoardingScreen>() {

    override val layoutId: Int?
        get() = R.layout.activity_on_boarding
    override val viewClass: Class<*>?
        get() = OnBoardingActivity::class.java

    val langOption = KTextInputLayout { withId(R.id.language_option)}

    val getStartedButton = KButton { withId(R.id.get_started_btn)}
}