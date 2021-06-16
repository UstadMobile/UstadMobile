package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.launchFragmentInContainer
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.port.android.view.Login2Fragment
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import kotlinx.serialization.json.Json

object LoginScreen : KScreen<LoginScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_login2
    override val viewClass: Class<*>?
        get() = Login2Fragment::class.java

    val userNameTextInput = KTextInputLayout { withId(R.id.username_view) }

    val passwordTextInput = KTextInputLayout { withId(R.id.password_view) }

    val loginButton = KButton { withId(R.id.login_button) }

    val createAccount = KButton { withId(R.id.create_account) }

    val connectAsGuest = KButton { withId(R.id.connect_as_guest) }

    val loginErrorText = KTextView { withId(R.id.login_error_text) }

    const val VALID_USER = "JohnDoe"

    const val VALID_PASS = "password"

    fun launchFragment(serverUrl: String? = null, fillAllFields: Boolean = false,
                       registration: Boolean = false, guestConnection: Boolean = false, systemImplNavRule: SystemImplTestNavHostRule) {

        val workspace = Site().apply {
            siteName = ""
            guestLogin = guestConnection
            registrationAllowed = registration
        }
        val args = mapOf(UstadView.ARG_SITE to Json.encodeToString(Site.serializer(), workspace))
        val bundle = args.plus(mapOf(UstadView.ARG_SERVER_URL to serverUrl,
                UstadView.ARG_NEXT to ContentEntryListTabsView.VIEW_NAME) as Map<String, String>).toBundle()

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
            Login2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        if (fillAllFields) {
            userNameTextInput {
                edit {
                    typeText(this@LoginScreen.VALID_USER)
                }
            }
            passwordTextInput {
                edit {
                    typeText(this@LoginScreen.VALID_PASS)
                }
            }
        }
        closeSoftKeyboard()

        loginButton {
            click()
        }
    }


}