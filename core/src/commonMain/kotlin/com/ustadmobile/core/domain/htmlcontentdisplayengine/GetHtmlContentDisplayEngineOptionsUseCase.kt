package com.ustadmobile.core.domain.htmlcontentdisplayengine

class GetHtmlContentDisplayEngineOptionsUseCase(
    val optionsList: List<HtmlContentDisplayEngineOption>
) {
    operator fun invoke(): List<HtmlContentDisplayEngineOption> = optionsList

}