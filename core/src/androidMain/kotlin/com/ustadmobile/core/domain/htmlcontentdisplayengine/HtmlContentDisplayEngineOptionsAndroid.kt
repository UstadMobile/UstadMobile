package com.ustadmobile.core.domain.htmlcontentdisplayengine
import com.ustadmobile.core.MR

const val HTML_ENGINE_USE_CHROMETAB = 1

const val HTML_ENGINE_USE_WEBVIEW = 2

val HTML_CONTENT_OPTIONS_ANDROID = listOf(
    HtmlContentDisplayEngineOption(HTML_ENGINE_USE_CHROMETAB, MR.strings.chrome_recommended, MR.strings.chrome_recommended_info),
    HtmlContentDisplayEngineOption(HTML_ENGINE_USE_WEBVIEW, MR.strings.internal_webview, MR.strings.internal_webview_info),
)