package com.ustadmobile.port.android.screen

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.OnBoardingActivity

object OnBoardingScreen : KScreen<OnBoardingScreen>() {

    override val layoutId: Int?
        get() = R.layout.activity_on_boarding
    override val viewClass: Class<*>?
        get() = OnBoardingActivity::class.java

    val langOption = KTextInputLayout { withId(R.id.language_option)}

    val getStartedButton = KButton { withId(R.id.get_started_btn)}
}