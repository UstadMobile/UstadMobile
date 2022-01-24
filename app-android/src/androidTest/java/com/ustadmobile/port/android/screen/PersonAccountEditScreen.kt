package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.PersonAccountEditFragment
import io.github.kakaocup.kakao.edit.KTextInputLayout

object PersonAccountEditScreen : KScreen<PersonAccountEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_person_account_edit
    override val viewClass: Class<*>?
        get() = PersonAccountEditFragment::class.java

    val usernameTextInput = KTextInputLayout { withId(R.id.username_textinputlayout)}

    val currentPasswordTextInput = KTextInputLayout { withId(R.id.current_password_textinputlayout)}

    val newPasswordTextInput = KTextInputLayout { withId(R.id.new_password_textinputlayout)}

    val confirmNewPassTextInput = KTextInputLayout { withId(R.id.confirm_password_textinputlayout)}


}