package com.ustadmobile.port.android.screen

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.progress.KProgressBar
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntry2DetailFragment

object ContentEntryDetailScreen : KScreen<ContentEntryDetailScreen>() {
    override val layoutId: Int
        get() = R.layout.fragment_content_entry2_detail
    override val viewClass: Class<*>?
        get() = ContentEntry2DetailFragment::class.java

    val entryTitleTextView = KTextView { withId(R.id.entry_detail_title)}

    val progress = KProgressBar { withId(R.id.entry_detail_progress_bar)}

    val progressCheck = KImageView { withId(R.id.content_progress_fail_correct)}

    val translationsList: KRecyclerView = KRecyclerView({
        withId(R.id.availableTranslationView)
    }, itemTypeBuilder = {

    })

}