package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.AboutView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.InputStream

/**
 * Created by mike on 12/27/16.
 */

class AboutPresenter(context: Any, args: Map<String, String>?, view: AboutView,
                     val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<AboutView>(context, args!!, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            var aboutInputStream : InputStream? = null
            try {
                aboutInputStream = impl.getAssetInputStreamAsync(context, "com/ustadmobile/core/about.html")
                val aboutText = UMIOUtils.readStreamToString(aboutInputStream)
                view.runOnUiThread(Runnable {
                    view.setAboutHTML(aboutText)
                })
            }finally {
                aboutInputStream?.close()
            }
        }

        val currentApiUrl:String = impl.getAppConfigString("apiUrl",
                "http://localhost", context).toString()
        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)) + "\n" +
                "API: " + currentApiUrl + "\n")
    }

}
