package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest2
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class ContainerUploader2(val request: ContainerUploaderRequest2,
                         val chunkSize: Int = 200*1024,
                         val endpoint: Endpoint,
                         override val di: DI) : DIAware{

    private val httpClient: HttpClient by di.instance()

    private val db: UmAppDatabase by di.on(endpoint).instance()

    suspend fun upload(): Int = withContext(Dispatchers.IO){
        //create the session
        val startFrom = httpClient.get<String>(
                "${endpoint.url}ContainerUpload2/${request.uploadUuid}/init").toLong()

        //val concatResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse2()
        0
    }

}