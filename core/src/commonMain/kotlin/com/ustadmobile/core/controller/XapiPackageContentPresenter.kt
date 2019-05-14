package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ShowErrorUmCallback
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.http.ShowErrorUmHttpResponseCallback
import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.XapiPackageContentView
import kotlinx.coroutines.Runnable
import kotlinx.io.*
import org.kmp.io.KMPPullParserException

/**
 * Created by mike on 9/13/17.
 *
 * Displays an XAPI Zip Package.
 *
 * Pass EpubContentPresenter.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
class XapiPackageContentPresenter(context: Any, args: Map<String, String>?, view: XapiPackageContentView)
    : UstadBaseController<XapiPackageContentView>(context, args!!, view) {

    private var mountedPath: String? = null

    private var tinCanXml: TinCanXML? = null

    private var launchHref: String? = null

    private var launchUrl: String? = null

    private var registrationUUID: String? = null

    private inner class ZipMountedCallbackHandler : ShowErrorUmCallback<String>(view, MessageID.error_opening_file) {

        override fun onSuccess(result: String?) {
            mountedPath = result
            UstadMobileSystemImpl.instance.makeRequestAsync(UmHttpRequest(
                    context,
                    UMFileUtil.joinPaths(mountedPath!!, "tincan.xml")),
                    TinCanResponseCallback())
        }
    }

    private inner class TinCanResponseCallback : ShowErrorUmHttpResponseCallback(view, MessageID.error_opening_file) {

        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            super.onComplete(call, response)
            if (response.isSuccessful) {
                try {
                    handleTinCanXmlLoaded(response.responseBody!!)
                } catch (e: IOException) {
                    UMLog.l(UMLog.ERROR, 75, null, e)
                    onFailure(call, e)
                } catch (e: KMPPullParserException) {
                    UMLog.l(UMLog.ERROR, 75, null, e)
                    onFailure(call, e)
                }

            }
        }
    }

    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)
        registrationUUID = UMUUID.randomUUID().toString()
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]!!.toLong()
        view.mountContainer(containerUid, ZipMountedCallbackHandler())
    }

    private fun handleTinCanXmlLoaded(tincanXmlBytes: ByteArray) {
        val xpp = UstadMobileSystemImpl.instance.newPullParser(
                ByteArrayInputStream(tincanXmlBytes), "UTF-8")
        tinCanXml = TinCanXML.loadFromXML(xpp)
        launchHref = tinCanXml?.launchActivity?.launchUrl
        launchUrl = UMFileUtil.joinPaths(mountedPath!!, launchHref!!)
        view.runOnUiThread(Runnable {
            view.setTitle(tinCanXml!!.launchActivity?.name!!)
            view.loadUrl(launchUrl!!)
        })
    }

}
