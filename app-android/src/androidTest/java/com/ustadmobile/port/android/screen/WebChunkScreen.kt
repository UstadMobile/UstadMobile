package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.web.KWebView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.WebChunkFragment

object WebChunkScreen : KScreen<WebChunkScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_web_chunk

    override val viewClass: Class<*>?
        get() = WebChunkFragment::class.java

    val webView = KWebView { withId(R.id.webchunk_webview)}


}