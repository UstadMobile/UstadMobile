package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ParentalConsentManagementFragment

object ParentalConsentManagementScreen : KScreen<ParentalConsentManagementScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_parental_consent_management

    override val viewClass: Class<*>
        get() = ParentalConsentManagementFragment::class.java

    val relationshipTextInput = KTextView { withId(R.id.relationship_value) }

    val consentButton = KButton { withId(R.id.consent_button) }

    val dontConsentButton = KButton { withId(R.id.dont_consent_button) }

    val changeConsentButton = KButton { withId(R.id.change_consent_button) }


}