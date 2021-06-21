package com.ustadmobile.port.android.screen

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.SiteEnterLinkFragment
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule

object SiteEnterLinkScreen : KScreen<SiteEnterLinkScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_site_enter_link
    override val viewClass: Class<*>?
        get() = SiteEnterLinkFragment::class.java

    val enterLinkTextInput = KTextInputLayout { withId(R.id.site_link_view)}

    val nextButton = KButton { withId(R.id.next_button)}

    fun launchFragment(workSpaceLink: String, systemImpl: SystemImplTestNavHostRule){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            SiteEnterLinkFragment().also {
                it.installNavController(systemImpl.navController)
            }
        }

        enterLinkTextInput{
            edit{
                typeText(workSpaceLink)
            }
        }

    }


}