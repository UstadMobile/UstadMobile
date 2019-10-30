package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.AboutView
import kotlinx.io.IOException
import kotlinx.io.InputStream

/**
 * Created by mike on 12/27/16.
 */

class AboutPresenter(context: Any, args: Map<String, String>?, view: AboutView,
                     val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<AboutView>(context, args!!, view) {

    private var aboutHTMLStr: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        impl.getAsset(context, "com/ustadmobile/core/about.html", object : UmCallback<InputStream> {
            override fun onSuccess(result: InputStream?) {
                if (result != null) {
                    try {
                        aboutHTMLStr = UMIOUtils.readStreamToString(result)
                        view.setAboutHTML(aboutHTMLStr!!)
                    } catch (e: IOException) {
                        dumpException(e)
                    }
                }

            }

            override fun onFailure(exception: Throwable?) {
                if (exception != null) {
                    dumpException(exception)
                }
            }
        })


        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)))

        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)))
        view.setAboutHTML(aboutHTMLStr!!)

        val currentApiUrl:String = impl.getAppConfigString("apiUrl",
                "http://localhost", context).toString()
        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)) + "\n" +
                "API: " + currentApiUrl + "\n")
    }

}
