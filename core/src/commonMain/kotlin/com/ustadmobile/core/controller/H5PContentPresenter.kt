package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.UstadView
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.stringFromUtf8Bytes

/**
 * Created by mike on 2/15/18.
 */


class H5PContentPresenter(context: Any, arguments: Map<String, String?>, view: H5PContentView,
                          private val containerMounter: suspend (Long) -> String) :
        H5PContentPresenterBase(context, arguments, view) {

    private var containerUid = 0L

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        this.containerUid = (arguments[UstadView.ARG_CONTAINER_UID] ?: error("")).toLong()
        GlobalScope.launch {
            val h5pPath = mountH5PDist()
            val containerPath = containerMounter(containerUid)
            var htmlStr = stringFromUtf8Bytes(UstadMobileSystemImpl.instance.getAssetAsync(context,
                    "com/ustadmobile/core/h5p/contentframe.html"))
            htmlStr = htmlStr.replace("\$DISTPATH", h5pPath)
            val h5pMountUrl2 = containerPath.substring(0, containerPath.length - 1)
            val subHtmlStr = htmlStr.replace("\$CONTENTPATH", h5pMountUrl2)
            view.runOnUiThread(Runnable {
                view.setContentHtml(h5pMountUrl2, subHtmlStr)
            })

            val client = defaultHttpClient()

            val h5PJsonUrl = UMFileUtil.joinPaths(h5pMountUrl2, "h5p.json")
            var h5pTitle = ""
            try {
                val h5pManifestStr = client.get<String>(h5PJsonUrl)
                val h5pManifestJsonObj = Json.parse(JsonObject.serializer(), h5pManifestStr)
                h5pTitle = h5pManifestJsonObj.getPrimitiveOrNull("title")?.content ?: ""
            }catch(e: Exception) {
                UMLog.l(UMLog.INFO, 100, "Could not get h5p json", e)
            }

            with(view) {
                runOnUiThread(Runnable {setContentTitle(h5pTitle) })
            }
        }
    }

}
