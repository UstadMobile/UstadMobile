package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.CourseTerminologyListFragment
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object CourseTerminologyListScreen : KScreen<CourseTerminologyListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = CourseTerminologyListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::CourseTerminology)
    })


    class CourseTerminology(parent: Matcher<View>) : KRecyclerItem<CourseTerminology>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.line1_text) }
    }

}