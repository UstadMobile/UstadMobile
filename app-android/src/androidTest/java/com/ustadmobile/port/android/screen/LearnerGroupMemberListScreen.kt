package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LearnerGroupMemberListFragment
import org.hamcrest.Matcher

object LearnerGroupMemberListScreen : KScreen<LearnerGroupMemberListScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = LearnerGroupMemberListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::LearnerGroupMember)
    })

    class LearnerGroupMember(parent: Matcher<View>) : KRecyclerItem<LearnerGroupMember>(parent) {
        val memberName: KTextView = KTextView(parent) { withId(R.id.item_learnergroupmember_name) }
        val memberRole: KTextView = KTextView(parent) { withId(R.id.item_learnergroupmember_role)}
    }
}