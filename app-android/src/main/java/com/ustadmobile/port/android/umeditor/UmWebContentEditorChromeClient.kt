package com.ustadmobile.port.android.umeditor

import android.webkit.WebView
import com.ustadmobile.core.impl.UMLog
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient


/**
 * Class which is responsible to handle all page loads and native-to-js client interaction.
 * It is also responsible to handle all Javascript console logs.
 *
 * @author kileha3
 */
class UmWebContentEditorChromeClient
/**
 * Constructor which is used when creating an instance of this class.
 * @param callback Callback to handle all page loads
 */
(private val callback: JsLoadingCallback) : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        UMLog.l(UMLog.DEBUG, 700, consoleMessage.message())
        return true
    }

    /**
     * Listen for the page load progress change and notifies the UI
     * @param view WebView in which the page is currently loading
     * @param newProgress new progress percentage
     */
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        if (newProgress == 100) {
            callback.onPageFinishedLoading()
        }

        callback.onProgressChanged(newProgress)

    }


    /**
     * Interface which listens for the page loads and values when js function are executed.
     */
    interface JsLoadingCallback {
        /**
         * Invoked when page loading progress changes
         * @param newProgress new progress value
         */
        fun onProgressChanged(newProgress: Int)

        /**
         * Invoked when page has finished loading
         */
        fun onPageFinishedLoading()

        /**
         * Invoked when return value or console message is created.
         * @param value valued to be passed to the native android
         */
        fun onCallbackReceived(value: String)

    }


}
