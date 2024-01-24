package com.ustadmobile.core.domain.htmlcontentdisplayengine

import dev.icerock.moko.resources.StringResource

data class HtmlContentDisplayEngineOption(
    val code: Int,
    val stringResource: StringResource,
    val explanationStringResource: StringResource?,
) {

    companion object {

        const val SETTING_KEY_HTML_CONTENT_DISPLAY_ENGINE = "com.ustadmobile.htmlcontentdisplayengine"

    }

}
