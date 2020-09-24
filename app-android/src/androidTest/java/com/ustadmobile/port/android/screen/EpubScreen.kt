package com.ustadmobile.port.android.screen

import android.view.View
import android.webkit.WebView
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.agoda.kakao.web.KWebView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.EpubContentActivity
import org.hamcrest.Matcher

object EpubScreen:  KScreen<EpubScreen>() {

    override val layoutId: Int?
        get() = R.layout.activity_epub_content
    override val viewClass: Class<*>?
        get() = EpubContentActivity::class.java


    val epubTitle: KTextView = KTextView { withId(R.id.item_basepoint_cover_title) }

    val toolBarTitle: KView = KView { withId(R.id.toolbar)}

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.container_epubrunner_pager)
    }, itemTypeBuilder = {
        itemType(::EpubPage)
    })

    class EpubPage(parent: Matcher<View>) : KRecyclerItem<EpubPage>(parent)


}