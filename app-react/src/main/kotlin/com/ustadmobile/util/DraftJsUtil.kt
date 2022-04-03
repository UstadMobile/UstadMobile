package com.ustadmobile.util

/**
 * JavaScript rich text editor framework, built for React and backed by an immutable model.
 * It will be used as peer dependency to MuiHtmlEditor, it has some util function used by
 * MuiHtmlEditor to format contents
 */
@JsModule("draft-js")
@JsNonModule
external val draftJsModule: dynamic


/**
 * Draft-js Helper utils functions to export editor state contents to universal HTML content
 */
@JsModule("draft-js-export-html")
@JsNonModule
external val draftExportHtmlModule: dynamic


@JsModule("striptags")
@JsNonModule
external val stripTagsJsModule: dynamic

object DraftJsUtil {

    /**
     * Format html/string to a Draft-Js content state
     * @param data This can be plain string or html
     */
    fun convertDataToEditorState(data: String): String {
        val contentHTML =  draftJsModule.convertFromHTML(data)
        val state = draftJsModule.ContentState.createFromBlockArray(contentHTML.contentBlocks,
            contentHTML.entityMap)
        return JSON.stringify(draftJsModule.convertToRaw(state))
    }

    /**
     * Convert Draft-Js content state to a well html formatted string to share across
     * platforms
     * @param content This is Draft-Js Mui editor content state
     */
    fun convertEditorContentToHtml(content: dynamic): String {
        return draftExportHtmlModule.stateToHTML(content).toString()
    }

    /**
     * Strip all html tags from a string
     * @param html Html string
     * @return a clean string without html tags
     */
    fun clean(html: String): String {
        return stripTagsJsModule(html).toString()
    }
}