package com.ustadmobile.port.android.screen

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.WorkspaceEnterLinkFragment
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule

object WorkSpaceEnterLinkScreen : KScreen<WorkSpaceEnterLinkScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_work_space_enter_link
    override val viewClass: Class<*>?
        get() = WorkspaceEnterLinkFragment::class.java

    val enterLinkTextInput = KTextInputLayout { withId(R.id.workspace_link_view)}

    val nextButton = KButton { withId(R.id.next_button)}

    fun launchFragment(workSpaceLink: String, systemImpl: SystemImplTestNavHostRule){
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            WorkspaceEnterLinkFragment().also {
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