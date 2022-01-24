package com.ustadmobile.port.android.screen

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SiteEditFragment
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Site

object SiteEditScreen : KScreen<SiteEditScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_site_edit

    override val viewClass: Class<*>
        get() = SiteEditFragment::class.java

    val siteTitleInput = KTextInputLayout { withId(R.id.title_textedit) }

}