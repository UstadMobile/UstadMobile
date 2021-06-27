package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LanguageListFragment
import org.hamcrest.Matcher

object LanguageListScreen : KScreen<LanguageListScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = LanguageListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Language)
    })


    class Language(parent: Matcher<View>) : KRecyclerItem<Language>(parent) {
        val name: KTextView = KTextView(parent) { withId(R.id.line1_text) }
        val code: KTextView = KTextView(parent) { withId(R.id.line2_text) }
    }


}