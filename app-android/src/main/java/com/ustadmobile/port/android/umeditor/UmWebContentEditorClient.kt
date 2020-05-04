package com.ustadmobile.port.android.umeditor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ustadmobile.core.controller.ContentEditorPresenterCommon
import com.ustadmobile.core.impl.UMAndroidUtil.getMimeType
import com.ustadmobile.core.util.UMFileUtil.joinPaths

import com.ustadmobile.core.view.ContentEditorView

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

import com.ustadmobile.core.view.ContentEditorView.Companion.RESOURCE_JS_TINYMCE
import com.ustadmobile.core.view.ContentEditorView.Companion.RESOURCE_JS_USTAD_EDITOR

/**
 * Class which handles HTTP request from WebView and native-to-js client interaction
 *
 * **Note: Operation Flow**
 *
 * Use [UmWebContentEditorClient.shouldInterceptRequest] to intercept
 * requested resources via HTTP.
 *
 * Use [UmWebContentEditorClient.executeJsFunction] to execute Javascript
 * function from native android and wait for the callback if execution
 * log a message or return value
 *
 * @author kileha3
 */
class UmWebContentEditorClient
/**
 * @param context application context
 */
(private val context: Context, isPreview: Boolean) : WebViewClient() {

    private var isPreview = false

    private val resourceTag = arrayOf("plugin", "skin", "theme", "font",
            "templates", "locale", "material-icon")

    init {
        this.isPreview = isPreview
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val inputStream: InputStream
        val resourceUri = request.url.toString()
        var mimeType = getMimeType(context, request.url)
        if (mimeType == null && resourceUri.endsWith(".json")) {
            mimeType = "application/json"
        }

        if (isInnerResource(resourceUri) || isUmEditorResource(resourceUri)) {
            try {
                val resourcePath = joinPaths("http", ContentEditorPresenterCommon.EDITOR_BASE_DIR_NAME,
                        getResourcePath(resourceUri))
                inputStream = context.assets.open(resourcePath)
                return WebResourceResponse(mimeType, "utf-8", 200,
                        "OK", request.requestHeaders, inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return super.shouldInterceptRequest(view, request)
    }

    private fun getResourcePath(requestUri: String): String {
        val parts = requestUri.split("/")
        val newParts = ArrayList<String>()
        for (i in parts.indices) {
            if (i > 5) {
                newParts.add(parts[i])
            }
        }
        val url = newParts.toTypedArray<String>()
        return joinPaths(*url)
    }


    /**
     * Check if the resource is from plugin calls.
     * @param uri requested resource uri
     * @return true if are tinymce calls otherwise false.
     */
    private fun isInnerResource(uri: String): Boolean {
        for (resource in resourceTag) {
            if (uri.contains(resource) && !uri.contains(ContentEditorView.RESOURCE_JS_USTAD_WIDGET)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if the resource is one of the Editor core resource.
     * @param uri requested resource uri
     * @return true if are editor core resource otherwise false.
     */
    private fun isUmEditorResource(uri: String): Boolean {
        return uri.contains(RESOURCE_JS_TINYMCE) || uri.contains(RESOURCE_JS_USTAD_EDITOR)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {

        return if (url == null || !(url.startsWith("http://") || url.startsWith("https://")) || !isPreview) {
            super.shouldOverrideUrlLoading(view, url)
        } else {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        }
    }

    companion object {

        /**
         * Execute js function from native android
         *
         * @param mWeb Current active WebView instance
         * @param function name of the function to be executed
         * @param callback listener
         * @param params params to be passed to the function
         */
        fun executeJsFunction(mWeb: WebView, function: String,
                              callback: UmWebContentEditorChromeClient.JsLoadingCallback,
                              vararg params: String) {
            val mBuilder = StringBuilder()
            mBuilder.append("javascript:try{")
            mBuilder.append(function)
            mBuilder.append("(")
            var separator = ""
            if (params.isNotEmpty()) {
                for (param in params) {
                    mBuilder.append(separator)
                    separator = ","
                    mBuilder.append("\"")
                    mBuilder.append(param)
                    mBuilder.append("\"")
                }
            }
            mBuilder.append(")}catch(error){console.error(error.message);}")
            val call = mBuilder.toString()
            mWeb.evaluateJavascript(call) { callback.onCallbackReceived(it) }
        }
    }
}

