package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.agoda.kakao.text.KTextView
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SiteDetailFragment

object  WorkspaceDetailScreen : KScreen<WorkspaceDetailScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_person_detail
    override val viewClass: Class<*>?
        get() = SiteDetailFragment::class.java

    val title = KTextView { withId(R.id.entity_title) }

}