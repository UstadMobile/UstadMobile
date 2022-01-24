package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KTextView
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.@BaseFileName@Fragment

object  @BaseFileName@Screen : KScreen<@BaseFileName@Screen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_person_detail
    override val viewClass: Class<*>?
        get() = @BaseFileName@Fragment::class.java

    val title = KTextView { withId(R.id.entity_title) }

}