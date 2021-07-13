package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.Login2Fragment
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object LoginScreen : KScreen<LoginScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_login2

    override val viewClass: Class<*>
        get() = Login2Fragment::class.java

    val userNameTextInput = KTextInputLayout { withId(R.id.username_view) }

    val passwordTextInput = KTextInputLayout { withId(R.id.password_view) }

    val loginButton = KButton { withId(R.id.login_button) }

    val createAccount = KButton { withId(R.id.create_account) }

    val connectAsGuest = KButton { withId(R.id.connect_as_guest) }

    val loginErrorText = KTextView { withId(R.id.login_error_text) }

    const val VALID_USER = "JohnDoe"

    const val VALID_PASS = "password"



}