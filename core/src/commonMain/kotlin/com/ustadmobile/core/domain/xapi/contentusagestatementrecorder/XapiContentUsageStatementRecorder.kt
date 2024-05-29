package com.ustadmobile.core.domain.xapi.contentusagestatementrecorder

/**
 * Records xAPI statements for activities that don't directly generate xAPI statements themselves
 * e.g. videos, epubs, pdfs.
 *
 * On Android: uses lifecycle events.
 * On Web: Page visibility: https://html.spec.whatwg.org/multipage/interaction.html#page-visibility
 * https://stackoverflow.com/questions/1060008/is-there-a-way-to-detect-if-a-browser-window-is-not-currently-active/1060034#1060034
 *
 */
interface XapiContentUsageStatementRecorder {

    fun onExit() {

    }


}