package com.ustadmobile.port.android.screen

import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzEditFragment

object ClazzEditScreen : KScreen<ClazzEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_edit
    override val viewClass: Class<*>?
        get() = ClazzEditFragment::class.java

    val editNameLayout = KTextInputLayout { withId(R.id.activity_clazz_edit_name)}


}