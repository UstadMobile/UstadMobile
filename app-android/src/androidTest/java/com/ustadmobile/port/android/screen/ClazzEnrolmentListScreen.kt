package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzEnrolmentListFragment
import org.hamcrest.Matcher

object ClazzEnrolmentListScreen : KScreen<ClazzEnrolmentListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ClazzEnrolmentListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::ClazzEnrolment)
        itemType(::Profile)
    })


    class ClazzEnrolment(parent: Matcher<View>) : KRecyclerItem<ClazzEnrolment>(parent) {
        val roleAndStatusText : KTextView = KTextView(parent) { withId(R.id.line1_text) }
        val editButton: KImageView = KImageView(parent) { withId(R.id.item_clazz_enrolment_edit_enrolment)}
        val startEndDate: KTextView = KTextView(parent) {withId(R.id.line2_text)}
    }

    class Profile(parent: Matcher<View>) : KRecyclerItem<Profile>(parent) {
        val profileButton = KTextView(parent) { withId(R.id.item_clazz_enrolment_view_profile_button)}
    }


}