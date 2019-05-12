package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.impl.http.UmHttpResponseCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.Runnable

import org.json.JSONObject

import java.io.IOException
import java.io.InputStream
import java.util.HashMap
import kotlinx.io.*

/**
 * Created by mike on 2/15/18.
 */

class H5PContentPresenter(context: Any, arguments: Map<String, String?>, view: H5PContentView) :
        UstadBaseController<H5PContentView>(context, arguments, view) {

    private var containerUid = 0L

    private var h5pDistMountUrl: String? = null

    private var h5pFileMountUrl: String? = null


    private val mH5PDistMountedCallback = object : UmCallback<String> {
        override fun onSuccess(result: String?) {
            h5pDistMountUrl = result
            view.mountH5PContainer(containerUid, h5PFileMountedCallback)
        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    private val h5PFileMountedCallback = object : UmCallback<String> {
        override fun onSuccess(result: String?) {
            h5pFileMountUrl = result
            UstadMobileSystemImpl.instance.getAsset(context,
                    "/com/ustadmobile/core/h5p/contentframe.html", contentFrameLoadedCallback)
        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    private val contentFrameLoadedCallback = object : UmCallback<InputStream> {

        override fun onSuccess(result: InputStream?) {
            if (result != null) {
                try {
                    var htmlStr = UMIOUtils.readStreamToString(result)
                    htmlStr = htmlStr.replace("\$DISTPATH", h5pDistMountUrl!!)
                    val h5pMountUrl2 = h5pFileMountUrl!!.substring(0, h5pFileMountUrl!!.length - 1)
                    val subHtmlStr = htmlStr.replace("\$CONTENTPATH", h5pMountUrl2)
                    view.runOnUiThread(Runnable {
                        view.setContentHtml(h5pFileMountUrl!!, subHtmlStr)
                    })
                    val h5PJsonRequest = UmHttpRequest(context,
                            UMFileUtil.joinPaths(h5pFileMountUrl!!, "h5p.json"))
                    UstadMobileSystemImpl.instance.makeRequestAsync(h5PJsonRequest, h5pResponseCallback)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    private val h5pResponseCallback = object : UmHttpResponseCallback {
        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            try {
                if (response.isSuccessful) {
                    val jsonStr = UMIOUtils.readStreamToString(response.responseAsStream!!)
                    val jsonObj = JSONObject(jsonStr)
                    view.runOnUiThread(Runnable {
                        view.setTitle(
                                jsonObj.getString("title"))
                    })
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        override fun onFailure(call: UmHttpCall, exception: IOException) {

        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        this.containerUid = arguments[UstadView.ARG_CONTAINER_UID]!!.toLong()
        view.mountH5PDist(mH5PDistMountedCallback)
    }

}
