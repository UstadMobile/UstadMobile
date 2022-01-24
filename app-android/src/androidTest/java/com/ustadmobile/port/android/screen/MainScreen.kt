package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.bottomnav.KBottomNavigationView
import io.github.kakaocup.kakao.common.views.KView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.MainActivity

object MainScreen : KScreen<MainScreen>() {

    override val layoutId: Int?
        get() = R.layout.activity_main
    override val viewClass: Class<*>?
        get() = MainActivity::class.java

    val fab: KView = KView {
        withId(R.id.activity_main_extendedfab)
    }

    val toolBarTitle: KView = KView { withId(R.id.toolbar)}

    val bottomNav = KBottomNavigationView { withId(R.id.bottom_nav_view)}

    val menuDone = KView { withId(R.id.menu_done)}

}