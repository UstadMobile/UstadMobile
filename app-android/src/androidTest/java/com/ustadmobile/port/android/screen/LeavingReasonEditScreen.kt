package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LeavingReasonEditFragment

object LeavingReasonEditScreen : KScreen<LeavingReasonEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_leaving_reason_edit
    override val viewClass: Class<*>?
        get() = LeavingReasonEditFragment::class.java

    val LeavingReasonTitleInput = KTextInputLayout { withId(R.id.leaving_reason_edit_description_textinput)}

}