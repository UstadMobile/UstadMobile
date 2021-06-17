package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.RegisterMinorWaitForParentFragment

object RegisterMinorWaitForParentScreen: KScreen<RegisterMinorWaitForParentScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_register_minor_wait_for_parent

    override val viewClass: Class<*>
        get() = RegisterMinorWaitForParentFragment::class.java


    val okButton = KButton { withId(R.id.ok_button) }

    val toggleVisibilityButton = KButton { withId(R.id.password_toggle) }

    val usernameText = KTextView { withId(R.id.username_text) }

    val passwordText = KTextView { withId(R.id.password_text) }

}