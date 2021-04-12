package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.StatementListViewFragment
import org.hamcrest.Matcher

object StatementListScreen : KScreen<StatementListScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = StatementListViewFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::PersonWithSessionDetail)
    })


    class PersonWithSessionDetail(parent: Matcher<View>) : KRecyclerItem<PersonWithSessionDetail>(parent) {
        val verbTitle: KTextView = KTextView(parent) { withId(R.id.item_person_verb_title) }
        val objectTitle: KTextView = KTextView(parent) { withId(R.id.item_person_object_description) }
        val scoreText: KTextView = KTextView(parent) { withId(R.id.item_person_progress) }
        val scoreResults: KTextView = KTextView(parent) { withId(R.id.item_person_score_results) }

    }

}
