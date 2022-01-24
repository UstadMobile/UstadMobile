package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KTextView
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SiteDetailFragment
import org.hamcrest.Matcher

object  SiteDetailScreen : KScreen<SiteDetailScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_site_detail

    override val viewClass: Class<*>?
        get() = SiteDetailFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::SiteDetailItem)
    })

    class SiteDetailItem(parent: Matcher<View>) : KRecyclerItem<SiteDetailItem>(parent) {
        val siteName: KTextView = KTextView(parent) {
            withId(R.id.site_name_text)
        }
    }

}