package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.port.android.view.PersonAccountEditFragment
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule

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