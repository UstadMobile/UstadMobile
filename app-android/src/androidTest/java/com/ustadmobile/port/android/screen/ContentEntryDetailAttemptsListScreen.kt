package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntryDetailAttemptsListFragment
import org.hamcrest.Matcher

object ContentEntryDetailAttemptsListScreen : KScreen<ContentEntryDetailAttemptsListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ContentEntryDetailAttemptsListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::PersonWithStatementDisplay)
    })

    class PersonWithStatementDisplay(parent: Matcher<View>) : KRecyclerItem<PersonWithStatementDisplay>(parent) {
        val personName: KTextView = KTextView(parent) { withId(R.id.item_person_text) }
        val attemptsCount: KTextView =  KTextView(parent) { withId(R.id.item_person_line2_text) }
        val progressText = KTextView(parent) { withId(R.id.attempt_progress_text)}
        val scoreText = KTextView(parent) { withId(R.id.attempt_score_text)}
    }

}
