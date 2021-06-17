package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.web.KWebView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.XapiPackageContentFragment

object XapiContentScreen: KScreen<XapiContentScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_xapi_package_content
    override val viewClass: Class<*>?
        get() = XapiPackageContentFragment::class.java

    val webView = KWebView { withId(R.id.activity_xapi_package_webview) }
}