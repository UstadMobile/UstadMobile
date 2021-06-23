package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzDetailOverviewFragment

object ClazzDetailOverviewScreen : KScreen<ClazzDetailOverviewScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_detail_overview
    override val viewClass: Class<*>?
        get() = ClazzDetailOverviewFragment::class.java

    val clazzDescTextView = KTextView { withId(R.id.fragment_clazz_detail_overview_description_text)}
}