package com.ustadmobile.core.controller

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.PDFContentView
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

actual class PDFContentPresenter actual constructor(context: Any, arguments: Map<String, String>, view: PDFContentView,
                                                    di: DI)
    : PDFContentPresenterCommon(context, arguments, view, di) {

    private val httpClient: HttpClient by di.instance()

    private val mountHandler: ContainerMounter by instance()

    actual override fun handleOnResume() {
        GlobalScope.launch {
            val baseMountUrl = mountHandler.mountContainer(
                accountManager.activeAccount.endpointUrl,containerUid)
            val videoContent = httpClient.get<String>(UMFileUtil.joinPaths(baseMountUrl,"/videoParams"))
            val params: VideoContentPresenterCommon.VideoParams = JSON.parse(videoContent)
            val pdfPath = UMFileUtil.joinPaths(baseMountUrl,params.videoPath?:"")

            //TODO this

            view.filePath = pdfPath
            view.loading = false
        }
    }

}