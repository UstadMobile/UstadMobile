package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PARENT_CONTACT
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PASSWORD
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_USERNAME
import com.ustadmobile.port.android.screen.RegisterMinorWaitForParentScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import org.junit.Rule
import org.junit.Test

class RegisterMinorWaitForParentFragmentTest : TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @Test
    fun givenArgsProvided_whenUserClicksTogglePassword_shouldShowAndHidePassword() {
        init {
            val args = bundleOf(ARG_USERNAME to "username",
                ARG_PARENT_CONTACT to "parent@email.com",
                ARG_PASSWORD to "secret")

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = args) {
                RegisterMinorWaitForParentFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {
            RegisterMinorWaitForParentScreen {
                usernameText {
                    hasText("username")
                }

                passwordText {
                    containsText("**")
                }

                toggleVisibilityButton {
                    click()
                }

                passwordText {
                    containsText("secret")
                }

                toggleVisibilityButton {
                    click()
                }

                passwordText {
                    containsText("**")
                }
            }
        }
    }

}