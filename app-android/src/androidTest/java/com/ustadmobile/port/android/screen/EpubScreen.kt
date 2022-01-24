package com.ustadmobile.port.android.screen

import android.view.View
import android.webkit.WebView
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.web.KWebView
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
        withId(R.id.epub_page_recycler_view)
    }, itemTypeBuilder = {
        itemType(::EpubPage)
    })

    class EpubPage(parent: Matcher<View>) : KRecyclerItem<EpubPage>(parent)


}