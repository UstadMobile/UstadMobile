package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentEditFragment
import io.github.kakaocup.kakao.common.views.KSwipeView
import io.github.kakaocup.kakao.edit.KTextInputLayout

object ClazzAssignmentEditScreen : KScreen<ClazzAssignmentEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_assignment_edit
    override val viewClass: Class<*>?
        get() = ClazzAssignmentEditFragment::class.java


    val nestedScroll = KSwipeView { withId(R.id.fragment_clazz_assignment_edit_edit_scroll) }

    val clazzAssignmentTitleInput = KTextInputLayout { withId(R.id.ca_title_input)}

    val clazzAssignmentDescInput = KTextInputLayout {withId(R.id.ca_edit_description_textinput)}

    val caStartDateText = KTextInputLayout { withId(R.id.ca_start_date_textinput)}

}