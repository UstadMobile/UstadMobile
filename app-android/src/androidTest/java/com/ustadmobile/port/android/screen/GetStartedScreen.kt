package com.ustadmobile.port.android.screen

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.common.views.KView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.GetStartedFragment

object GetStartedScreen : KScreen<GetStartedScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_get_started
    override val viewClass: Class<*>?
        get() = GetStartedFragment::class.java

    val publicLibView = KView { withId(R.id.use_public_library_view)}

    val workspaceView = KView { withId(R.id.join_workspace_view)}

    val createWorkSpaceView = KView { withId(R.id.create_workspace_view)}

}