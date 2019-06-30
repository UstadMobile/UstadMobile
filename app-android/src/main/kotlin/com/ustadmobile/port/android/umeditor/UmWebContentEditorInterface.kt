package com.ustadmobile.port.android.umeditor

import android.app.Activity
import android.webkit.JavascriptInterface


/**
 * Class which listen for all the calls that javascript will make to the native side.
 *
 * **Operational flow**
 *
 *
 * On JS side make sure you call the method with UmCotentEditor,
 * i.e UmCotentEditor.onContentChanged so that below methods can
 * be invoked on android native.
 *
 *
 * @author kileha3
 */
class UmWebContentEditorInterface
/** Instantiate the interface and set the context  */
(private val activity: Activity,
 private val callback: UmWebContentEditorChromeClient.JsLoadingCallback) {


    /**
     * Handle all JS callback to native interface
     * @param callbackValue value passed from JS side
     */
    @JavascriptInterface
    fun handleJsCallbackValue(callbackValue: String) {
        try {
            activity.runOnUiThread { callback.onCallbackReceived(callbackValue) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
