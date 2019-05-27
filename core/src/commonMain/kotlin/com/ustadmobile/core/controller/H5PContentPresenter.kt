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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.serialization.internal.defaultSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import kotlinx.serialization.serializer
import kotlinx.serialization.stringFromUtf8Bytes

/**
 * Created by mike on 2/15/18.
 */


class H5PContentPresenter(context: Any, arguments: Map<String, String?>, view: H5PContentView) :
        H5PContentPresenterBase(context, arguments, view) {

    private var containerUid = 0L

    private var h5pDistMountUrl: String? = null

    private var h5pFileMountUrl: String? = null


//    //@ImplicitReflectionSerializer
//    private val mH5PDistMountedCallback = object : UmCallback<String> {
//        override fun onSuccess(result: String?) {
//            h5pDistMountUrl = result
//            view.mountH5PContainer(containerUid, h5PFileMountedCallback)
//        }
//
//        override fun onFailure(exception: Throwable?) {
//
//        }
//    }
//
//    //@ImplicitReflectionSerializer
//    private val h5PFileMountedCallback = object : UmCallback<String> {
//        override fun onSuccess(result: String?) {
//            h5pFileMountUrl = result
//            UstadMobileSystemImpl.instance.getAsset(context,
//                    "/com/ustadmobile/core/h5p/contentframe.html", contentFrameLoadedCallback)
//        }
//
//        override fun onFailure(exception: Throwable?) {
//
//        }
//    }
//
//    private val contentFrameLoadedCallback = object : UmCallback<InputStream> {
//
////        @ImplicitReflectionSerializer
////        private fun getTitle(jsonStr: String) :String? {
////            val jsonObj = Json.parse<Map<String, String>>(jsonStr)
////            return jsonObj["title"]
////        }
//
//        override fun onSuccess(result: InputStream?) {
//            if (result != null) {
//                try {
//                    var htmlStr = UMIOUtils.readStreamToString(result)
//                    htmlStr = htmlStr.replace("\$DISTPATH", h5pDistMountUrl!!)
//                    val h5pMountUrl2 = h5pFileMountUrl!!.substring(0, h5pFileMountUrl!!.length - 1)
//                    val subHtmlStr = htmlStr.replace("\$CONTENTPATH", h5pMountUrl2)
//                    view.runOnUiThread(Runnable {
//                        view.setContentHtml(h5pFileMountUrl!!, subHtmlStr)
//                    })
//                    val h5PJsonRequest = UMFileUtil.joinPaths(h5pFileMountUrl!!, "h5p.json")
//
//                    /* DISABLED due to compilation errors
//                    GlobalScope.launch {
//
//                        val client = HttpClient()
//                        val h4PContent = client.get<String>(h5PJsonRequest)
//
//                        view.runOnUiThread(Runnable {
//                            view.setTitle(jsonObj["title"].toString())
//                        })
//
//
//                    }*/
//                } catch (e: IOException) {
//                    dumpException(e)
//                }
//            }
//
//        }
//
//        override fun onFailure(exception: Throwable?) {
//
//        }
//    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        this.containerUid = arguments[UstadView.ARG_CONTAINER_UID]!!.toLong()
        GlobalScope.async {
            val h5pPath = mountH5PDist()
            val containerPath = mountH5PContainer(containerUid)
            var htmlStr = stringFromUtf8Bytes(UstadMobileSystemImpl.instance.getAssetAsync(context,
                    "com/ustadmobile/core/h5p/contentframe.html"))
            htmlStr = htmlStr.replace("\$DISTPATH", h5pPath)
            val h5pMountUrl2 = containerPath.substring(0, containerPath.length - 1)
            val subHtmlStr = htmlStr.replace("\$CONTENTPATH", h5pMountUrl2)
            view.runOnUiThread(Runnable {
                view.setContentHtml(h5pMountUrl2, subHtmlStr)
            })

            val client = HttpClient()
            val h5PJsonUrl = UMFileUtil.joinPaths(h5pMountUrl2, "h5p.json")
            val h5pManifest = client.get<Map<String, String>>(h5PJsonUrl)

            view.runOnUiThread(Runnable {
                val h5pTitle = h5pManifest["title"]
                if(h5pTitle != null)
                    view.setTitle(h5pTitle)
            })
        }
    }

}
