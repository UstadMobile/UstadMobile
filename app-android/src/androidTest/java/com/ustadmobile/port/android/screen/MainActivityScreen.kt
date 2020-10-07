package com.ustadmobile.port.android.screen

import com.agoda.kakao.bottomnav.KBottomNavigationView
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.MainActivity

object MainActivityScreen : KScreen<MainActivityScreen>() {

    override val layoutId: Int?
        get() = R.layout.activity_main
    override val viewClass: Class<*>?
        get() = MainActivity::class.java

    val fab: KView = KView {
        withId(R.id.activity_listfragmelayout_behaviornt_fab)
    }

    val toolBarTitle: KView = KView { withId(R.id.toolbar)}

    val bottomNav = KBottomNavigationView { withId(R.id.bottom_nav_view)}

}