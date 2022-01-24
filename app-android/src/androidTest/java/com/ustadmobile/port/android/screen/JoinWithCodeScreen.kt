package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.JoinWithCodeFragment

object JoinWithCodeScreen : KScreen<JoinWithCodeScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_join_with_code
    override val viewClass: Class<*>?
        get() = JoinWithCodeFragment::class.java

    val codeEditLayout = KTextInputLayout { withId(R.id.code_input_textinputlayout)}
}