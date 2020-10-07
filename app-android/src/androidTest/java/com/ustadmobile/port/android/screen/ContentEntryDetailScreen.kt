package com.ustadmobile.port.android.screen

import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntry2DetailFragment

object ContentEntryDetailScreen : KScreen<ContentEntryDetailScreen>() {
    override val layoutId: Int
        get() = R.layout.fragment_content_entry2_detail
    override val viewClass: Class<*>?
        get() = ContentEntry2DetailFragment::class.java

    val groupActivityButton = KTextView { withId(R.id.entry_detail_group_activity_button) }

}