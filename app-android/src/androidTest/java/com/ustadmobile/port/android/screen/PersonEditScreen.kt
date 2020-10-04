package com.ustadmobile.port.android.screen

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.common.views.KSwipeView
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.scroll.KScrollView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.PersonEditFragment

object PersonEditScreen : KScreen<PersonEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_person_edit
    override val viewClass: Class<*>?
        get() = PersonEditFragment::class.java

    val nestedView = KSwipeView { withId(R.id.nested_view) }

    fun scrollToBottom() {
        nestedView.swipeUp()
    }

    val clazzListRecyclerView: KRecyclerView = KRecyclerView({ withId(R.id.clazzlist_recyclerview) },
            itemTypeBuilder = {
            })

    val clazzListHeaderTextView: KTextView = KTextView { withId(R.id.clazzlist_header_textview) }

    val rolesList: KRecyclerView = KRecyclerView({ withId(R.id.roles_and_permissions_rv) },
            itemTypeBuilder = {
            })

    val roleHeaderTextView: KTextView = KTextView { withId(R.id.roles_and_permissions_header_textview) }

    val usernameTextInput = KTextInputLayout { withId(R.id.username_textinputlayout) }

    val passwordTextInput = KTextInputLayout { withId(R.id.username_textinputlayout) }

    val confirmPassTextInput = KTextInputLayout { withId(R.id.confirm_password_textinputlayout) }

    val birthdayTextInput = KTextInputLayout { withId(R.id.birthday_textinputlayout) }

    val genderValue = KView { withId(R.id.gender_value) }

    fun fillGender(message: String) {

    }

}