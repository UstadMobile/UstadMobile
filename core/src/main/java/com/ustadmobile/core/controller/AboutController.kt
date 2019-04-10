package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.AboutView
import java.io.IOException
import java.io.InputStream

/**
 * Created by mike on 12/27/16.
 */

class AboutController(context: Any, args: Map<String, String>?, view: AboutView)
    : UstadBaseController<AboutView>(context, args!!, view) {

    private var aboutHTMLStr: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val impl = UstadMobileSystemImpl.instance

        impl.getAsset(context!!, "com/ustadmobile/core/about.html", object : UmCallback<InputStream> {
            override fun onSuccess(result: InputStream) {
                try {
                    aboutHTMLStr = UMIOUtils.readStreamToString(result)
                    view?.setAboutHTML(aboutHTMLStr!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(exception: Throwable) {

            }
        })


        view?.setVersionInfo(impl.getVersion(context!!) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context!!)))

        view?.setVersionInfo(impl.getVersion(context!!) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context!!)))
        view?.setAboutHTML(aboutHTMLStr!!)
    }

}
