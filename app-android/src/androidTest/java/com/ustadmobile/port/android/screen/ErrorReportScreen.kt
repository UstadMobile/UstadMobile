package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R

object ErrorReportScreen : KScreen<ErrorReportScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_error_report

    override val viewClass: Class<*>
        get() = ErrorReportScreen::class.java


    val errorCodeText = KTextView {
        withId(R.id.error_code_text)
    }

    val incidentIdText = KTextView {
        withId(R.id.incident_id_text)
    }

}