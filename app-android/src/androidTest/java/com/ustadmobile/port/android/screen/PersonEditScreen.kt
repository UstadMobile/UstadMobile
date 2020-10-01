package com.ustadmobile.port.android.screen

import androidx.test.espresso.matcher.ViewMatchers.withId
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

    val nestedView = KScrollView { withId(R.id.nested_view) }

    fun scrollToBottom(){
        nestedView.scrollToEnd()
    }

    val ClazzListRecyclerView: KRecyclerView = KRecyclerView({ withId(R.id.clazzlist_recyclerview) },
            itemTypeBuilder = {
            })

    val clazzListHeaderTextView: KTextView = KTextView { withId(R.id.clazzlist_header_textview) }

    val rolesList: KRecyclerView = KRecyclerView({ withId(R.id.roles_and_permissions_rv) },
            itemTypeBuilder = {
            })

    val roleHeaderTextView: KTextView = KTextView { withId(R.id.roles_and_permissions_header_textview) }
}