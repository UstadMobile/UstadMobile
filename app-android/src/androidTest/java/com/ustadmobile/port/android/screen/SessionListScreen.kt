package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SessionListFragment
import org.hamcrest.Matcher

object SessionListScreen : KScreen<SessionListScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = SessionListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::PersonWithSession)
    })


    class PersonWithSession(parent: Matcher<View>) : KRecyclerItem<PersonWithSession>(parent) {
        val successStatusText: KTextView = KTextView(parent) { withId(R.id.item_person_success_complete) }
        val scoreText = KTextView(parent) {withId(R.id.item_person_progress)}
        val scoreResults = KTextView(parent) {withId(R.id.item_person_score_results)}
    }

}
