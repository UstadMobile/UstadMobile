package com.ustadmobile.core.controller

import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.XapiPackageContentView
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.StringReader
import org.kmp.io.KMPXmlParser

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
class XapiPackageContentPresenter(context: Any, args: Map<String, String>, view: XapiPackageContentView,
                                  private val containerMounter: suspend (Long) -> String)
    : UstadBaseController<XapiPackageContentView>(context, args, view) {

    private var mountedPath: String? = null

    private var tinCanXml: TinCanXML? = null

    private var launchHref: String? = null

    private var launchUrl: String? = null

    private var registrationUUID: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        registrationUUID = UMUUID.randomUUID().toString()
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]!!.toLong()
        GlobalScope.launch {
            mountedPath = containerMounter(containerUid)

            val client = defaultHttpClient()
            val tincanContent = client.get<String>(UMFileUtil.joinPaths(mountedPath!!, "tincan.xml"))

            val xpp = KMPXmlParser()
            xpp.setInput(StringReader(tincanContent))
            tinCanXml = TinCanXML.loadFromXML(xpp)
            launchHref = tinCanXml?.launchActivity?.launchUrl
            launchUrl = UMFileUtil.joinPaths(mountedPath!!, launchHref!!)
            view.runOnUiThread(Runnable {
                view.setTitle(tinCanXml!!.launchActivity?.name!!)
                view.loadUrl(launchUrl!!)
            })
        }
    }

}
