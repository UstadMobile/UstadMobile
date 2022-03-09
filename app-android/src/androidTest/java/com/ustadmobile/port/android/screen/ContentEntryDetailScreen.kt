package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntryDetailOverviewFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.progress.KProgressBar
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ContentEntryDetailScreen : KScreen<ContentEntryDetailScreen>() {
    override val layoutId: Int
        get() = R.layout.fragment_content_entry2_detail
    override val viewClass: Class<*>?
        get() = ContentEntryDetailOverviewFragment::class.java

    val entryTitleTextView = KTextView { withId(R.id.entry_detail_title)}

    val downloadOpenButton = KButton { withId(R.id.entry_download_button)}

    val progress = KProgressBar { withId(R.id.entry_detail_progress_bar)}

    val progressCheck = KImageView { withId(R.id.content_progress_fail_correct)}

    val translationsList: KRecyclerView = KRecyclerView({
        withId(R.id.availableTranslationView)
    }, itemTypeBuilder = {

    })

    val progressRecycler: KRecyclerView = KRecyclerView({
        withId(R.id.contentJobItemProgressList)
    }, itemTypeBuilder = {
        itemType(::ContentJobItemProgressItem)
    })

    class ContentJobItemProgressItem(parent: Matcher<View>) : KRecyclerItem<ContentJobItemProgressItem>(parent) {
        val progress: KView = KView(parent) { withId(R.id.entryDetailProgress) }
    }

}