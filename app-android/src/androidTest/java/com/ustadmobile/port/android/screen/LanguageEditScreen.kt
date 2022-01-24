package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LanguageEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson

object LanguageEditScreen : KScreen<LanguageEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_language_edit
    override val viewClass: Class<*>?
        get() = LanguageEditFragment::class.java

    val languageTitleInput = KTextInputLayout { withId(R.id.lang_edit_name_textinput)}

}