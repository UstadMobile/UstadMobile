package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.UstadView
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse

/**
 * Created by mike on 2/15/18.
 */


class H5PContentPresenter(context: Any, arguments: Map<String, String?>, view: H5PContentView) :
        UstadBaseController<H5PContentView>(context, arguments, view) {

    private var containerUid = 0L

    private var h5pDistMountUrl: String? = null

    private var h5pFileMountUrl: String? = null


    @ImplicitReflectionSerializer
    private val mH5PDistMountedCallback = object : UmCallback<String> {
        override fun onSuccess(result: String?) {
            h5pDistMountUrl = result
            view.mountH5PContainer(containerUid, h5PFileMountedCallback)
        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    @ImplicitReflectionSerializer
    private val h5PFileMountedCallback = object : UmCallback<String> {
        override fun onSuccess(result: String?) {
            h5pFileMountUrl = result
            UstadMobileSystemImpl.instance.getAsset(context,
                    "/com/ustadmobile/core/h5p/contentframe.html", contentFrameLoadedCallback)
        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    @ImplicitReflectionSerializer
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
                    val h5PJsonRequest = UMFileUtil.joinPaths(h5pFileMountUrl!!, "h5p.json")

                    GlobalScope.launch {
                        val client = HttpClient()
                        val h4PContent = client.get<String>(h5PJsonRequest)
                        val jsonObj = Json.parse<Map<String, String>>(h4PContent)
                        view.runOnUiThread(Runnable {
                            view.setTitle(jsonObj["title"].toString())
                        })

                    }
                } catch (e: IOException) {
                    dumpException(e)
                }
            }

        }

        override fun onFailure(exception: Throwable?) {

        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        this.containerUid = arguments[UstadView.ARG_CONTAINER_UID]!!.toLong()
        view.mountH5PDist(mH5PDistMountedCallback)
    }

}
