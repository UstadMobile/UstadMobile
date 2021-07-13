package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.progress.KProgressBar
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntryDetailOverviewFragment

object ContentEntryDetailScreen : KScreen<ContentEntryDetailScreen>() {
    override val layoutId: Int
        get() = R.layout.fragment_content_entry2_detail
    override val viewClass: Class<*>?
        get() = ContentEntryDetailOverviewFragment::class.java

    val entryTitleTextView = KTextView { withId(R.id.entry_detail_title)}

    val progress = KProgressBar { withId(R.id.entry_detail_progress_bar)}

    val progressCheck = KImageView { withId(R.id.content_progress_fail_correct)}

    val translationsList: KRecyclerView = KRecyclerView({
        withId(R.id.availableTranslationView)
    }, itemTypeBuilder = {

    })

}