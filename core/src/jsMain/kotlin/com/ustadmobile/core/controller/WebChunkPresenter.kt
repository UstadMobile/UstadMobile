package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.WebChunkView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.kodein.di.DI
import org.kodein.di.instance

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: WebChunkView, di: DI)
    : WebChunkPresenterCommon(context, arguments, view, di) {

    private val httpClient: HttpClient by di.instance()

    private val mountHandler: ContainerMounter by instance()

    private val systemImpl: UstadMobileSystemImpl by instance()

    actual override suspend fun handleMountChunk() {
        val container = repo.containerDao.findByUidAsync(containerUid ?: 0L)
        if (container == null) {
            view.showSnackBar(systemImpl.getString(MR.strings.error_opening_file))
            return
        }
        val baseMountUrl = mountHandler.mountContainer(accountManager.activeAccount.endpointUrl,containerUid ?: 0)
        val indexContent: String = httpClient.get(
            UMFileUtil.joinPaths(baseMountUrl,"index.json")).body()
        val indexLog:IndexLog = JSON.parse(indexContent)
        view.url = indexLog.entries?.get(0)?.url?:""
        view.loading = false
    }
}